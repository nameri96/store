package bgu.spl.app.Passive_Objects;


/*
 * This class is a simple receipt for sending a purchase information between micro-services.
 * it also comes with a nice looking toString() for displaying it in the console.
 */
public class Receipt {

	public Receipt(String seller, String customer, String shoeType, boolean discount, int issuedTick, int requestTick,
			int amountSold) {
		this.seller = seller;
		this.customer = customer;
		this.shoeType = shoeType;
		this.discount = discount;
		this.issuedTick = issuedTick;
		this.requestTick = requestTick;
		this.amountSold = amountSold;
	}
	
	@Override
	public String toString() {
		return " ____________________________________________________________\n"
				+ "| Seller: " + seller + " | Customer: " + customer + " | Shoe Type: " + shoeType + "\n"
				+ "| Discount: "	+discount + " | Issued Tick: " + issuedTick+ " | Request Tick: " + requestTick+ " | Amount Sold="+amountSold 
				+"\n|____________________________________________________________\n";
	}

	private String seller,customer,shoeType;
	private boolean discount;
	private int issuedTick,requestTick,amountSold;
	public int getAmount()
	{
		return this.amountSold;
	}
}
