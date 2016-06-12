package bgu.spl.app.Active_Objects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import bgu.spl.app.Store;
import bgu.spl.app.Messages.TerminateBroadcast;
import bgu.spl.app.Messages.TickBroadcast;
import bgu.spl.mics.MicroService;

/*
 * This MicroService is responsible for updating the tick counter in all time sensitive services.
 * This is done using an event listener that sends a broadcast upon tick update.
 */
public class TimeService extends MicroService{

	private int currenttime,speed,duration;
	private Timer time;
	public TimeService(int speed,int duration) {
		super("timer");
		this.speed = speed;
		this.duration = duration;
		this.currenttime = 0;
	}

	@Override
	protected void initialize() {
		System.out.println(Store.log(this.getName()+": start"));
		subscribeBroadcast(TerminateBroadcast.class, bro ->{
			terminate();
			while(!(Store.finishedAgents.getNumberWaiting()==Store.finishedAgents.getParties()-1)){}
			try
			{
				Store.finishedAgents.await();
			} catch (Exception e1){}
			Store.getInstance().print();
			System.err.println("All threads have shut down, Shutting down the program.");
			System.exit(0);
		});
		time = new Timer(speed,new ActionListener() {
			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currenttime!=duration){
					currenttime++;
					System.out.println(Store.log("Tick: " +currenttime));
					sendBroadcast(new TickBroadcast(getName(), currenttime));
				}else{
					time.stop();
					sendBroadcast(new TerminateBroadcast(false));
									
				}
			}
		});
		time.start();
		
	}
	public void terminateProgram()
	{
		time.stop();
		sendBroadcast(new TerminateBroadcast(true));
	}

}
