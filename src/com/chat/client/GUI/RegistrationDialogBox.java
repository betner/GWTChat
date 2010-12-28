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

public class RegistrationDialogBox extends DialogBox {

    private VerticalPanel mainPanel;
    private HorizontalPanel inputPanel;
    private HorizontalPanel buttonPanel;
    
    private Button registerBtn;
    private Button cancelBtn;
    
    private TextBox nameBox;
    private PasswordTextBox passBox;
    
    public RegistrationDialogBox(/*RegistrationHandler*/)
    {
	mainPanel   = new VerticalPanel();
	inputPanel  = new HorizontalPanel();
	buttonPanel = new HorizontalPanel();
	registerBtn = new Button("Register");
	cancelBtn   = new Button("Cancel");
	nameBox     = new TextBox();
	passBox     = new PasswordTextBox();
	
	mainPanel.add(inputPanel);
	mainPanel.add(buttonPanel);
	inputPanel.add(nameBox);
	inputPanel.add(passBox);
	buttonPanel.add(registerBtn);
	buttonPanel.add(cancelBtn);
	
	this.setWidget(mainPanel);
	this.setText("Register new user");
	this.setModal(true);
	
	
	passBox.addKeyPressHandler(new KeyPressHandler() {

	    public void onKeyPress(KeyPressEvent event) {
		if (event.getCharCode() == KeyCodes.KEY_ENTER) {
		    registerBtn.click();
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
    
    public void setRegistrationClickHandler(ClickHandler handler){
	registerBtn.addClickHandler(handler);
    }
    
    public void setCancelClickHandler(ClickHandler handler){
	cancelBtn.addClickHandler(handler);
    }
        
    public void clearInput() {
	nameBox.setText("");
	passBox.setText("");
    }
}
