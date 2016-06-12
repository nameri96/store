package bgu.spl.app.Passive_Objects;

/*
 * an item in a manager's discount schedule.
 */
public class DiscountSchedule {

	private String shoeType;
	private int tick,amount;
	
	public DiscountSchedule(String shoeType, int tick, int amount) {
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}

	public Integer getTick() {
		return this.tick;
	}

	public String getShoeType() {
		return shoeType;
	}


	public int getAmount() {
		return amount;
	}
	
}
