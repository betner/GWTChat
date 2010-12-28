package com.chat.client.exceptions;

/**
 * Thrown when a user can't be found in a room.
 */
public class UserNotInRoom extends Exception {
	private static final long serialVersionUID = 5473825212731820158L;

	public UserNotInRoom() {}
	
	public UserNotInRoom(String userName, String roomName)
	{
		super(userName + " is not inside room " + roomName); 
	}
}
