package com.chat.client.GUI;

import java.util.ArrayList;

import com.chat.client.User;

import com.google.gwt.user.client.ui.ListBox;

public class UserList extends ListBox {
    
    public UserList() {
	// Create a listbox without dropdown
	super(true); 
	this.setWidth("200");
	this.setHeight("300");
    }
    
    
    /**
     * Get the text of the selected item in the list
     * @return
     */
    public String getSelectedItem() {
	return this.getItemText(getSelectedIndex());
    }
    
    
    
    /**
     * Add new users and remove inactive users in the room
     * @param userList
     */
    public void update(ArrayList<User> users) {
	int index = this.getSelectedIndex();
	this.clear();
	
	for (User u : users) {
	    this.addItem(u.getName());
	}
	
	this.setSelectedIndex(index);
    }
}
