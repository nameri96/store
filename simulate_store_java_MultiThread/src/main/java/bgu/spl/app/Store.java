package bgu.spl.app;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

import bgu.spl.app.Passive_Objects.Receipt;
import bgu.spl.app.Passive_Objects.ShoeStorageInfo;

/*
 * This is the singleton store class.
 * this class is the gateway for every interaction with the storage (Selling, restocking, adding discounts),
 * and for the filing system.
 * 
 * This class holds a record of the shoe storage, a list of receipts filed on the current run,
 * an enum describing the purchase response, and a CyclicBarrier for termination intentions.
 * 
 *  
 */
public class Store {

	private static String _log = "";
	private ConcurrentHashMap<String, ShoeStorageInfo> shoesStorage;
	private LinkedList<Receipt> receipts;
	public enum BuyResult{NOT_IN_STOCK,NOT_ON_DISCOUNT,REGULAR_PRICE,DISCOUNTED_PRICE,NO_SHOE_EXISTS}
	public static CyclicBarrier finishedAgents;
	private static class StoreHolder 
	{
		private static Store instance = new Store();

	}
	private Store() 
	{
		receipts = new LinkedList<>();
		shoesStorage = new ConcurrentHashMap<String, ShoeStorageInfo>();

	}
	public static Store getInstance() 
	{
		return StoreHolder.instance;
	}
	public static void setNumberOfAgents(int num)
	{
		finishedAgents = new CyclicBarrier(num);
	}

	public void load ( ShoeStorageInfo [ ] storage ){

		for(ShoeStorageInfo shoe : storage){
			if(shoesStorage.containsKey(shoe.getShoeType())){
				add(shoe.getShoeType(),shoe.getAmountOnStorage());
				addDiscounts(shoe.getShoeType(),shoe.getDiscountedAmount());
			}else
				shoesStorage.put(shoe.getShoeType(), shoe);
		}
	}
	public boolean exists(String shoeType)
	{
		return (shoesStorage.containsKey(shoeType));
	}
	public BuyResult take(String shoeType, boolean onlyDiscount){
		if(!shoesStorage.containsKey(shoeType))
			return BuyResult.NO_SHOE_EXISTS;
		else{
			synchronized (shoesStorage.get(shoeType)) {
				if(shoesStorage.get(shoeType).getAmountOnStorage()==0)
					return BuyResult.NOT_IN_STOCK;
				if(onlyDiscount){
					if(shoesStorage.get(shoeType).getDiscountedAmount()==0)
						return BuyResult.NOT_ON_DISCOUNT;
					else{
						shoesStorage.get(shoeType).BuyWithDiscount();
						return BuyResult.DISCOUNTED_PRICE;
					}
				}else{
					if(shoesStorage.get(shoeType).getDiscountedAmount()> 0){
						shoesStorage.get(shoeType).BuyWithDiscount();
						return BuyResult.DISCOUNTED_PRICE;
					}
					else{
						shoesStorage.get(shoeType).Buy();
						return BuyResult.REGULAR_PRICE;
					}
				}
			}
		}
	}

	public void addDiscounts(String shoeType, int discountedAmount) {
		shoesStorage.get(shoeType).setDiscountedAmount(discountedAmount);

	}

	public void add(String shoeType, int amountOnStorage) {
		shoesStorage.get(shoeType).setAmountOnStorage(amountOnStorage);

	}

	public void file(Receipt receipt){
		receipts.add(receipt);
	}

	public void print(){
		for(ShoeStorageInfo shoeType : shoesStorage.values())
			System.out.println(log(shoeType.toString()));
		for(Receipt receipt : receipts)
			System.out.println(log(receipt.toString()));

		try {
			System.out.println("A log file was created in the project main source directory");
			PrintWriter out = new PrintWriter("src/log.txt");
			out.println(_log);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void resetStorage()
	{
		shoesStorage.clear();
	}

	public static String log(String message)
	{
		synchronized(_log)
		{
			_log+=message+"\n";
		}
		return message;

	}
}
