package bgu.spl.app.Messages;

import bgu.spl.app.Passive_Objects.Receipt;
import bgu.spl.mics.Request;


/*
 * This class is a request sent from a client web service to a seller.
 */
 
public class PurchaseOrderRequest implements Request<Receipt>{

	private String senderName,shoeType;
	private boolean onlyDiscount;
	private int requestTick;
	public PurchaseOrderRequest(String senderName,String shoeType,boolean onlyDiscount, int tick) {
		this.senderName = senderName;
		this.shoeType = shoeType;
		this.onlyDiscount = onlyDiscount;
		this.requestTick = tick;
	}

	public String getSenderName() {
		return senderName;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean isOnlyDiscount() {
		return onlyDiscount;
	}

	public int getRequestTick() {
		return requestTick;
	}

}
