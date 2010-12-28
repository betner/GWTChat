package com.chat.client.GUI;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class RoomDialogBox extends DialogBox {

    VerticalPanel mainPanel;
    HorizontalPanel inputPanel;
    HorizontalPanel buttonPanel;
    Button okBtn;
    Button cancelBtn;
    TextBox roomNameBox;
    
    public RoomDialogBox() {
	mainPanel   = new VerticalPanel();
	inputPanel  = new HorizontalPanel();
	buttonPanel = new HorizontalPanel();
	okBtn       = new Button("OK");
	cancelBtn   = new Button("Cancel");
	roomNameBox = new TextBox();
		
	mainPanel.add(inputPanel);
	mainPanel.add(buttonPanel);
	inputPanel.add(roomNameBox);
	buttonPanel.add(okBtn);
	buttonPanel.add(cancelBtn);
	
	this.setWidget(mainPanel);
	this.setText("Enter room name");
	this.setModal(true);
	
	cancelBtn.addClickHandler(new ClickHandler() {
	    public void onClick(ClickEvent event) {
		clearInput();
		hide();
	    }
	});
	
	roomNameBox.addKeyPressHandler(new KeyPressHandler() {
	    public void onKeyPress(KeyPressEvent event) {
		if (event.getCharCode() == KeyCodes.KEY_ENTER) {
		    okBtn.click();
		}
	    }
	});
}

// Make sure the name input box gets focus
public void show() {
    super.show();
    roomNameBox.setFocus(true);
}

public void clearInput() {
    roomNameBox.setText("");
}

public String getRoomName() {
	return roomNameBox.getText();
}

public void setOKClickHandler(ClickHandler handler){
	okBtn.addClickHandler(handler);
}

public void setCancelClickHandler(ClickHandler handler){
	cancelBtn.addClickHandler(handler);
}
}
