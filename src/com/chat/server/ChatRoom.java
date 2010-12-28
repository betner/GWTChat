package com.chat.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import com.chat.client.Message;
import com.chat.client.RoomState;
import com.chat.client.User;
import com.chat.client.UserType;
import com.chat.client.exceptions.DecryptionException;
import com.chat.client.exceptions.EncryptionException;
import com.chat.client.exceptions.NotInAllowList;
import com.chat.client.exceptions.UserAlreadyInRoom;
import com.chat.client.exceptions.UserNotAllowed;
import com.chat.client.exceptions.UserNotInRoom;

/**
 * A chat room.
 */
public class ChatRoom {
	/**
	 * The size of the message buffer.
	 */
	static final private int BUFFER_SIZE = 1000;

	/**
	 * A "fake" user with system privilages. Used for sending system messages to the users.
	 */
	private static final User SYSTEM_USER = new User("System", UserType.System); 

	/**
	 * Buffer for messages.
	 */
	private LinkedList<Message> buffer = new LinkedList<Message>();
	
	/**
	 * Used to create sequential time stamps for messages.
	 */
	private long counter;
	/**
	 * Holds the time stamp of the last message.
	 */
	private long lastCount;
	/**
	 * The name of the room.
	 */
	private String name;
	/**
	 * True if the room is public and false if private.
	 */
	private boolean isPublic;
	
	/**
	 * A list of all users in the room.
	 */
	private HashMap<String, User> users = new HashMap<String, User>();
	/**
	 * If the room is public then this is a blacklist and if the room is private it's a whitelist.
	 */
	private HashSet<String> allowSet = new HashSet<String>();

	/**
	 * Constructor
	 * @param
	 * @param isPublic
	 */
	public ChatRoom(String name, boolean isPublic)
	{
		this.name = name;
		this.isPublic = isPublic;
	}

	/**
	 * Add a user to the room
	 * 
	 * @param newUser The new users
	 * @return The time stamp of the last message when the user entered the room 
	 * @throws UserAlreadyInRoom thrown if the user is already in the room
	 * @throws UserNotAllowed thrown if the user is in the blacklist or if it's not in the whitelist 
	 * @throws EncryptionException 
	 */
	public long addUser(User newUser) throws UserAlreadyInRoom, UserNotAllowed, EncryptionException
	{
		//Is the user already in the room?
		if(users.containsKey(newUser.getName()))
		{
			throw new UserAlreadyInRoom(newUser.getName(), name);
		}
		
		boolean inList = allowSet.contains(newUser.getName());
		
		//If public, is the user in the blacklist?
		if(isPublic && inList)
		{
			throw new UserNotAllowed(newUser, name);
		}
		
		//If private, is the user in the whitelist?
		else if(!isPublic && !inList)
		{
			throw new UserNotAllowed(newUser, name);
		}
		
		users.put(newUser.getName(), newUser);
		postSystemMessage(newUser.getName() + " has joined the room");
		
		return lastCount;
	}
	
	/**
	 * Add a user to the blacklist/whitelist.
	 * 
	 * @param user
	 */
	public void addUserToAllowList(User user)
	{
		if(!allowSet.contains(user.getName()))
		{
			allowSet.add(user.getName());
		}
	}
	
	/**
	 * Get the state of the room. This is used to poll the room for messages and to see which users are in the room.
	 * @param user 
	 * 
	 * @param fromCount only return messages with a time stamp that is higher than this
	 * @return
	 * @throws UserNotInRoom 
	 */
	public RoomState getState(User user, long fromCount) throws UserNotInRoom
	{
		//Is the user in this room?
		if(!users.containsKey(user.getName()))
		{
			throw new UserNotInRoom();
		}
		
		ArrayList<Message> messages = new ArrayList<Message>();
		
		//Is the lastCount older than fromCount? This would probably be a bug on the client, but it does not concern the server so it's ignored.   
		if(fromCount < lastCount)
		{
			ListIterator<Message> iter = buffer.listIterator(buffer.size() - 1);
			Message msg;
			
			//Step back to the message before the last to be included in the state
			while(iter.hasPrevious())
			{
				msg = iter.previous();
				
				if(msg.getCounter() == fromCount)
				{
					//Due to a weird Java quirk there must be a next here for the second loop to work.
					iter.next();
					break;
				}
			}
					
			//Add all messages from here to the end to the state to to the return list.
			while(iter.hasNext())
			{
				msg = iter.next();
				messages.add(msg);
			}
		}
		
		//Copy the user list
		ArrayList<User> currentUsers = new ArrayList<User>(users.values());
		
		return new RoomState(messages, currentUsers);
	}

