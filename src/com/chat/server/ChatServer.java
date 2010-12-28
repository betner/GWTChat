package com.chat.server;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.chat.client.LoginToken;
import com.chat.client.Message;
import com.chat.client.PrivateMessage;
import com.chat.client.RoomState;
import com.chat.client.ServerInterface;
import com.chat.client.User;
import com.chat.client.UserType;
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
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Chat server implementation. 
 */
public class ChatServer extends RemoteServiceServlet implements ServerInterface {
	private static final long serialVersionUID = 3686488780545578102L;

	/**
	 * Salt used when hashing. This must be unique for every deplyed server.
	 * Generate this with a preprocessor or read it from a file.
	 */
	final static private String SALT = "4RfVjY6FnCdE3A";
	
	/**
	 * Delay between user garbage collection.
	 */
	final static private long COPY_INTERVAL = 60000;
	
	/**
	 * Database file.
	 */
	final static private String DB_FILE = "database.db";

	/**
	 * Default administrator name. Only used for testing purposes. This MUST be removed before deployment.
	 */
	final static private String ADMIN_NAME = "Admin";
	
	/**
	 * Default administrator password. Only used for testing purposes. This MUST be removed before deployment.
	 */
	final static private String ADMIN_PASSWORD = "Admin";
	
	/**
	 * Database.
	 */
	private Database db;
	
	/**
	 * The time of the last garbage collection.
	 */
	private long lastCopyTime;

	/**
	 * Queue for private messages.
	 */
	private HashMap<String, Queue<PrivateMessage>> messageQueue = new HashMap<String, Queue<PrivateMessage>>();
	
	/**
	 * List of rooms.
	 */
	private HashMap<String, ChatRoom> rooms = new HashMap<String, ChatRoom>();
	
	/**
	 * Mapping from user name to session key. Used to find a user's session key.
	 */
	private HashMap<String, SessionKey> nameToKey = new HashMap<String, SessionKey>();
	
	/**
	 * List of active users. A user is active if it has performed an action within the last COPY_INTERVAL milliseconds.
	 */
	private HashMap<SessionKey, User> activeList = new HashMap<SessionKey, User>();
	
	/**
	 * List of inactive users. A user is inactive if it has not performed an action within the last COPY_INTERVAL milliseconds.
	 */
	private HashMap<SessionKey, User> inactiveList = new HashMap<SessionKey, User>();

	/**
	 * Default constructor.
	 */
	public ChatServer() {
		//Replace this with false before deployment.
	    this(true);
	}
	
	/**
	 * Server constructor. If newDB is true the database will be reseted.
	 * 
	 * @param newDB
	 */
	public ChatServer(boolean newDB) {
		//Create a database connection.
		db = new SQLiteDatabase(DB_FILE, newDB);
		
		//Set the lastCopyTime to the current time.
		lastCopyTime = Calendar.getInstance().getTimeInMillis();

		//Create initial users.
		addInitalUsers();
		
		//Fetch a list of all existing rooms from the database. 
		List<RoomData> roomsToCreate = db.getRoomData();

		//Add rooms to the server.
		for (RoomData r : roomsToCreate) {
			createRoomFromDB(r);
		}
	}

	public void allowUser(String stringKey, String userName,
			String roomName) throws InvalidSessionKey, NoSuchRoom, NoSuchUser,
			InsufficientPrivilege {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		ChatRoom room = rooms.get(roomName);

		//Does the room exist?
		if (room == null) {
			throw new NoSuchRoom(roomName);
		}

		User user = db.getUser(userName);

		//Does this user exist?
		if (user == null) {
			throw new NoSuchUser(userName);
		}

		//Is the user allowed to do this?
		if (initiator.getType().ordinal() >= UserType.Moderator.ordinal()) {
			throw new InsufficientPrivilege();
		}

		if(!room.isPublic())
		{
			//Add the user to the whitelist.
			room.addUserToAllowList(user);
			db.addToAllowList(userName, roomName);
		}else if(room.isInAllowList(user))
		{
			//Remove the user from the blacklist.
			try {
				room.removeUserFromAllowList(user);
			} catch (NotInAllowList e) {
				//Programming error!
				throw new Error(e);
			}
			db.removeFromAllowList(userName, roomName);
		}
	}

