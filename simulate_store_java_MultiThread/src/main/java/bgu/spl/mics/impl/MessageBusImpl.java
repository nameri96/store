package bgu.spl.mics.impl;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import testingPackage.TestReciever;
import bgu.spl.app.Store;
import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;


/*
 * This class implements the message bus, an element that handles communication between micro-services.
 * for the implementation we are using three maps.
 * map - handles message sending to the micro services. it maps from the target micro service to that service's message queue.
 * MessageToMSQ - handles ordering in the request sending. maps message types to their micro service listeners.
 * ReqToRequester - handles the return messages. maps from a request to its sender.
 * 
 * This class is a singleton. the constructor is private.
 */
public class MessageBusImpl implements MessageBus
{
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> map;
	private ConcurrentHashMap<Class<? extends Message>,RoundRobinQueue<MicroService>> MessageToMSQ;
	private ConcurrentHashMap<Request<?>, MicroService> ReqToRequester;
	
	private static class MessageBusImplHolder
	{
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/*
	 * initializing all 3 maps.
	 */
	private MessageBusImpl() 
	{
		this.map = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		this.MessageToMSQ = new ConcurrentHashMap<Class<? extends Message>, RoundRobinQueue<MicroService>>();
		this.ReqToRequester = new ConcurrentHashMap<Request<?>, MicroService>();
	}

	/*
	 * get the static instance of the message bus.
	 */
	public static MessageBusImpl getInstance() 
	{
		return MessageBusImplHolder.instance;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m)
	{
	    MessageToMSQ.putIfAbsent(type, new RoundRobinQueue<MicroService>());
	    MessageToMSQ.get(type).Enqueue(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m)
	{
		MessageToMSQ.putIfAbsent(type, new RoundRobinQueue<MicroService>());
		MessageToMSQ.get(type).Enqueue(m);
	}

	@Override
	public <T> void complete(Request<T> r, T result)
	{
		map.get(ReqToRequester.get(r)).add(new RequestCompleted<T>(r, result));
	}

	@Override
	public void sendBroadcast(Broadcast b)
	{
		MessageToMSQ.putIfAbsent(b.getClass(), new RoundRobinQueue<MicroService>());
		
		for(MicroService m : MessageToMSQ.get(b.getClass()))
		{
			map.get(m).add(b);
		}

	}


	@Override
	public boolean sendRequest(Request<?> r, MicroService requester)
	{
		MessageToMSQ.putIfAbsent(r.getClass(), new RoundRobinQueue<MicroService>());
		
		while(MessageToMSQ.get(r.getClass()).size()!=0)
		{
			
			MicroService m;
			synchronized(MessageToMSQ.get(r.getClass()))
			{
				try {
					m = MessageToMSQ.get(r.getClass()).DequeueAndReEnqueue();
				} catch (InterruptedException e) {return false;}
			}
			
			synchronized (m) {
				if(map.containsKey(m)){
					ReqToRequester.put(r, requester);
					map.get(m).add(r);
					return true;
				}
			}

		}
		return false;

	}

	@Override
	public void register(MicroService m)
	{
		map.put(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public void unregister(MicroService m)
	{
		
		if(map.containsKey(m)){ 
			LinkedBlockingQueue<Message> toResend;
			synchronized(m){
				toResend = map.remove(m);
			}
			for(RoundRobinQueue<MicroService> micQueue :  MessageToMSQ.values()){
				synchronized(micQueue){
					micQueue.remove(m);
				}
			}
			for(Message message : toResend)
			{
				if(message instanceof Request)
				{
					this.sendRequest((Request<?>)message, ReqToRequester.get(message));
				}
			}
		} 
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException
	{
		Message temp = null;
		if(map.containsKey(m))
		{
			temp = map.get(m).take();
		}
		else
			throw new IllegalStateException();
		return temp;
	}

	public boolean TESTContains(MicroService m)
	{
		return map.containsKey(m);
	}

}
