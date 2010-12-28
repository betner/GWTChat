package com.chat.client.exceptions;

/**
 * Thrown when a user performs an action that he's not allowed to do.
 */
public class InsufficientPrivilege extends Exception {
	private static final long serialVersionUID = -2621743464493715156L;

	public InsufficientPrivilege() {
	}
}
