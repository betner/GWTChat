package com.chat.client.GUI;

import com.google.gwt.user.client.ui.ListBox;

public class RoomList extends ListBox {

    public RoomList() {
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
     * Update the list with available chat rooms on the server
     * @param roomArray
     */
    public void update(String[] roomArray) {
	int index = this.getSelectedIndex();
	this.clear();
	
	for (String room : roomArray) {
	    this.addItem(room);
	}
	
	this.setSelectedIndex(index);
    }
}
