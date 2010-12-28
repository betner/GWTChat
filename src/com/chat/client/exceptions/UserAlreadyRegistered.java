package com.chat.client.exceptions;

/**
 * Thrown when trying to register a user that already exists.
 */
public class UserAlreadyRegistered extends Exception {
	private static final long serialVersionUID = -2591956749982408283L;

	public UserAlreadyRegistered() {}
	
	public UserAlreadyRegistered(String userName)
	{
		super("User " + userName + " is already registered!");
	}
}
