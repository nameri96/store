package testingPackage;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class TestReciever extends MicroService
{
	private String m="";
	public TestReciever(String name)
	{
		super(name);
	}

	@Override
	protected void initialize()
	{
		this.subscribeBroadcast(TestBroadcast.class, bro ->{
			this.m="TestBroadcast";
		});
		this.subscribeRequest(TestRequest.class, req ->{
			this.m="TestRequest";
		});
		
	}

	public void register()
	{
		MessageBusImpl.getInstance().register(this);
		
	}

	public void unregister()
	{
		MessageBusImpl.getInstance().unregister(this);
		
	}

	public String recieve()
	{
		return m;
	}
	
	
}
