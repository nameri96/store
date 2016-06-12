package bgu.spl.mics;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

import bgu.spl.app.Messages.TerminateBroadcast;
import bgu.spl.mics.impl.MessageBusImpl;

/**
 * The MicroService is an abstract class that any micro-service in the system
 * must extend. The abstract MicroService class is responsible to get and
 * manipulate the singleton {@link MessageBus} instance.
 * <p>
 * Derived classes of MicroService should never directly touch the message-bus.
 * Instead, they have a set of internal protected wrapping methods (e.g.,
 * {@link #sendBroadcast(bgu.spl.mics.Broadcast)}, {@link #sendBroadcast(bgu.spl.mics.Broadcast)},
 * etc.) they can use . When subscribing to message-types,
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the micro-service
 * message-queue (see {@link MessageBus#register(bgu.spl.mics.MicroService)}
 * method). The abstract MicroService stores this callback together with the
 * type of the
 * message is related to.
 * <p>
 */
public abstract class MicroService implements Runnable {

	private boolean terminated = false;
	private final String name;
	private ConcurrentHashMap<Class<? extends Message>, Callback> messageM;
	private ConcurrentHashMap<Request, Callback> completedM;
	CyclicBarrier barrier;
	/**
	 * @param name the micro-service name (used mainly for debugging purposes -
	 *             does not have to be unique)
	 */
	public MicroService(String name) {
		this.name = name;
		messageM = new ConcurrentHashMap<Class<? extends Message>, Callback>();
		completedM = new ConcurrentHashMap<Request, Callback>();
		MessageBusImpl.getInstance().register(this);
		
		
	}
	public void setBarrier(CyclicBarrier b)
	{
		barrier = b;
	}

	/**
	 * subscribes to requests of type {@code type} with the callback
	 * {@code callback}. This means two things:
	 * 1. subscribe to requests in the singleton event-bus using the supplied
	 * {@code type}
	 * 2. store the {@code callback} so that when requests of type {@code type}
	 * received it will be called.
	 * <p>
	 * for a received message {@code m} of type {@code type = m.getClass()}
	 * calling the callback {@code callback} means running the method
	 * {@link Callback#call(java.lang.Object)} by calling
	 * {@code callback.call(m)}.
	 * <p>
	 * @param <R>      the type of request to subscribe to
	 * @param type     the {@link Class} representing the type of request to
	 *                 subscribe to.
	 * @param callback the callback that should be called when messages of type
	 *                 {@code type} are taken from this micro-service message
	 *                 queue.
	 */
	protected final <R extends Request> void subscribeRequest(Class<R> type, Callback<R> callback) {
		messageM.put(type, callback);
		MessageBusImpl.getInstance().subscribeRequest(type, this);
	}

	/**
	 * subscribes to broadcast message of type {@code type} with the callback
	 * {@code callback}. This means two things:
	 * 1. subscribe to broadcast messages in the singleton event-bus using the
	 * supplied {@code type}
	 * 2. store the {@code callback} so that when broadcast messages of type
	 * {@code type} received it will be called.
	 * <p>
	 * for a received message {@code m} of type {@code type = m.getClass()}
	 * calling the callback {@code callback} means running the method
	 * {@link Callback#call(java.lang.Object)} by calling
	 * {@code callback.call(m)}.
	 * <p>
	 * @param <B>      the type of broadcast message to subscribe to
	 * @param type     the {@link Class} representing the type of broadcast
	 *                 message to
	 *                 subscribe to.
	 * @param callback the callback that should be called when messages of type
	 *                 {@code type} are taken from this micro-service message
	 *                 queue.
	 */
	protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
		messageM.put(type, callback);
		MessageBusImpl.getInstance().subscribeBroadcast(type, this);
	}

	/**
	 * send the request {@code r} using the message-bus and storing the
	 * {@code onComplete} callback so that it will be executed <b> in this
	 * micro-service event loop </b> once the request is complete.
	 * <p>
	 * @param <T>        the type of the expected result of the request
	 *                   {@code r}
	 * @param r          the request to send
	 * @param onComplete the callback to call when {@code r} is completed. This
	 *                   callback expects to receive (i.e., in the
	 *                   {@link Callback#call(java.lang.Object)} first argument)
	 *                   the result provided when the micro-service receiving {@code r} completes
	 *                   it.
	 * @return true if there was at least one micro-service subscribed to
	 *         {@code r.getClass()} and false otherwise.
	 */
	protected final <T> boolean sendRequest(Request<T> r, Callback<T> onComplete) {
		if(MessageBusImpl.getInstance().sendRequest(r, this)){
			completedM.put(r, onComplete);
			return true; 
		}return false;
	}

	/**
	 * send the broadcast message {@code b} using the message-bus.
	 * <p>
	 * @param b the broadcast message to send
	 */
	protected final void sendBroadcast(Broadcast b) {
		MessageBusImpl.getInstance().sendBroadcast(b);
	}

	/**
	 * complete the received request {@code r} with the result {@code result}
	 * using the message-bus.
	 * <p>
	 * @param <T>    the type of the expected result of the received request
	 *               {@code r}
	 * @param r      the request to complete
	 * @param result the result to provide to the micro-service requesting
	 *               {@code r}.
	 */
	protected final <T> void complete(Request<T> r, T result) {
		MessageBusImpl.getInstance().complete(r, result);
	}

	/**
	 * this method is called once when the event loop starts.
	 */
	protected abstract void initialize();

	/**
	 * signal the event loop that it must terminate after handling the current
	 * message.
	 */
	protected final void terminate() {
		this.terminated = true;
	}

	/**
	 * @return the name of the service - the service name is given to it in the
	 *         construction time and is used mainly for debugging purposes.
	 */
	public final String getName() {
		return name;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public final void run() 
	{
		initialize();
		try
		{
			if(barrier!=null)
					barrier.await();
			
		} catch (Exception e){}
		Message recieve=null;
		while (!terminated) 
		{
			MessageBus bus = MessageBusImpl.getInstance();
			try
			{
				recieve = bus.awaitMessage(this);
				if(!(recieve instanceof RequestCompleted<?>))
					this.messageM.get(recieve.getClass()).call(recieve);
				else
				{
					RequestCompleted<?> temp = (RequestCompleted<?>)recieve;
					this.completedM.get(temp.getCompletedRequest()).call(temp.getResult());
					
				}
			} catch (InterruptedException e)
			{
				this.terminated=true;
			}
			catch (Exception e)
			{
				System.out.println(recieve.getClass());
				System.out.print(this.getName()+": ");
				System.out.println(e.getMessage());
			}

		}
		MessageBusImpl.getInstance().unregister(this);
		try
		{
			Thread.currentThread().join();
		} catch (InterruptedException e){}
	}

}
