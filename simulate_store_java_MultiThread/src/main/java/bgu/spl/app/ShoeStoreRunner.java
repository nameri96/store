package bgu.spl.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import bgu.spl.app.Active_Objects.ManagementService;
import bgu.spl.app.Active_Objects.SellingService;
import bgu.spl.app.Active_Objects.ShoeFactoryService;
import bgu.spl.app.Active_Objects.TimeService;
import bgu.spl.app.Active_Objects.WebsiteClientService;
import bgu.spl.app.Passive_Objects.DiscountSchedule;
import bgu.spl.app.Passive_Objects.PurchaseSchedule;
import bgu.spl.app.Passive_Objects.ShoeStorageInfo;
import bgu.spl.mics.MicroService;

/*
 * This is the entry point of the program. it is responsible for reading the JSON files and loading the program accordingly.
 * This Thread terminates after running main.
 */
public class ShoeStoreRunner
{
	public static void main(String[] args)
	{
		Scanner s = new Scanner(System.in);
		System.out.println("Please insert the JSON file you wish to execute from the src/jsons directory:");
		String input = s.next();
		
		TimeService t = null;
		//Getting the JSON text from the main folder of the program.
		String jsonStr="";
		try {
			jsonStr = getJSonTesxt(System.getProperty("user.dir")+"/src/jsons/"+input+".json");
		} catch (IOException e) {}

		//initializing the executor and the list of time sensitive services.
		Executor exec = Executors.newCachedThreadPool();
		LinkedList<MicroService> servList = new LinkedList<MicroService>();
		
		/*
		 * The next try/catch block is parsing the JSON file.
		 */
		try {
			JSONObject rootObject = new JSONObject(jsonStr);
			JSONArray initialStorage = rootObject.getJSONArray("initialStorage");
			JSONObject services = rootObject.getJSONObject("services");
			CyclicBarrier barrier;
			JSONObject tempservice;

			//Parsing the manager object, and initializing its discount schedule.
			tempservice = services.getJSONObject("manager");
			JSONArray discounts = tempservice.getJSONArray("discountSchedule");
			LinkedList<DiscountSchedule> discountSchedule = new LinkedList<DiscountSchedule>();
			for(int i=0; i < discounts.length(); i++)
			{
				JSONObject discount = discounts.getJSONObject(i);


				discountSchedule.add(new DiscountSchedule(discount.getString("shoeType"),discount.getInt("tick"),discount.getInt("amount")));

				System.out.println(Store.log(discountSchedule.get(i).getShoeType()+", "+ discountSchedule.get(i).getAmount()+" Added to discount schedule."));
			}
			MicroService tempRunnable = new ManagementService(discountSchedule);
			servList.add(tempRunnable);

			
			//Initializing the store, with its storage array.
			ShoeStorageInfo[] storage = new ShoeStorageInfo[initialStorage.length()];;
			for(int i=0; i < initialStorage.length(); i++) 
			{
				JSONObject shoe = initialStorage.getJSONObject(i);


				storage[i] = new ShoeStorageInfo(shoe.getString("shoeType"), shoe.getInt("amount"), 0);

				System.out.println(Store.log(storage[i].getShoeType()+", "+ storage[i].getAmountOnStorage()+" Added to storage."));
			}
			Store.getInstance().load(storage);
			
			
			// Initializing the factories.
			for(int i = 0 ; i< services.getInt("factories") ; i++)
			{
				tempRunnable = new ShoeFactoryService("Factory_" + (i+1));
				servList.add(tempRunnable);
			}
			
			//Initializing the sellers.
			for(int i = 0 ; i< services.getInt("sellers") ; i++)
			{
				tempRunnable = new SellingService("Seller_" + (i+1));
				servList.add(tempRunnable);
			}

			//Parsing the customers with their wishlist and purchase schedule.
			JSONArray customers = services.getJSONArray("customers");
			for(int i=0; i < customers.length(); i++) 
			{
				JSONObject customer = customers.getJSONObject(i);
				
				LinkedList<String> wishList = new LinkedList<String>();
				JSONArray wishListSource = customer.getJSONArray("wishList");
				for (int j = 0; j < wishListSource.length(); j++)
				{
					wishList.add(wishListSource.getString(j));
				}
				
				LinkedList<PurchaseSchedule> purchaseSchedule = new LinkedList<PurchaseSchedule>();
				JSONArray purchaseScheduleSource = customer.getJSONArray("purchaseSchedule");
				for (int j = 0; j < purchaseScheduleSource.length(); j++)
				{
					JSONObject tempPurchase = purchaseScheduleSource.getJSONObject(j);
					purchaseSchedule.add(new PurchaseSchedule(tempPurchase.getString("shoeType"), tempPurchase.getInt("tick")));
				}
				
				tempRunnable = new WebsiteClientService(customer.getString("name"), purchaseSchedule, wishList);
				servList.add(tempRunnable);
			}
			
			/*
			 * The next part executes the services in a coordinated way, in which the entire store is initialized
			 * BEFORE the time service is started. This ensures that no service misses a tick.
			 * This system is implemented with a CyclicBarrier.
			 * ***Notice that the framework itself supports synchronized launch, with the ability to set barriers for
			 * ***different services.
			 */
			barrier = new CyclicBarrier(servList.size());
			Store.setNumberOfAgents(servList.size()+1);
			for(MicroService ms : servList)
			{
				ms.setBarrier(barrier);
				exec.execute(ms);
				//Execute each service, and give it the barrier for which it should wait.
			}
			
			//Waiting for all services to finish initializing...
			while(!(barrier.getNumberWaiting()==0)){}
			tempservice = services.getJSONObject("time");
			//Executing the time service once everybody else is ready.
			t = new TimeService(tempservice.getInt("speed"), tempservice.getInt("duration"));
			exec.execute(t);

		} catch (Exception e) {e.printStackTrace();}
		while(!(s.next().equals("shutdown")))
		{
			System.err.println("Unknown command...");
		}
		
		s.close();
		t.terminateProgram();
		System.err.println("Shutting down!");

	}

	public static String getJSonTesxt(String url) throws IOException{
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

}
