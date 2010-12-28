package com.chat.server;

/**
 * Container that holds a room's name and public flag.
 *
 */
public class RoomData {
	/**
	 * The name of the room.
	 */
	private String name;
	/**
	 * The room's public flag.
	 */
	private boolean isPublic;

	public RoomData(String name, boolean isPublic) {
		this.name = name;
		this.isPublic = isPublic;
	}

	public String getName() {
		return name;
	}

	public boolean isPublic() {
		return isPublic;
	}
}
