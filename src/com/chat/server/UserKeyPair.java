package com.chat.server;

import com.chat.client.User;

public class UserKeyPair {
	private User user;
	private SessionKey key;

	public UserKeyPair(User user, SessionKey key) {
		this.user = user;
		this.key = key;
	}

	public User getUser() {
		return user;
	}

	public SessionKey getKey() {
		return key;
	}
}
