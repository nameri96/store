package bgu.spl.app.Messages;

import bgu.spl.mics.Broadcast;

/*
 * A broadcast informing about a new discount. 
 */
public class NewDiscountBroadcast implements Broadcast{

	String shoeType;
	int amount;
	public NewDiscountBroadcast(String shoeType, int amount)
	{
		this.shoeType = shoeType;
		this.amount = amount;
	}
	public String getShoeType()
	{
		return this.shoeType;
	}
	public int getAmount()
	{
		return this.amount;
	}

}
