package com.chat.server;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Server representation of a session key.
 */
public class SessionKey {
	/**
	 * Pseudo random number generator used to generate a random key.
	 */
	static private SecureRandom random = new SecureRandom();
	
	/**
	 * String representation of a key.
	 */
	private String key;
	
	/**
	 * Create a new random session key.
	 */
	public SessionKey()
	{
		key = new BigInteger(130, random).toString(32);
	}
	
	/**
	 * Create a session key from a string representation.
	 * 
	 * @param key
	 */
	public SessionKey(String key)
	{
		this.key = key;
	}
	
	/**
	 * Returns a string representation of the key.
	 * 
	 * @return
	 */
	public String getKey()
	{
		return key;
	}
	
	@Override
	public boolean equals(Object o)
	{
		//check for self-comparison
	    if ( this == o ) return true;

	    if ( !(o instanceof SessionKey) ) return false;
	    
	    SessionKey s = (SessionKey)o;

	    return key.equals(s.key);
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
