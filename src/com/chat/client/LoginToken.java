package com.chat.client;

import java.io.Serializable;

/**
 * Token returned by the server that holds a session key and user type for a newly logged in user.
 */

public class LoginToken implements Serializable {
	private static final long serialVersionUID = 7393922603563695867L;
	/**
	 * The user's session key.
	 */
	private String sessionKey;
	
	/**
	 * The user's type.
	 */
	private UserType userType;

	/**
	 * Default constructor to make LoginToken serializable.
	 * Do not use unless you know what you are doing. 
	 */
	public LoginToken() {}
	
	/**
	 * Use this to create new LoginTokens.
	 * 
	 * @param sessionKey
	 * @param userType
	 */
	public LoginToken(String sessionKey, UserType userType) {
		this.sessionKey = sessionKey;
		this.userType = userType;
	}

	/**
	 * Returns the user's session key.
	 * 
	 * @return
	 */
	public String getSessionKey() {
		return sessionKey;
	}

	/**
	 * Returns the user's type.
	 * 
	 * @return
	 */
	public UserType getUserType() {
		return userType;
	}
}