	public void createRoom(String stringKey, String roomName, boolean isPublic)
			throws InvalidSessionKey, InsufficientPrivilege, RoomAlreadyExists {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		//Is the user allowed to do this?
		if (initiator.getType() != UserType.Administrator
				&& initiator.getType() != UserType.Moderator) {
			throw new InsufficientPrivilege();
		}

		//Does the room already exist?
		if (rooms.containsKey(roomName)) {
			throw new RoomAlreadyExists(roomName);
		}

		ChatRoom newRoom = new ChatRoom(roomName, isPublic);

		db.addRoom(roomName, isPublic);
		rooms.put(roomName, newRoom);
	}

	public long enterRoom(String stringKey, String roomName)
			throws InvalidSessionKey, NoSuchRoom, UserAlreadyInRoom,
			UserNotAllowed, EncryptionException {
		SessionKey key = new SessionKey(stringKey);

		User user = getLoggedInUser(key);

		//Is the session key valid?
		if (user == null) {
			throw new InvalidSessionKey(stringKey);
		}

		ChatRoom room = rooms.get(roomName);

		//Does the room exist?
		if (room == null) {
			throw new NoSuchRoom(roomName);
		}

		return room.addUser(user);
	}

	public User[] getLoggedInUsers(String stringKey) throws InvalidSessionKey {
		User initiator = getLoggedInUser(new SessionKey(stringKey));

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(stringKey);
		}

		//Create an array that can hold all active and inactive users.
		User[] userArr = new User[activeList.size() + inactiveList.size()];
		int i = 0;

		//Add all active uesrs to the array.
		for (User u : activeList.values()) {
			userArr[i++] = u;
		}

		//Add all inactive users to the array.
		for (User u : inactiveList.values()) {
			userArr[i++] = u;
		}

		//Sort the array.
		Arrays.sort(userArr);

