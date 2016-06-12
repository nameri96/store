package bgu.spl.app.Messages;


import bgu.spl.mics.Request;

/*
 * A request sent from a seller to the manager about missing items in the store's storage.
 */
public class RestockRequest implements Request<Boolean>{

	private Boolean isCompleted;
	private String senderName;
	private String shoeType;

	public RestockRequest(String senderName,String shoeType) {
		this.senderName = senderName;
		this.shoeType = shoeType;
	}

	public Boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(Boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public String getSenderName() {
		return this.senderName;
	}

	public String getShoeType() {
		return shoeType;
	}



}