	/**
	 * Returns true if the room is public and false if private.
	 * 
	 * @return
	 */
	public boolean isPublic()
	{
		return isPublic;
	}
	
	/**
	 * Post a message to the room.
	 * 
	 * @param fromUser The user that sent the message
	 * @param msg The message
	 * @throws UserNotInRoom Thrown if no such user is in the room
	 * @throws EncryptionException 
	 */
	public void postMessage(User fromUser, Message msg) throws UserNotInRoom, EncryptionException
	{
		//Is the user in the room?
		if(!users.containsKey(fromUser.getName()))
		{
			throw new UserNotInRoom(fromUser.getName(), name);
		}
		
		msg.setFromUser(fromUser);
		msg.setCount(getNextCount());
		
		send(msg);
	}
	
	/**
	 * Remove a user from the room.
	 * 
	 * @param user
	 * @throws UserNotInRoom Thrown if no such user is in the room
	 * @throws DecryptionException 
	 * @throws EncryptionException 
	 */
	public void removeUser(User user) throws UserNotInRoom, EncryptionException
	{
		//Is the user in the room?
		if(users.containsKey(user.getName()))
		{
			users.remove(user.getName());
			postSystemMessage(user.getName() + " has left the room");
		} else
		{
			throw new UserNotInRoom(user.getName(), name);
		}
	}
	
	/**
	 * Remove a user from the blacklist/whitelist.
	 * 
	 * @param user
	 * @throws NotInAllowList Thrown if the user is not in the blacklist/whitelist
	 */
	public void removeUserFromAllowList(User user) throws NotInAllowList
	{
		if(allowSet.contains(user.getName()))
		{
			allowSet.remove(user.getName());
		}
		else
		{
			throw new NotInAllowList(user.getName(), name);
		}
	}
	
	/**
	 * Returns true if the user is in the blacklist/whitelist.
	 * 
	 * @param user
	 * @return
	 */
	public boolean isInAllowList(User user) {
		return allowSet.contains(user.getName());
	}
	
	/**
	 * Returns true if the user is in the room.
	 * 
	 * @param name
	 * @return
	 */
	public boolean isInRoom(String name) {
		return users.containsKey(name);
	}

	/**
	 * Returns the name of the room.
	 * 
	 * @return
	 */
	public String getRoomName() {
		return name;
	}
	
	/**
	 * Post a system message to the room.
	 * 
	 * @param message
	 * @throws EncryptionException 
	 */
	public void postSystemMessage(String message) throws EncryptionException {
		send(createMessage(SYSTEM_USER, message));
	}

	/**
	 * Create a new message time stamp.
	 * 
	 * @return
	 */
	private long getNextCount() {
		return counter++;
	}
	
	/**
	 * Create a Message object for the message.
	 * 
	 * @param fromUser
	 * @param message
	 * @return
	 * @throws DecryptionException 
	 * @throws EncryptionException 
	 */
	private Message createMessage(User fromUser, String message) throws EncryptionException {
		Message msg = new Message(message);
		msg.setCount(getNextCount());
		msg.setFromUser(fromUser);
		
		return msg;
	}

	/**
	 * Add a message to the message buffer.
	 * 
	 * @param newMsg
	 */
	private void send(Message newMsg) {
		//If the buffer is full then remove the oldest message.
		if(buffer.size() >= BUFFER_SIZE)
		{
			buffer.removeFirst();
		}
		
		buffer.addLast(newMsg);
		lastCount = newMsg.getCounter();
	}
}
