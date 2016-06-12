package bgu.spl.app.Messages;


import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.app.Passive_Objects.Receipt;
import bgu.spl.mics.Request;

/*
 * a request sent from the manager to the shoe factory about restocking request.
 */
public class ManufacturingOrderRequest implements Request<Receipt>{

	 private String senderName,shoeType;
	 private AtomicInteger tempAmount;
	 private final int finalAmount;
	 private int requestTick;
	    public ManufacturingOrderRequest(String senderName,String shoeType,int amount) {
	    	this.senderName = senderName;
	    	this.shoeType = shoeType;
	    	this.tempAmount = new AtomicInteger(amount);
	    	this.finalAmount = amount;
	    }

		public String getSenderName() {
			return senderName;
		}
		public int getInitialAmount()
		{
			return finalAmount;
		}

		public String getShoeType() {
			return shoeType;
		}
		
		public int getAmount() {
			return tempAmount.get();
		}
		public void decreaseAmount()
		{
			tempAmount.decrementAndGet();
		}

		public int getRequestTick() {
			return requestTick;
		}

		public void setRequestTick(int requestTick) {
			this.requestTick = requestTick;
		}

}
