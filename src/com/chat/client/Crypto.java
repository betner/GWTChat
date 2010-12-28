package com.chat.client;

/**
 * Base class for ciphers.
 */
public abstract class Crypto {
	/**
	 * Encryption key.
	 */
	protected String key;
	
	public Crypto(String key) {
		this.key = key;
	}
	
	/**
	 * Decrypt string.
	 * 
	 * @param encryptedText
	 * @return
	 */
	abstract public String decrypt(String encryptedText);
	
	/**
	 * Encrypt string.
	 * 
	 * @param clearText
	 * @return
	 */
	abstract public String encrypt(String clearText);
}
