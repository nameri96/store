package bgu.spl.app.Messages;

import bgu.spl.mics.Broadcast;
/*
 * A simple message containing a boolean for urgent shutdowns.
 */
public class TerminateBroadcast implements Broadcast
{
	private boolean Urgent;
	
	public TerminateBroadcast(boolean Urgent)
	{
		this.Urgent = Urgent;
	}
	
	public boolean isUrgent()
	{
		return Urgent;
	}
}
