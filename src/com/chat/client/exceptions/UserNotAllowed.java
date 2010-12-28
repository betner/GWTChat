package com.chat.client.exceptions;

import com.chat.client.User;

/**
 * Thrown when a user is blocked by a blacklist/whitelist.
 */
public class UserNotAllowed extends Exception {
	private static final long serialVersionUID = 817039623468746128L;
	
	public UserNotAllowed() {}

	public UserNotAllowed(User user, String roomName)
	{
		super("User " + user.getName() + " is not allowed to enter the room " + roomName + "!"); 
	}
}
