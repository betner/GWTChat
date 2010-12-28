package com.chat.client.exceptions;

/**
 * Thrown when a room that does not exist is requested.
 */
public class NoSuchRoom extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -920764010099608263L;

	public NoSuchRoom() {}
	
	public NoSuchRoom(String roomName) {
		super(roomName + " does not exist!");
	}

}
