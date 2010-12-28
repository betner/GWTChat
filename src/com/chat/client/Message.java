package com.chat.client;

import java.io.Serializable;

import com.chat.client.exceptions.DecryptionException;
import com.chat.client.exceptions.EncryptionException;

/**
 * A normal message. 
 */
public class Message implements Serializable {
	private static final long serialVersionUID = -5451752911074729327L;
	/**
	 * The message's time stamp.
	 */
	private long counter;
	
	/**
	 * The content of the message.
	 */
	private String messageContent;
	
	/**
	 * The user that sent the message.
	 */
	private User user;
	
	/**
	 * Is the message encrypted?
	 */
	private boolean encrypted;
	
	/**
	 * Default constructor to make Message serializable.
	 * Do not use unless you know what you are doing. 
	 */
	public Message() {
		counter = -1;
	}
	
	/**
	 * Use this to create an encrypted messages.
	 * 
	 * @param user
	 * @param content
	 * @param counter
	 * @throws DecryptionException 
	 * @throws EncryptionException 
	 */
	public Message(String content, Crypto cipher) throws EncryptionException {
		System.out.println(content);
		messageContent = cipher.encrypt(content);
		encrypted = true;
	}
	
	/**
	 * Use this to create an unencrypted messages.
	 * 
	 * @param message
	 */
	public Message(String message) {
		messageContent = message;
		encrypted = false;
	}

	/**
	 * Returns the message's time stamp.
	 * 
	 * @return
	 */
	public long getCounter() {
		return counter;
	}
	
	/**
	 * Decrypts the content of the message.
	 * 
	 * @return
	 * @throws DecryptionException 
	 */
	public String getMessageContent(Crypto cipher) throws DecryptionException {
			return cipher.decrypt(messageContent);
	}
	
	/**
	 * Returns the user that sent the  message.
	 * 
	 * @return
	 */
	public User getUser()
	{
		return user;
	}

	/**
	 * Add the sender to the message.
	 * 
	 * @param from
	 */
	public void setFromUser(User from) {
		this.user = from;
	}

	/**
	 * Set the message time stamp.
	 * 
	 * @param count
	 */
	public void setCount(long count) {
		this.counter = count;
	}
	
	/**
	 * Is the message encrypted?
	 * 
	 * @return
	 */
	public boolean isEncrypted()
	{
		return encrypted;
	}

	/**
	 * Get the message without decrypting it.
	 * 
	 * @return
	 */
	public String getMessageContent() {
		return messageContent;
	}
}
