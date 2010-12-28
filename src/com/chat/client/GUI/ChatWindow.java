package com.chat.client.GUI;

import java.util.ArrayList;
import java.util.List;

import com.chat.client.Crypto;
import com.chat.client.Message;
import com.chat.client.PrivateMessage;
import com.chat.client.exceptions.DecryptionException;

import com.google.gwt.user.client.ui.TextArea;

public class ChatWindow extends TextArea {

    private List<Message> messages;
    
    public ChatWindow() {
	this.setText("*** GWT Chat system ***\n\n\n");
	this.setReadOnly(true);
    }
    
    /**
     * 
     * @param message
     */
    public void addMessage(Message message) {
	messages.add(message);
	//updateWindow();
    }
    
    /**
     * Update the window with new messages
     * @throws DecryptionException 
     */
    public void update(ArrayList<Message> messages, Crypto cipher) throws DecryptionException {
	
	StringBuffer sb = new StringBuffer();
	
	for (Message m : messages) {
		String clearText;
		if(m.isEncrypted())
		{
			clearText = m.getMessageContent(cipher);
		} else
		{
			clearText = m.getMessageContent();
		}
		
	    sb.append(m.getUser().getName() + ": " + clearText + "\n");
	}
	this.setText(this.getText() + sb.toString());
    }
    
    /**
     * Update the chat window with private messages.
     * We don't save private messages yet.
     * @param messages
     * @throws DecryptionException 
     */
    public void updateWithPrivMsg(PrivateMessage[] messages, Crypto cipher) throws DecryptionException {
	StringBuffer sb = new StringBuffer();
	
	for (PrivateMessage pm : messages) {
	    //addMessage(pm); WE DON'T SAVE PRIVATE MESSAGES YET
	    sb.append("# Message from " + pm.getFromUser().getName() + ": " + pm.getMessageContent(cipher) + "\n");
	}
	this.setText(this.getText() + sb.toString());
    }
}
