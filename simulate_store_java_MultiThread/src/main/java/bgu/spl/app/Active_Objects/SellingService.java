package bgu.spl.app.Active_Objects;

import bgu.spl.app.Store;
import bgu.spl.app.Store.BuyResult;
import bgu.spl.app.Messages.PurchaseOrderRequest;
import bgu.spl.app.Messages.RestockRequest;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.app.Passive_Objects.Receipt;

/*
 * This is the seller service, communicating between a buyer and the store.
 * The seller is responsible for making the purchases the client requests.
 */
public class SellingService extends TickListener{

	public SellingService(String name) {
		super(name);
	}

	@Override
	protected void initialize() {
		super.initialize();
		System.out.println(Store.log(getName() + " started"));

		subscribeBroadcast(TickBroadcast.class, req ->{
			this.currentTick++;
		});

		subscribeRequest(PurchaseOrderRequest.class, req -> {
			System.out.println(Store.log(this.getName()+" got a purchase from "+req.getSenderName()+" for a pair of "+ req.getShoeType()));
			BuyResult res = Store.getInstance().take(req.getShoeType(), req.isOnlyDiscount());
			Receipt answer;
			if(res.compareTo(BuyResult.NO_SHOE_EXISTS)==0)
			{
				System.out.println(Store.log(req.getSenderName()+", there are no "+ req.getShoeType() +" shoes in our storage."));
				complete(req,null);
			}
			else
			{
				if(res.compareTo(BuyResult.NOT_IN_STOCK)==0)
				{
					sendRequest(new RestockRequest(this.getName(),req.getShoeType()), V -> {
						System.out.println(Store.log("Sender " + getName() + " got notified about request completion with result: \"" + V + "\""));
						Receipt answer2;
						boolean onDiscount=false;
						if(V){
							BuyResult br = Store.getInstance().take(req.getShoeType(), req.isOnlyDiscount());
							onDiscount = br.compareTo(BuyResult.DISCOUNTED_PRICE)==0;
							if(!(br.compareTo(BuyResult.DISCOUNTED_PRICE)==0)&& req.isOnlyDiscount())
								answer2=null;
							else
								answer2 = new Receipt(this.getName(), req.getSenderName(), req.getShoeType(), onDiscount,currentTick , req.getRequestTick(), 1);
						}else
							answer2=null;
						if(answer2!=null)
						{
							String toPrint = (onDiscount) ? req.getSenderName()+ " got their "+req.getShoeType()+" on discount!" : req.getSenderName()+ " got their "+req.getShoeType() ;
							System.out.println(Store.log(toPrint+" from "+this.getName()));
							Store.getInstance().file(answer2);
						}
						complete(req, answer2);

					});

				}
				else
				{  
					if(res.compareTo(BuyResult.NOT_ON_DISCOUNT)==0&&req.isOnlyDiscount())
					{
						System.out.println(Store.log("We're sorry, "+req.getSenderName()+" but there is no discount on "+req.getShoeType()));
						complete(req,null);
					}
					else
					{
						boolean onDiscount = res.compareTo(BuyResult.DISCOUNTED_PRICE)==0;
						answer = new Receipt(this.getName(), req.getSenderName(), req.getShoeType(), onDiscount,currentTick , req.getRequestTick(), 1);
						String toPrint = (onDiscount) ? req.getSenderName()+ " got their "+req.getShoeType()+" on discount!" : req.getSenderName()+ " got their "+req.getShoeType() ;
						System.out.println(Store.log(toPrint+" from "+this.getName()));
						Store.getInstance().file(answer);
						complete(req, answer);
					}
				}
			}
		});


	}

}
