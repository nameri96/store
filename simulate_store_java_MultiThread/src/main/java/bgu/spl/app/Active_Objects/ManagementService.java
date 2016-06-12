package bgu.spl.app.Active_Objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.app.Store;
import bgu.spl.app.Messages.ManufacturingOrderRequest;
import bgu.spl.app.Messages.NewDiscountBroadcast;
import bgu.spl.app.Messages.RestockRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Passive_Objects.DiscountSchedule;

/*
 * This service is responsible for managing manufacturing, discounts.
 * The manager keeps track of the manufacturing orders that are in progress, as requested in the assignment.
 */
public class ManagementService extends TickListener{

	private ConcurrentHashMap<Integer ,DiscountSchedule> discounts;
	private ConcurrentHashMap<String, ManufacturingOrderRequest> inProgress;
	private ConcurrentHashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>> waitingForShoes;
	public ManagementService(LinkedList<DiscountSchedule> disc) {
		super("manager");
		this.discounts = new ConcurrentHashMap<Integer, DiscountSchedule>();
		this.inProgress = new ConcurrentHashMap<String, ManufacturingOrderRequest>();
		this.waitingForShoes = new ConcurrentHashMap<ManufacturingOrderRequest, LinkedList<RestockRequest>>();
		for(DiscountSchedule d : disc)
		{
			discounts.put(d.getTick(), d);
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		System.out.println(Store.log(getName() + " started"));
		subscribeBroadcast(TickBroadcast.class, req ->{
			this.currentTick++;
			if(discounts.containsKey(currentTick))
			{
				DiscountSchedule d = discounts.remove(currentTick);
				if(Store.getInstance().exists(d.getShoeType()))
				{
					System.out.println(Store.log(this.getName()+": got tick "+currentTick+" and is making a discount on "+d.getAmount()+" "+d.getShoeType()));
					Store.getInstance().addDiscounts(d.getShoeType(), d.getAmount());
					sendBroadcast(new NewDiscountBroadcast(d.getShoeType(),d.getAmount()));
				}
				else
				{
					System.out.println(Store.log(this.getName()+": got tick "+currentTick+" and is NOT(!!!!) making a discount on non-existing shoes!!!!"));
				}
			}	
		});

		subscribeRequest(RestockRequest.class, req -> {
			System.out.println(Store.log(getName() + " got a new restock request from " + req.getSenderName()));
			boolean order = false;
			if(this.inProgress.containsKey(req.getShoeType()))
			{
				ManufacturingOrderRequest temp = inProgress.get(req.getShoeType());
				if(temp.getAmount()-1>0)
				{
					waitingForShoes.get(temp).add(req);
					temp.decreaseAmount();
				}
				else
				{
					
					this.inProgress.remove(req.getShoeType());
					order = true;
				}
			}
			else
				order = true;
			if(order)
			{
				ManufacturingOrderRequest temp = new ManufacturingOrderRequest(this.getName(),req.getShoeType(),(currentTick%5)+1);
				this.inProgress.put(temp.getShoeType(), temp);
				waitingForShoes.put(temp, new LinkedList<RestockRequest>());
				waitingForShoes.get(temp).add(req);
				sendRequest(temp, V -> {
					if(V!=null){
						System.out.println(Store.log(getName() + " got notified about manufacturing completion for "+temp.getInitialAmount()+" "+temp.getShoeType()));
						Store.getInstance().add(req.getShoeType(), temp.getInitialAmount());
						Store.getInstance().file(V);
					}
					
					for(RestockRequest r : waitingForShoes.get(temp))
					{
						complete(r, V!=null);
					}
				});
			}

		});
	}
}
