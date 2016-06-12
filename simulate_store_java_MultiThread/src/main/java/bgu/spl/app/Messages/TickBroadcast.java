package bgu.spl.app.Messages;

import bgu.spl.mics.Broadcast;

/*
 * A broadcast updating every time sensitive micro service about a tick happening.
 */
public class TickBroadcast implements Broadcast{

	private String senderId;
	private int tick;

    public TickBroadcast(String senderId,int tick) {
        this.senderId = senderId;
        this.tick = tick;
        
    }

    public String getSenderId() {
        return senderId;
    }

	public int getTick() {
		return tick;
	}
}
