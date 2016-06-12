package bgu.spl.mics.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import bgu.spl.mics.MicroService;

public class RoundRobinQueue<E> implements Iterable<E>{

	/*
	 * This class supplies an iterable queue that is fully implementing the round robin order method.
	 * We are using two sub-queues, when adding an element that was already in the queue, we add it in the end of the entire queue
	 * and when adding a new element, we add it in the end of the primary queue. 
	 * 
	 * 
	 */
	private ConcurrentLinkedQueue<E> majorQueue;
	private ConcurrentLinkedQueue<E> seconderyQueue;

	/*
	 * Initializing the two queues
	 */
	public RoundRobinQueue() {
		super();
		this.majorQueue = new ConcurrentLinkedQueue<E>();
		this.seconderyQueue = new ConcurrentLinkedQueue<E>();
	}
	
	/*
	 * implementing the removing of an existing element and re-enqueueing it.
	 * this method is synchronized so that the order of the queue is preserved.
	 */
	public synchronized E DequeueAndReEnqueue() throws InterruptedException
	{
		while (majorQueue.size()==0)
		{
			if(seconderyQueue.size()!=0)
			{
				moveQueues();
				return take();
			}
			else
				this.wait();
		}
		return take();
	}
	
	
	private E take()
	{
		E temp = majorQueue.poll();
		seconderyQueue.add(temp);
		return temp;
	}
	
	//may not need to be synchronized, and should not include notifyAll().
	public synchronized boolean Enqueue(E e)
	{
		boolean temp = majorQueue.add(e);
		this.notifyAll();
		return temp;
		
	}

	private void moveQueues(){
		majorQueue.addAll(seconderyQueue);
		seconderyQueue.clear();
	}

	public void remove(MicroService m) {
		majorQueue.remove(m);
		seconderyQueue.remove(m);

	}

	@Override
	public Iterator<E> iterator()
	{
		ConcurrentLinkedQueue<E> temp = new ConcurrentLinkedQueue<E>();
		temp.addAll(majorQueue);
		temp.addAll(seconderyQueue);
		return temp.iterator();
	}

	public int size()
	{
		return this.majorQueue.size()+this.seconderyQueue.size();
	}


}