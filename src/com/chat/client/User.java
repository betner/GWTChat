package com.chat.client;

import java.io.Serializable;

/**
 * A user.
 */
public class User implements Serializable, Comparable<User> {
	private static final long serialVersionUID = 3551342736480143889L;
	
	/**
	 * The user's name.
	 */
	private String name;
	
	/**
	 * The user's type.
	 */
	private UserType type;

	/**
	 * Default constructor to make the class serializable.
	 * Do not use this if you don't know what you are doing. 
	 */
	public User()
	{	
	}
	
	/**
	 * Use this constructor when creating users.
	 * 
	 * @param name
	 * @param type
	 */
	public User(String name, UserType type)
	{
		this.name = name;
		this.type = type;
	}
	
	/**
	 * Returns the user's type.
	 * 
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns the user's type.
	 * @return
	 */
	public UserType getType()
	{
		return type;
	}
	
	@Override
	public boolean equals(Object o)
	{
		//check for self-comparison
	    if ( this == o ) return true;

	    if ( !(o instanceof User) ) return false;
	    
	    User u = (User)o;

	    return name.equals(u.name) && type == u.type;
	}
	
	@Override
	public int hashCode() {
		int hash = 31;
		hash = hash + 31 * name.hashCode();
		hash = hash + 31 * type.hashCode();
		
		return hash;
	}

	public int compareTo(User cmpTo) {
		return name.compareTo(cmpTo.name);
	}
}
