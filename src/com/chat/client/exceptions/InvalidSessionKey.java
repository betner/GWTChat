package com.chat.client.exceptions;

public class InvalidSessionKey extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8234177875626020736L;

	public InvalidSessionKey() {}
	
	public InvalidSessionKey(String key) {
		super(key + " is not a valid session key!");
	}

}
