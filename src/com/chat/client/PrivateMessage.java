package com.chat.client;

import java.io.Serializable;

import com.chat.client.exceptions.DecryptionException;
import com.chat.client.exceptions.EncryptionException;

/**
 * A private message. 
 */
public class PrivateMessage implements Serializable {
	private static final long serialVersionUID = 8461953151967222474L;
	
	/**
	 * The user that sent the emssage.
	 */
	private User fromUser;
	
	/**
	 * The content of the message.
	 */
	private String messageContent;
	
	/**
	 * Default constructor to make PrivateMesage serializable.
	 * Do not use unless you know what you are doing. 
	 */
	public PrivateMessage() {}

	/**
	 * Use this to create new PrivateMessages.
	 * 
	 * @param msg
	 * @throws EncryptionException 
	 */
	public PrivateMessage(String msg, Crypto cipher) throws EncryptionException {

		messageContent = cipher.encrypt(msg);

	}
	
	public void setFromUser(User fromUser)
	{
		this.fromUser = fromUser;
	}
	
	/**
	 * Returns the content of the message.
	 * 
	 * @return
	 * @throws DecryptionException 
	 */
	public String getMessageContent(Crypto cipher) throws DecryptionException {
		return cipher.decrypt(messageContent);
	}

	/**
	 * Returns the user that sent the message.
	 * 
	 * @return
	 */
	public User getFromUser()
	{
		return fromUser;
	}
}
