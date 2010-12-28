package com.chat.client.exceptions;

/**
 * Thrown when a user can't be successfully authenticated.
 */
public class AuthenticationFailure extends Exception {
	private static final long serialVersionUID = -2010454388911066971L;

	public AuthenticationFailure() {
		super("User name and password does not match a valid user!");
	}

}
