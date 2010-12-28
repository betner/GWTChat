package com.chat.client.exceptions;

public class DecryptionException extends Exception {
	private static final long serialVersionUID = -6253926965654476559L;
	
	public DecryptionException() {
	}
	
	public DecryptionException(Exception e) {
		super(e);
	}
}
