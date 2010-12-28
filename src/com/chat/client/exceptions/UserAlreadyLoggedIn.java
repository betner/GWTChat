package com.chat.client.exceptions;

/**
 * Thrown when a user that is already logged in tries to log in again.
 */
public class UserAlreadyLoggedIn extends Exception {
	private static final long serialVersionUID = 7574764662862822948L;

	public UserAlreadyLoggedIn() {}
	
	public UserAlreadyLoggedIn(String userName) {
		super(userName + " is already logged in!");
	}

}
