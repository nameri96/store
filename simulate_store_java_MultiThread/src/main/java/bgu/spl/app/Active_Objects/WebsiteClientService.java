package bgu.spl.app.Active_Objects;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.app.Store;
import bgu.spl.app.Messages.NewDiscountBroadcast;
import bgu.spl.app.Messages.PurchaseOrderRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Passive_Objects.PurchaseSchedule;
import bgu.spl.mics.impl.MessageBusImpl;

/*
 * This is a class simulating the interaction of a buyer with the store.
 * The buyer holds two lists. The purchase Schedule is being bought when the appropriate tick is received, 
 * and the wishlist is bought when a discount is put on a shoe in the wishlist.
 */
public class WebsiteClientService extends TickListener{

	private ConcurrentHashMap<Integer ,LinkedList<PurchaseSchedule>> purchaseSchedule;
	private LinkedList<String> wishList;
	public WebsiteClientService(String name,LinkedList<PurchaseSchedule> purchaseSchedule,LinkedList<String> wishList) {
		super(name);
		System.out.println(Store.log(name+" started"));
		this.purchaseSchedule = new ConcurrentHashMap<Integer ,LinkedList<PurchaseSchedule>>();
		for(PurchaseSchedule p : purchaseSchedule)
		{
			this.purchaseSchedule.putIfAbsent(p.getTick(), new LinkedList<PurchaseSchedule>());
			this.purchaseSchedule.get(p.getTick()).add(p);
		}
		this.wishList = wishList;
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		this.subscribeBroadcast(NewDiscountBroadcast.class, bro -> 
		{ 
			if(this.wishList.contains(bro.getShoeType()))
			{
				System.out.println(Store.log(this.getName()+" is ordering "+bro.getShoeType() +" from their wishlist"));
				PurchaseOrderRequest myRequest = new PurchaseOrderRequest(this.getName(), bro.getShoeType(), true, this.currentTick);
				this.sendRequest(myRequest, reqComp ->
				{
					if(reqComp!=null && reqComp.getAmount()>0)
						wishList.remove(bro.getShoeType());
				});
			}
		}
				);

		this.subscribeBroadcast(TickBroadcast.class, bro ->
		{
			this.currentTick++;
			if(purchaseSchedule.isEmpty()&&wishList.isEmpty())
			{
				this.terminate();
				System.out.println(Store.log(this.getName()+" is done shopping and is terminating!"));
				try {
					Store.finishedAgents.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(purchaseSchedule.containsKey(bro.getTick()))
			{
				for(PurchaseSchedule p : purchaseSchedule.get(bro.getTick()))
				{
					System.out.println(Store.log(this.getName()+" is ordering "+p.getShoeType() +" on tick "+p.getTick()));
					PurchaseOrderRequest myRequest = new PurchaseOrderRequest(this.getName(), p.getShoeType(), false, this.currentTick);
					this.sendRequest(myRequest, reqComp ->
					{
						System.out.println(Store.log(this.getName()+" got "+p.getShoeType() +" on tick "+this.currentTick));
						purchaseSchedule.get(bro.getTick()).remove(p);
						
						if(purchaseSchedule.get(bro.getTick()).isEmpty())
							purchaseSchedule.remove(bro.getTick());
					});
				}
				
			}
		});
	}
}
