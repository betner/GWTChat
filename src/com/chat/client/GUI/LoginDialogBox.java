package com.chat.client.GUI;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoginDialogBox extends DialogBox 
{
    private VerticalPanel mainPanel;
    private HorizontalPanel inputPanel;
    private HorizontalPanel buttonPanel;
    private Button loginBtn;
    private Button registerBtn;
    private TextBox nameBox;
    private PasswordTextBox passBox;
    
    public LoginDialogBox(/*LoginHandler*/)
    {
	mainPanel   = new VerticalPanel();
	inputPanel  = new HorizontalPanel();
	buttonPanel = new HorizontalPanel();
	loginBtn    = new Button("Login");
	registerBtn = new Button("Cancel");
	nameBox     = new TextBox();
	passBox     = new PasswordTextBox();
		
	inputPanel.add(nameBox);
	inputPanel.add(passBox);
	buttonPanel.add(loginBtn);
	buttonPanel.add(registerBtn);
	
	mainPanel.add(inputPanel);
	mainPanel.add(buttonPanel);
	
	this.setWidget(mainPanel);
	this.setText("Log in with user account");
	this.setModal(true);
	nameBox.setFocus(true);
	
	passBox.addKeyPressHandler(new KeyPressHandler() {

	    public void onKeyPress(KeyPressEvent event) {
		if (event.getCharCode() == KeyCodes.KEY_ENTER) {
		    loginBtn.click();
		}
	    }
	});
    }
    
    // Make sure the name input box gets focus
    public void show() {
	super.show();
	nameBox.setFocus(true);
    }
    
    public String getUser() {
	return nameBox.getText();
    }
    
    public String getPass() {
	return passBox.getText();
    }
    
    public void setLoginClickHandler(ClickHandler handler) {
	loginBtn.addClickHandler(handler);
    }
    
    public void setRegisterClickHandler(ClickHandler handler) {
	registerBtn.addClickHandler(handler);
    }
    
    public void clearInput() {
	nameBox.setText("");
	passBox.setText("");
    }
}
