package com.chat.client.exceptions;

/**
 * Thrown when a user can't be found in a room's blacklist/whitelist.
 */
public class NotInAllowList extends Exception {
	private static final long serialVersionUID = -6713423212218089391L;

	public NotInAllowList() {}
	
	public NotInAllowList(String userName, String roomName) {
		super(userName + " is not in the allow list for room " + roomName);
	}
}
