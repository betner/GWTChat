package com.chat.client;

public class CryptoJS extends Crypto {
	public CryptoJS(String key) {
		super(key);
	}

	public String decrypt(String encryptedText)
	{
		String res = JSDecrypt(rm64(encryptedText), key);
	
		return res;
	}
	
	public String encrypt(String clearText)
	{
		String res = JSEncrypt(clearText, key);
		
		return res; 
	}
	
	/**
	 * Removes line breaks if they exist. This is to make the algorithm compatible with OpenSSL. This is not needed now but could be useful in the future.
	 * 
	 * @param clearText
	 * @return
	 */
	private String rm64(String clearText) {
		return clearText.replaceAll("[\n]+", "");
	}

	/**
	 * Use pidCrypt to decrypt a string.
	 * 
	 * @param encryptedText
	 * @param key
	 * @return
	 */
	private static native String JSDecrypt(String encryptedText, String key)
	/*-{
	    var aes = new $wnd.pidCrypt.AES.CBC();
	  
	   	return aes.decryptText(encryptedText, key, {nBits:256, A0_PAD:false});
	    
	}-*/;
	
	/**
	 * Use pidCrypt to encrypt a string.
	 * 
	 * @param clearText
	 * @param key
	 * @return
	 */
	private static native String JSEncrypt(String clearText, String key)
	/*-{
	    var aes = new $wnd.pidCrypt.AES.CBC();
	
	   	return aes.encryptText(clearText, key, {nBits: 256});
	    
	}-*/;
}
