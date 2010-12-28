package com.chat.client;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The state of a room. 
 */
public class RoomState implements Serializable {
	private static final long serialVersionUID = -19530997763084019L;
	/**
	 * The time stamp of the first message.
	 */
	private long firstCount;
	/**
	 * The time stamp of the last message.
	 */
	private long lastCount;
	
	/**
	 * New messages.
	 */
	private ArrayList<Message> messages;
	
	/**
	 * Users in the room.
	 */
	private ArrayList<User> users;
	
	/**
	 * Default constructor to make RoomState serializable.
	 * Do not use this if you don't know what you are doing.
	 */
	public RoomState() {
		firstCount = -1;
		lastCount = -1;
	}
	
	/**
	 * Use this to create new RoomState objects.
	 * 
	 * @param messages
	 * @param users
	 */
	public RoomState(ArrayList<Message> messages, ArrayList<User> users){
		if(messages.size() > 0)
		{
			//Find the first and last time stamp.
			this.firstCount = messages.get(0).getCounter();
			this.lastCount = messages.get(messages.size() - 1).getCounter();
		}
		else
		{
			//If there are no new messages set the time stamps to -1.
			this.firstCount = -1;
			this.lastCount = -1;
		}
		
		this.messages = messages;
		this.users = users;
	}

	/**
	 * Returns the first time stamp.
	 *  
	 * @return
	 */
	public long getFirstCount() {
		return firstCount;
	}

	/**
	 * Returns the last time stamp.
	 * 
	 * @return
	 */
	public long getLastCount() {
		return lastCount;
	}

	/**
	 * Returns the new massages.
	 * 
	 * @return
	 */
	public ArrayList<Message> getMessages() {
		return messages;
	}

	/**
	 * Return the list of users in the room.
	 * 
	 * @return
	 */
	public ArrayList<User> getUsers() {
		return users;
	}
}
