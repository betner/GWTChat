package com.chat.client;

import com.chat.client.exceptions.AuthenticationFailure;
import com.chat.client.exceptions.EncryptionException;
import com.chat.client.exceptions.InsufficientPrivilege;
import com.chat.client.exceptions.InvalidSessionKey;
import com.chat.client.exceptions.NoSuchRoom;
import com.chat.client.exceptions.NoSuchUser;
import com.chat.client.exceptions.NotInAllowList;
import com.chat.client.exceptions.RoomAlreadyExists;
import com.chat.client.exceptions.UserAlreadyInRoom;
import com.chat.client.exceptions.UserAlreadyLoggedIn;
import com.chat.client.exceptions.UserAlreadyRegistered;
import com.chat.client.exceptions.UserNotAllowed;
import com.chat.client.exceptions.UserNotInRoom;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * RPC interface to the server.
 */
@RemoteServiceRelativePath("chatServer")
public interface ServerInterface extends RemoteService{

	/**
	 * Allow a user to enter the room.
	 * 
	 * This can only be done by a user with at least moderator privileges.
	 * 
	 * @param key
	 * @param user
	 * @throws InvalidSessionKey
	 * @throws NoSuchRoom
	 * @throws NoSuchUser
	 * @throws InsufficientPrivilege
	 */
	public void allowUser(String key, String user, String room)
			throws InvalidSessionKey, NoSuchRoom, NoSuchUser,
			InsufficientPrivilege;

	/**
	 * Ban a user from the room.
	 * 
	 * This can only be done by a user with at least moderator privileges, and only if the user that shall be banned has a lower privilege level.
	 * 
	 * @param key
	 * @param user
	 * @throws InvalidSessionKey
	 * @throws NoSuchRoom
	 * @throws NoSuchUser
	 * @throws InsufficientPrivilege
	 * @throws NotInAllowList
	 * @throws EncryptionException 
	 */
	public void banUser(String key, String user, String room)
			throws InvalidSessionKey, NoSuchRoom, NoSuchUser,
			InsufficientPrivilege, NotInAllowList, EncryptionException;
	
	/**
	 * Create a new chat room.
	 * 
	 * This can only be done by a user with at least moderator privileges.
	 * 
	 * @param key
	 * @param roomName
	 * @throws InvalidSessionKey
	 * @throws InsufficientPrivilege
	 * @throws RoomAlreadyExists
	 */
	public void createRoom(String key, String roomName, boolean isPublic)
			throws InvalidSessionKey, InsufficientPrivilege, RoomAlreadyExists;
	
	/**
	 * Remove a room
	 * 
	 * This can only be done by a user with at least moderator privileges.
	 * 
	 * @param key
	 * @param roomName
	 * @throws InvalidSessionKey
	 * @throws InsufficientPrivilege
	 * @throws NoSuchRoom
	 */
	public void removeRoom(String key, String roomName) throws InvalidSessionKey, InsufficientPrivilege, NoSuchRoom;

	/**
	 * Enter a room.
	 * 
	 * @param key
	 * @param roomName
	 * @return
	 * @throws InvalidSessionKey
	 * @throws NoSuchRoom
	 * @throws UserAlreadyInRoom
	 * @throws UserNotAllowed
	 * @throws EncryptionException 
	 */
	public long enterRoom(String key, String roomName)
			throws InvalidSessionKey, NoSuchRoom, UserAlreadyInRoom,
			UserNotAllowed, EncryptionException;

	/**
	 * Get a list of all users logged in to the server.
	 * 
	 * @param key
	 * @return
	 * @throws InvalidSessionKey
	 */
	public User[] getLoggedInUsers(String key) throws InvalidSessionKey;

	/**
	 * Get a list of all rooms on the server.
	 * 
	 * @return
	 * @throws InvalidSessionKey 
	 */
	public String[] getRooms(String key) throws InvalidSessionKey;