		return userArr;
	}
	
	public String[] getRooms(String stringKey) throws InvalidSessionKey {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		//Extract and copy the room names from rooms.
		Set<String> nameSet = rooms.keySet();
		String[] roomNames = new String[nameSet.size()];
		nameSet.toArray(roomNames);

		return roomNames;
	}

	public void leaveRoom(String stringKey, String userName, String roomName)
			throws InvalidSessionKey, NoSuchUser, NoSuchRoom,
			InsufficientPrivilege, UserNotInRoom, EncryptionException {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		User toRemove = getLoggedInUser(userName);

		//Does this user exist?
		if (toRemove == null) {
			throw new NoSuchUser(userName);
		}

		ChatRoom room = rooms.get(roomName);

		//Does the room exist?
		if (room == null) {
			throw new NoSuchRoom(roomName);
		}

		//Is the user allowed to do this?
		if (initiator != toRemove
				&& (initiator.getType().ordinal() <= toRemove.getType()
						.ordinal())) {
			throw new InsufficientPrivilege();
		}

		room.removeUser(toRemove);
	}

	public LoginToken login(String userName, String password)
			throws UserAlreadyLoggedIn, AuthenticationFailure {
		User user = getLoggedInUser(userName);

		//Is the user already logged in?
		if (user != null) {
			throw new UserAlreadyLoggedIn(userName);
		}

		//Does the user name and password match a valid user?
		if (!db.matchesUser(userName, createHash(userName, password))) {
			throw new AuthenticationFailure();
		}

		user = db.getUser(userName);
		SessionKey key = new SessionKey();

		activeList.put(key, user);
		nameToKey.put(user.getName(), key);

		return new LoginToken(key.getKey(), user.getType());
	}

	public void logout(String stringKey, String userName)
			throws InvalidSessionKey, NoSuchUser, InsufficientPrivilege {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		User toLogOut = getLoggedInUser(userName);

		//Does this user exist?
		if (toLogOut == null) {
			throw new NoSuchUser(userName);
		}

		//Is the user allowed to do this?
		if (initiator != toLogOut
				&& (initiator.getType().ordinal() > toLogOut.getType()
						.ordinal())) {
			throw new InsufficientPrivilege();
		}

		clearUserData(toLogOut);
		removeFromLists(key);
	}

	public RoomState poll(String stringKey, String roomName, long lastPost)
			throws InvalidSessionKey, NoSuchRoom, UserNotInRoom {
		SessionKey key = new SessionKey(stringKey);

		User user = getLoggedInUser(key);

		//Is the session key valid?
		if (user == null) {
			throw new InvalidSessionKey(stringKey);
		}

		ChatRoom room = rooms.get(roomName);

		//Does the room exist?
		if (room == null) {
			throw new NoSuchRoom(roomName);
		}

		return room.getState(user, lastPost);
	}

	public PrivateMessage[] pollPrivateMessages(String stringKey)
			throws InvalidSessionKey {
		SessionKey key = new SessionKey(stringKey);
		User user = getLoggedInUser(key);

		//Is the session key valid?
		if (user == null) {
			throw new InvalidSessionKey(stringKey);
		}

		return getMessagesFromQueue(user.getName());
	}

	public void post(String stringKey, String roomName, Message msg)
			throws InvalidSessionKey, NoSuchRoom, UserNotInRoom, EncryptionException {
		SessionKey key = new SessionKey(stringKey);

		User user = getLoggedInUser(key);

		//Is the session key valid?
		if (user == null) {
			throw new InvalidSessionKey(stringKey);
		}

		ChatRoom room = rooms.get(roomName);

		//Does the room exist?
		if (room == null) {
			throw new NoSuchRoom(roomName);
		}

		room.postMessage(user, msg);
	}

	public void registerPrivilegedUser(String stringKey, String userName,
			String password, UserType type) throws InsufficientPrivilege,
			UserAlreadyRegistered, InvalidSessionKey {
		SessionKey key = new SessionKey(stringKey);
		User initiatedBy = getLoggedInUser(key);

		//Is the session key valid?
		if (initiatedBy == null) {
			throw new InvalidSessionKey(stringKey);
		}

		//Is the user allowed to do this?
		if (initiatedBy.getType().ordinal() < UserType.Administrator.ordinal()
				|| initiatedBy.getType().ordinal() <= type.ordinal()) {
			throw new InsufficientPrivilege();
		}

		//Does a user with this name already exist?
		if (db.userExists(userName)) {
			throw new UserAlreadyRegistered(userName);
		}

		String hash = createHash(userName, password);
		User newUser = new User(userName, type);

		db.addUser(newUser, hash);
	}

	public void registerUser(String userName, String password)
			throws UserAlreadyRegistered {
		//Does a user with this name already exist?
		if (db.userExists(userName)) {
			throw new UserAlreadyRegistered(userName);
		}

		String hash = createHash(userName, password);
		User newUser = new User(userName, UserType.User);

		db.addUser(newUser, hash);
	}

	public void banUser(String stringKey, String userName,
			String roomName) throws InvalidSessionKey, NoSuchRoom, NoSuchUser,
			InsufficientPrivilege, NotInAllowList, EncryptionException {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		ChatRoom room = rooms.get(roomName);

		//Does the room exist?
		if (room == null) {
			throw new NoSuchRoom(roomName);
		}

		User user = db.getUser(userName);

		//Does this user exist?
		if (user == null) {
			throw new NoSuchUser(userName);
		}

		//Is the user allowed to do this?
		if (initiator.getType().ordinal() <= user.getType().ordinal()) {
			throw new InsufficientPrivilege();
		}

		if(room.isPublic())
		{
			//Add the user to the blacklist.
			room.addUserToAllowList(user);
			db.addToAllowList(userName, roomName);
		}else if(room.isInAllowList(user))
		{
			//Remove the user from the whitelist.
			try {
				room.removeUserFromAllowList(user);
			} catch (NotInAllowList e) {
				//Programming error!
				throw new Error(e);
			}
			db.removeFromAllowList(userName, roomName);
		}

		// Check if user must be removed from room
		if (room.isInRoom(userName)) {
			try {
				room.removeUser(user);
			} catch (UserNotInRoom e) {
				// Programming error. Should not happen
				throw new Error(e);
			}
		}
	}

	public void removeRoom(String stringKey, String roomName)
			throws InvalidSessionKey, InsufficientPrivilege, NoSuchRoom {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		//Is the user allowed to do this?
		if (initiator.getType() != UserType.Administrator
				&& initiator.getType() != UserType.Moderator) {
			throw new InsufficientPrivilege();
		}

		//Does the room exist?
		if (!rooms.containsKey(roomName)) {
			throw new NoSuchRoom(roomName);
		}

		// Not the best way to do this. This will cause problems for the users
		// in the room, but without callbacks there isn't much else that can be
		// done.
		rooms.remove(roomName);

		db.removeRoom(roomName);
	}

	public void removeUser(String stringKey, String userName)
			throws InvalidSessionKey, NoSuchUser, InsufficientPrivilege {
		SessionKey key = new SessionKey(stringKey);
		User initiator = getLoggedInUser(key);

		//Is the session key valid?
		if (initiator == null) {
			throw new InvalidSessionKey(key.getKey());
		}

		User toRemove = db.getUser(userName);

		//Does this user exist?
		if (toRemove == null) {
			throw new NoSuchUser(userName);
		}

		//Is the user allowed to do this?
		if (initiator != toRemove
				&& (initiator.getType().ordinal() <= toRemove.getType()
						.ordinal())) {
			throw new InsufficientPrivilege();
		}

		//If the user is logged in then log him out.
		if (getLoggedInUser(userName) != null) {
			logout(stringKey, userName);
		}

		removeUserFromAllowLists(toRemove);
		db.removeUser(userName);
	}

	public void sendPrivateMessage(String stringKey, String toUser,
			PrivateMessage message) throws InvalidSessionKey, NoSuchUser, EncryptionException {
		SessionKey key = new SessionKey(stringKey);

		User from = getLoggedInUser(key);

		//Is the session key valid?
		if (from == null) {
			throw new InvalidSessionKey(stringKey);
		}

		User to = getLoggedInUser(toUser);

		//Does this user exist?
		if (to == null) {
			throw new NoSuchUser(toUser);
		}

		message.setFromUser(from);
		
		addMessageToQueue(toUser, message);
	}

	/**
	 * Create some initial users. This is used for testing.
	 */
	private void addInitalUsers() {
		if (!db.userExists("Admin")) {
			User admin = new User(ADMIN_NAME, UserType.Administrator);
			db.addUser(admin, createHash(admin.getName(), ADMIN_PASSWORD));
		}
	}

	/**
	 * Add a private message to a user's message queue.
	 * 
	 * @param toUser
	 * @param privateMessage
	 */
	private void addMessageToQueue(String toUser, PrivateMessage privateMessage) {
		//Does this user already have a queue.
		if (!messageQueue.containsKey(toUser)) {
			//Create a new queue for this user.
			messageQueue.put(toUser, new LinkedList<PrivateMessage>());
		}

		Queue<PrivateMessage> queue = messageQueue.get(toUser);
		queue.add(privateMessage);
	}
	
	/**
	 * Remove a user from rooms and lookup lists.
	 * 
	 * @param toLogOut
	 */
	private void clearUserData(User toLogOut) {
		// Remove user's message queue
		messageQueue.remove(toLogOut.getName());

		// Remove the user from all rooms
		for (ChatRoom room : rooms.values()) {
			if (room.isInRoom(toLogOut.getName())) {
				try {
					room.removeUser(toLogOut);
				} catch (UserNotInRoom e) {
					// Programming error. Should not happen
					throw new Error(e);
				} catch (EncryptionException e) {
					throw new Error(e);
				}
			}
		}
		nameToKey.remove(toLogOut.getName());
	}
	
	/**
	 * Garbage collect inactive users. Using a modified Stop-and-Copy algorithm.
	 */
	private void copyActiveUsers() {
		//Log out all inactive users.
		for (User u : inactiveList.values()) {
			clearUserData(u);
		}
		
		//Clear the list of inactive users.
		inactiveList.clear();

		//Swap the active and inactive user list.
		HashMap<SessionKey, User> tmp = inactiveList;
		inactiveList = activeList;
		activeList = tmp;

		//Update the lastCopyTime time.
		lastCopyTime = Calendar.getInstance().getTimeInMillis();
	}
	
	/**
	 * Create a password hash.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	private String createHash(String userName, String password) {
		String hash = "";

		try {
			//Create a hash from the concatenation of the user's name, password and a salt.
			hash = AeSimpleSHA1.SHA1(userName + password + SALT);
		} catch (NoSuchAlgorithmException e) {
			// Programming error. Should not happen.
			throw new Error(e);
		} catch (UnsupportedEncodingException e) {
			// Programming error. Should not happen.
			throw new Error(e);
		}
		return hash;
	}
	
	/**
	 * Recreate a room from database data.
	 * 
	 * @param r
	 */
	private void createRoomFromDB(RoomData r) {
		//Create the room
		ChatRoom newRoom = new ChatRoom(r.getName(), r.isPublic());
		
		//Fetch the blacklist/whitelist from the database.
		List<User> allowList = db.getAllowList(r.getName());

		//Add the users in the list from the database to the room's list.
		for (User u : allowList) {
			newRoom.addUserToAllowList(u);
		}

		//Add the room to the server.
		rooms.put(r.getName(), newRoom);
	}
	
	/**
	 * Returns the logged in user that has this session key, or null if the key is invalid.
	 * 
	 * @param key
	 * @return
	 */
	private User getLoggedInUser(SessionKey key) {
		// Is it time to do a garbage collection?
		if (lastCopyTime + COPY_INTERVAL < Calendar.getInstance()
				.getTimeInMillis()) {
			copyActiveUsers();
		}
		
		//Make sure that the client haven't sent a bad key.
		if(key == null || key.getKey() == null)
		{
		    return null;
		}

		User user = activeList.get(key);

		//Is the user active?
		if (user != null) {
			return user;
		}

		user = inactiveList.get(key);

		//Is the user inactive?
		if (user != null) {
			//Make the user active.
			inactiveList.remove(key);
			activeList.put(key, user);

			return user;
		}

		//No user has this key.
		return null;
	}
	
	/**
	 * Returns the logged in user that has this name, or null if no such user exists.
	 * 
	 * @param userName
	 * @return
	 */
	private User getLoggedInUser(String userName) {
		//Lookup the session key for this user.
		SessionKey key = nameToKey.get(userName);

		if (key == null) {
			//No user with this name is logged in.
			return null;
		} else {
			//Use the key to find the user.
			return getLoggedInUser(key);
		}
	}
	
	/**
	 * Pop all messages from a user's private message queue.
	 * 
	 * @param name
	 * @return
	 */
	private PrivateMessage[] getMessagesFromQueue(String name) {
		PrivateMessage[] messages;

		//Fetch the user's queue.
		Queue<PrivateMessage> queue = messageQueue.get(name);

		if (queue == null) {
			//The user don't have a queue so there are no private messages.
			messages = new PrivateMessage[0];
		} else {
			int n = queue.size();
			messages = new PrivateMessage[n];

			//Pop the messages from the queue.
			for (int i = 0; i < n; ++i) {
				messages[i] = queue.poll();
			}
		}

		return messages;
	}
	
	/**
	 * Remove a logged in user.
	 * 
	 * @param key
	 */
	private void removeFromLists(SessionKey key) {
		activeList.remove(key);
		inactiveList.remove(key);
	}
	
	/**
	 * Remove a user from all blacklists/whitelists.
	 * 
	 * @param toRemove
	 */
	private void removeUserFromAllowLists(User toRemove) {
		for (ChatRoom room : rooms.values()) {
			if (room.isInAllowList(toRemove)) {
				try {
					room.removeUserFromAllowList(toRemove);
				} catch (NotInAllowList e) {
					// Programming error. Should not happen.
					throw new Error(e);
				}
			}
			db.removeFromAllowList(toRemove.getName(), room.getRoomName());
		}
	}
	
	@SuppressWarnings("unused")
	/**
	 * Print out all active and inactive users.
	 */
	private void printList() {
		System.out.println("===========================================");
		System.out.println("Printing Active keys");
		Set<SessionKey> keys = activeList.keySet();

		for (SessionKey s : keys) {
			System.out.println(s.getKey());
		}
		System.out.println("-------------------------------------------");
		System.out.println("Printing Inactive keys");
		keys = inactiveList.keySet();
		for (SessionKey s : keys) {
			System.out.println(s.getKey());
		}
		System.out.println("===========================================");
	}
}