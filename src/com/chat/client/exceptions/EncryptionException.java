package com.chat.client.exceptions;

public class EncryptionException extends Exception {
	private static final long serialVersionUID = -647139649819877446L;
	
	public EncryptionException() {
	}
	
	public EncryptionException(Exception e)
	{
		super(e);
	}
}