	/**
	 * Remove a user from a room.
	 * 
	 * This can only be done by a user that has a higher privilege level than the user that is to be removed, or if the user is removing himself.
	 * 
	 * @param key
	 * @param user
	 * @param roomName
	 * @throws InvalidSessionKey
	 * @throws NoSuchUser
	 * @throws NoSuchRoom
	 * @throws InsufficientPrivilege
	 * @throws UserNotInRoom
	 * @throws EncryptionException 
	 */
	public void leaveRoom(String key, String user, String roomName)
			throws InvalidSessionKey, NoSuchUser, NoSuchRoom,
			InsufficientPrivilege, UserNotInRoom, EncryptionException;

	/**
	 * Login to the server.
	 * 
	 * @param user
	 * @param password
	 * @return
	 * @throws UserAlreadyLoggedIn
	 * @throws AuthenticationFailure
	 */
	public LoginToken login(String user, String password)
			throws UserAlreadyLoggedIn, AuthenticationFailure;

	/**
	 * Logout a user.
	 * 
	 * This can only be done by a user that has a higher privilege level than the user that is to be logged out, or if the user is login out himself.
	 * 
	 * @param key
	 * @param userName
	 * @throws InvalidSessionKey
	 * @throws NoSuchUser
	 * @throws InsufficientPrivilege
	 */
	public void logout(String key, String userName) throws InvalidSessionKey,
			NoSuchUser, InsufficientPrivilege;

	/**
	 * Poll the server for changes in a room.
	 * 
	 * This can only be done by a user that is in the room that is polled.
	 * 
	 * @param key
	 * @param roomName
	 * @param lastPost
	 * @return
	 * @throws InvalidSessionKey
	 * @throws NoSuchRoom
	 * @throws UserNotInRoom 
	 */
	public RoomState poll(String key, String roomName, long lastPost)
			throws InvalidSessionKey, NoSuchRoom, UserNotInRoom;

	/**
	 * Poll the server for new private messages.
	 * 
	 * @param key
	 * @return
	 * @throws InvalidSessionKey
	 */
	public PrivateMessage[] pollPrivateMessages(String key)
			throws InvalidSessionKey;

	/**
	 * Post a message to a room.
	 * 
	 * This can only be done by a user that is in the room.
	 * 
	 * @param key
	 * @param roomName
	 * @param msg
	 * @throws InvalidSessionKey
	 * @throws NoSuchRoom
	 * @throws UserNotInRoom
	 * @throws EncryptionException 
	 */
	public void post(String key, String roomName, Message msg)
			throws InvalidSessionKey, NoSuchRoom, UserNotInRoom, EncryptionException;

	/**
	 * Create a new user with a specified privilege level.
	 * 
	 * Can only be done by a user with at least administrator privileges and that has a higher privilege level than the user that is created..
	 * 
	 * @param key
	 * @param userName
	 * @param password
	 * @param type
	 * @throws InsufficientPrivilege
	 * @throws UserAlreadyRegistered
	 * @throws InvalidSessionKey 
	 */
	public void registerPrivilegedUser(String key, String userName,
			String password, UserType type) throws InsufficientPrivilege,
			UserAlreadyRegistered, InvalidSessionKey;

	/**
	 * Create a normal user.
	 * 
	 * @param userName
	 * @param password
	 * @throws UserAlreadyRegistered
	 */
	public void registerUser(String userName, String password)
			throws UserAlreadyRegistered;



	/**
	 * Permanently remove a user from the server.
	 * 
	 * This can only be done by a user with administrator privileges, and only if the user to be removed has a lower privilege level.
	 * 
	 * @param key
	 * @param userName
	 * @throws InvalidSessionKey
	 * @throws NoSuchUser
	 * @throws InsufficientPrivilege
	 */
	public void removeUser(String key, String userName)
			throws InvalidSessionKey, NoSuchUser, InsufficientPrivilege;

	/**
	 * Send a private message to a logged in user.
	 * 
	 * @param key
	 * @param toUser
	 * @param message
	 * @throws InvalidSessionKey
	 * @throws NoSuchUser
	 * @throws EncryptionException 
	 */
	public void sendPrivateMessage(String key, String toUser, PrivateMessage message)
			throws InvalidSessionKey, NoSuchUser, EncryptionException;
}
