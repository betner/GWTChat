package com.chat.client.exceptions;

/**
 * Thrown when a user that does not exist is requested.
 */
public class NoSuchUser extends Exception {
	private static final long serialVersionUID = -7185386644772884688L;

	public NoSuchUser() {}
	
	public NoSuchUser(String user) {
		super("Can't find user " + user + "!");
	}

}
