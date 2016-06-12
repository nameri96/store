package testingPackage;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;
import bgu.spl.app.Passive_Objects.ShoeStorageInfo;

/**
 * @author TestingGhost
 * This class will test both the store.
 * 
 * this class assumes there is a valid JSON file in the jsons directory that builds the storage for the test.
 *
 */
public class TestingStore
{
	
	/**
	 * @throws java.lang.Exception
	 * setting up the storage from a testStore.json
	 * file and firing up a store instance.
	 */
	private LinkedList<Pair<String,Integer>> shoeTypes;
	@Before
	public void setUp() throws Exception
	{
		resetStorage();
		shoeTypes = new LinkedList<Pair<String,Integer>>();
		String jsonStr="";
		try {
			jsonStr = getJSonTesxt(System.getProperty("user.dir")+"/jsons/testStorage.json");
		} catch (IOException e) {}
		
		JSONObject rootObject = new JSONObject(jsonStr);
		JSONArray initialStorage = rootObject.getJSONArray("initialStorage");
		
		ShoeStorageInfo[] storage = new ShoeStorageInfo[initialStorage.length()];;
		for(int i=0; i < initialStorage.length(); i++) 
		{
			JSONObject shoe = initialStorage.getJSONObject(i);
			storage[i] = new ShoeStorageInfo(shoe.getString("shoeType"), shoe.getInt("amount"), 0);
			System.out.println(storage[i].getShoeType()+", "+ storage[i].getAmountOnStorage()+" Added to storage.");
			shoeTypes.add(new Pair(storage[i].getShoeType(),storage[i].getAmountOnStorage()));
		}
		Store.getInstance().load(storage);
	}
	
	private static String getJSonTesxt(String url) throws IOException
	{
		File f = new File(url);
		String result = "";
		FileReader fr = new FileReader(f);
		org.json.JSONTokener j = new JSONTokener(fr);
		while(j.more())
		{
			result += j.next();
		}
		fr.close();
		return result.replace("\\\"", "\"");
	}
	
	@Test
	public void testStoreSingletonIsNotNull()
	{
		assertNotNull(Store.getInstance());
	}
	@Test
	public void testStorageAmount()
	{
		
		for (int index = 0; index < shoeTypes.size() ; index++)
		{
			int i=0;
			while(Store.getInstance().take(shoeTypes.get(index).getFirst(), false).compareTo(BuyResult.NOT_IN_STOCK)!=0)
			{
				i++;
			}
			assertEquals(shoeTypes.get(index).getSecond().intValue(), i);
		}
	}
	@Test
	public void testNoShoeExists()
	{
		
		for (int i = 0; i < 100; i++)
		{
			BuyResult res = Store.getInstance().take("NEVERWASINSTORAGE"+i, false);
			assertEquals(BuyResult.NO_SHOE_EXISTS, res);
		}
	}
	@Test
	public void testBoughtWithDiscount() throws Exception
	{
		
		for (int index = 0; index < shoeTypes.size() ; index++)
		{
			BuyResult res = Store.getInstance().take(shoeTypes.get(index).getFirst(), false);
			assertEquals(BuyResult.REGULAR_PRICE, res);
			Store.getInstance().add(shoeTypes.get(index).getFirst(), 1);
			Store.getInstance().addDiscounts(shoeTypes.get(index).getFirst(), 1);
			res = Store.getInstance().take(shoeTypes.get(index).getFirst(), false);
			assertEquals(BuyResult.DISCOUNTED_PRICE, res);
		}
	}
	@Test
	public void testNonOnDiscount() throws Exception
	{
		for (int index = 0; index < shoeTypes.size() ; index++)
		{
			BuyResult res = Store.getInstance().take(shoeTypes.get(index).getFirst(), true);
			assertEquals(BuyResult.NOT_ON_DISCOUNT, res);
		}
	}
	@Test
	public void testNotInStock()
	{
		for (int index = 0; index < shoeTypes.size() ; index++)
		{
			int i=0;
			while(Store.getInstance().take(shoeTypes.get(index).getFirst(), false).compareTo(BuyResult.NOT_IN_STOCK)!=0)
			{
				i++;
			}
			for (int j = 0; j < 1000; j++)
			{
				assertEquals(Store.getInstance().take(shoeTypes.get(index).getFirst(), false), BuyResult.NOT_IN_STOCK);
			}
			
		}
	}
	@Test
	public void testExitBarrier() throws InterruptedException
	{
		
		Store.setNumberOfAgents(2);
		Thread StoreWorker1 = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					//Thread.currentThread().sleep(1000);
					Store.finishedAgents.await();
				} catch (InterruptedException e){
					e.printStackTrace();
				} catch (BrokenBarrierException e){
					e.printStackTrace();
				}
				
			}
		});
		Thread StoreWorker2 = new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				try
				{
					//Thread.currentThread().sleep(1000);
					Store.finishedAgents.await();
				} catch (InterruptedException e){
					e.printStackTrace();
				} catch (BrokenBarrierException e){
					e.printStackTrace();
				}
				
			}
		});
		StoreWorker1.start();
		Thread.sleep(1000);
		assertEquals(1,Store.finishedAgents.getNumberWaiting());
		StoreWorker2.start();
		Thread.sleep(1000);
		assertEquals(0,Store.finishedAgents.getNumberWaiting());
		
		
	}
	@Test
	public void testMultipleBuyersAtOnce() throws InterruptedException
	{
		Executor e = Executors.newFixedThreadPool(10);
		for (int i = 0; i < 10; i++)
		{
			e.execute(new Runnable()
			{
				
				@Override
				public void run()
				{	
					Store.getInstance().take("Zero", false);
				}
			});
		}
		Thread.sleep(1000);
		for (int i = 0; i < 5; i++)
		{
			assertEquals(Store.getInstance().take("Zero", false), BuyResult.REGULAR_PRICE);
		}
		assertEquals(Store.getInstance().take("Zero", false), BuyResult.NOT_IN_STOCK);
		
	}
	
	private void resetStorage()
	{
		Store.getInstance().resetStorage();
	}
	
	
	
}
