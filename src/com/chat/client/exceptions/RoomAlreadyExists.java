package com.chat.client.exceptions;

/**
 * Thrown when trying to create a room that already exists.
 */
public class RoomAlreadyExists extends Exception {
	private static final long serialVersionUID = -1949318572108057853L;

	public RoomAlreadyExists() {}
	
	public RoomAlreadyExists(String roomName) {
		super("A room with the name " + roomName + " already exists!");
	}

}
