package com.chat.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServerInterfaceAsync {

	void allowUser(String key, String user, String room,
			AsyncCallback<Void> callback);

	void createRoom(String key, String roomName, boolean isPublic,
			AsyncCallback<Void> callback);

	void getLoggedInUsers(String key, AsyncCallback<User[]> callback);

	void getRooms(String key, AsyncCallback<String[]> callback);

	void leaveRoom(String key, String user, String roomName,
			AsyncCallback<Void> callback);

	void login(String user, String password, AsyncCallback<LoginToken> callback);

	void logout(String key, String userName, AsyncCallback<Void> callback);

	void poll(String key, String roomName, long lastPost,
			AsyncCallback<RoomState> callback);

	void pollPrivateMessages(String key,
			AsyncCallback<PrivateMessage[]> callback);

	void post(String key, String roomName, Message msg,
			AsyncCallback<Void> callback);

	void registerPrivilegedUser(String key, String userName, String password,
			UserType type, AsyncCallback<Void> callback);

	void registerUser(String userName, String password,
			AsyncCallback<Void> callback);

	void banUser(String key, String user, String room,
			AsyncCallback<Void> callback);

	void removeUser(String key, String userName, AsyncCallback<Void> callback);

	void sendPrivateMessage(String key, String toUser, PrivateMessage message,
			AsyncCallback<Void> callback);

	void removeRoom(String key, String roomName, AsyncCallback<Void> callback);

	void enterRoom(String key, String roomName, AsyncCallback<Long> callback);
}
