package bgu.spl.app.Passive_Objects;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * This class holds information about a type of shoe that is in storage.
 */
public class ShoeStorageInfo {

	private String shoeType;
	private AtomicInteger amountOnStorage,discountedAmount;
	
	public ShoeStorageInfo(String shoeType,int amountOnStorage,int discountedAmount){
		this.shoeType=shoeType;
		this.amountOnStorage = new AtomicInteger(amountOnStorage);
		this.discountedAmount = new AtomicInteger(discountedAmount);
	}
	
	public void Buy(){
		this.amountOnStorage.decrementAndGet();
	}

    public void BuyWithDiscount(){
		Buy();
    	this.discountedAmount.decrementAndGet();
	}
	
	@Override
	public String toString() {
		return "ShoeStorageInfo [shoeType=" + shoeType + ", amountOnStorage=" + amountOnStorage + ", discountedAmount="
				+ discountedAmount + "]";
	}

	public String getShoeType() {
		return shoeType;
	}
	public void setShoeType(String shoeType) {
		this.shoeType = shoeType;
	}
	public int getAmountOnStorage() {
		return amountOnStorage.get();
	}
	public void setAmountOnStorage(int amountOnStorage) {
		this.amountOnStorage.addAndGet(amountOnStorage);
	}
	public int getDiscountedAmount() {
		return discountedAmount.get();
	}
	public void setDiscountedAmount(int discountedAmount) {
		this.discountedAmount.addAndGet(discountedAmount);
	}
	
}
