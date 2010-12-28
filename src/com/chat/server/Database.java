package com.chat.server;

import java.util.List;

import com.chat.client.User;

/**
 * Database interface.
 */

public interface Database {
	/**
	 * Add a room to the database.
	 * 
	 * @param name
	 * @param isPublic
	 */
	public void addRoom(String name, boolean isPublic);
	
	/**
	 * Fetch data about a room.
	 * 
	 * @return
	 */
	public List<RoomData> getRoomData();
	
	/**
	 * Add a user to the database.
	 * 
	 * @param user
	 * @param passwordHash
	 */
	public void addUser(User user, String passwordHash);

	/**
	 * Add a user to a room's blacklist/whitelist.
	 * 
	 * @param user
	 * @param room
	 */
	public void addToAllowList(String user, String room);

	/**
	 * Remove a user from a room's blacklist/whitelist.
	 * 
	 * @param user
	 * @param room
	 */
	public void removeFromAllowList(String user, String room);
	
	/**
	 * Remove a room from the database.
	 * 
	 * @param name
	 */
	public void removeRoom(String name);
	
	/**
	 * Remove a user from the database.
	 * 
	 * @param name
	 */
	public void removeUser(String name);
	
	/**
	 * Fetch a user from the database.
	 * 
	 * @param name
	 * @return
	 */
	public User getUser(String name);
	
	/**
	 * Checks if a user name and password matches a valid user.
	 * 
	 * @param name
	 * @param passwordHash
	 * @return
	 */
	public boolean matchesUser(String name, String passwordHash);
	
	/**
	 * Checks if a user with this name exists.
	 * 
	 * @param name
	 * @return
	 */
	public boolean userExists(String name);
	
	/**
	 * Fetches a room's blacklist/whitelist.
	 * 
	 * @param roomName
	 * @return
	 */
	public List<User> getAllowList(String roomName);
}
