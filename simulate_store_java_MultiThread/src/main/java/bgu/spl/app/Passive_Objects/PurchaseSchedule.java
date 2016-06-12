package bgu.spl.app.Passive_Objects;


/*
 * An item in a customer's purchase schedule.
 */
public class PurchaseSchedule {

	private String shoeType;
	private int tick;
	
	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}

	public int getTick()
	{
		return this.tick;
	}

	public String getShoeType()
	{
		return this.shoeType;
	}
	
}
