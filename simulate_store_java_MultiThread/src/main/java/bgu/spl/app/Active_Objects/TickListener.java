package bgu.spl.app.Active_Objects;

import bgu.spl.app.Store;
import bgu.spl.app.Messages.TerminateBroadcast;
import bgu.spl.mics.MicroService;

/*
 * This is a super class for every time sensitive micro service.
 * It subscribes, by default to the Termination broadcast, so it stops upon time ending.
 * it also holds a field for current tick, which should be updated from the inheriting class.
 */
public abstract class TickListener extends MicroService{

	protected int currentTick;

	public TickListener(String name) {
		super(name);
		currentTick = 0;

	}

	protected void initialize(){
		subscribeBroadcast(TerminateBroadcast.class, bro ->
		{
			String name = this.getName();
			System.out.println(Store.log(name+" has recieved a terminating message, and is reporting to the store."));
			this.terminate();
			try {
				Store.finishedAgents.await();
			} catch (Exception e) {}
		});
	}

}
