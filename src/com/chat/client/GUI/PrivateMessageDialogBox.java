package com.chat.client.GUI;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A messagebox for sending private messages in the chat system
 * @author 
 *
 */
public class PrivateMessageDialogBox extends DialogBox {

    private VerticalPanel mainPanel;
    private HorizontalPanel inputPanel;
    private HorizontalPanel buttonPanel;
    private Button sendBtn;
    private Button cancelBtn;
    private TextBox messageBox;
    private String recipient;
        
    public PrivateMessageDialogBox() {
	mainPanel   = new VerticalPanel();
	inputPanel  = new HorizontalPanel();
	buttonPanel = new HorizontalPanel();
	sendBtn     = new Button("Send");
	cancelBtn   = new Button("Cancel");
	messageBox  = new TextBox();
		
	inputPanel.add(messageBox);
	buttonPanel.add(sendBtn);
	buttonPanel.add(cancelBtn);
	
	mainPanel.add(inputPanel);
	mainPanel.add(buttonPanel);
	
	this.setWidget(mainPanel);
	this.setText("Send private message");
	this.setModal(true);
	messageBox.setFocus(true);
	
	messageBox.addKeyPressHandler(new KeyPressHandler() {
	    public void onKeyPress(KeyPressEvent event) {
		if (event.getCharCode() == KeyCodes.KEY_ENTER) {
		    sendBtn.click();
		}
	    }
	});
    }
    
    public void show() {
	super.show();
	messageBox.setFocus(true);
    }
    
    public void setSendClickHandler(ClickHandler handler) {
	sendBtn.addClickHandler(handler);
    }
    
    public void setCancelClickHandler(ClickHandler handler) {
	cancelBtn.addClickHandler(handler);
    }
    
    public void clearInput() {
	messageBox.setText("");
    }
    
    public String getMessage() {
	return messageBox.getText();
    }
    
    public void setRecipient(String name) {
	recipient = name;
    }
    
    public String getRecipient() {
	return recipient;
    }
}
