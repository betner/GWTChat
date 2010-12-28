package com.chat.client.exceptions;

/**
 * Thrown when a user is trying to enter a room that he's already inside.
 */
public class UserAlreadyInRoom extends Exception {
	private static final long serialVersionUID = -974891461040184012L;

	public UserAlreadyInRoom() {}
	
	public UserAlreadyInRoom(String user, String roomName)
	{
		super(user + " is already in the room " + roomName); 
	}
}
