package testingPackage;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.impl.MessageBusImpl;

public class TestSender extends MicroService
{
	
	public TestSender(String name)
	{
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initialize()
	{
		// TODO Auto-generated method stub
		
	}

	public void register()
	{
		MessageBusImpl.getInstance().register(this);
		
	}

	public void unregister()
	{
		MessageBusImpl.getInstance().unregister(this);
		
	}

	@SuppressWarnings("unchecked")
	public void sendRequest(String string)
	{
		this.sendRequest(new TestRequest<Boolean>(), req ->{
		});
		
	}

	public void sendBroadcast(String string)
	{
		this.sendBroadcast(new TestBroadcast());
	}
	
}
