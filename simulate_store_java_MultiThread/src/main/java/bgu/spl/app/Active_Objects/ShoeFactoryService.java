package bgu.spl.app.Active_Objects;

import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.app.Store;
import bgu.spl.app.Messages.ManufacturingOrderRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Passive_Objects.Receipt;

/*
 * This service is responsible for making new shoes for the store. it operates when a manager sends a manufacturing request.
 * The factory makes one pair of shoes every tick, and notifies the manager when an order is complete.
 */
public class ShoeFactoryService extends TickListener{

	private LinkedBlockingQueue<ManufacturingOrderRequest> orders;
	private int progress;
	public ShoeFactoryService(String name) {
		super(name);
		progress=0;
		orders = new LinkedBlockingQueue<ManufacturingOrderRequest>();
	}

	@Override
	protected void initialize() {
		super.initialize();
		System.out.println(Store.log(getName() + " started"));
		subscribeBroadcast(TickBroadcast.class , bro ->{
			currentTick++;
			
			ManufacturingOrderRequest temp = orders.peek();
			if(!orders.isEmpty())
			{
				if(progress == 0)
				{
					orders.remove();
					Receipt receipt = new Receipt("Factory", "Store", temp.getShoeType(), false, bro.getTick(), temp.getRequestTick() ,temp.getInitialAmount() );
					progress = temp.getInitialAmount();
					complete(temp, receipt);
				}
				else
				{
					progress--;
					System.out.println(Store.log(this.getName()+" made one more pair of "+ temp.getShoeType()+ " and has "+progress+" more for this order"));
				}
			}
		});
		subscribeRequest(ManufacturingOrderRequest.class, req -> {
			System.out.println(Store.log(this.getName()+" got a new manufacturing request for "+req.getInitialAmount()+" " +req.getShoeType()));
			if(orders.isEmpty())
				progress = req.getInitialAmount();
			this.orders.add(req);
			req.setRequestTick(currentTick);
		});

	}

}
