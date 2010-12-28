package com.chat.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.chat.client.User;
import com.chat.client.UserType;

/**
 * SQLite implementation of the database interface.
 */

public class SQLiteDatabase implements Database {
	/**
	 * SQL database connection. 
	 */
	private Connection con;
	
	/**
	 * Create a new database connection. If reset is true then all existing data will be removed.
	 * 
	 * @param dbName
	 * @param reset
	 */
	public SQLiteDatabase(String dbName, boolean reset) {
		try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:" + dbName);
			
			if(reset)
			{
				resetDatabase();
			}
		} catch (ClassNotFoundException e) {
			throw new Error(e);
		} catch (SQLException e) {
			throw new Error(e);
		}
	}
	
	public void addRoom(String name, boolean isPublic) {
		try {
			PreparedStatement prep = con.prepareStatement("INSERT INTO Rooms VALUES (NULL, ?, ?);");
			
			prep.setString(1, name);
			prep.setBoolean(2, isPublic);
			prep.executeUpdate();
			
			prep.close();
		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	public void addToAllowList(String user, String room) {
		try {
			PreparedStatement prep = con.prepareStatement(
					"INSERT INTO AllowLists (User,Room) " +
					"SELECT Users.ID, Rooms.ID " +
					"FROM Users, Rooms " +
					"WHERE Users.Name = ? AND Rooms.Name = ?;"
			);
			
			prep.setString(1, user);
			prep.setString(2, room);
			prep.executeUpdate();
			
			prep.close();
		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	public void addUser(User user, String passwordHash) {
		try {
			PreparedStatement prep = con.prepareStatement("INSERT INTO Users VALUES(NULL,?, ?, ?);");
			
			prep.setString(1, user.getName());
			prep.setInt(2, user.getType().ordinal());
			prep.setString(3, passwordHash);
			prep.execute();
			
			prep.close();
		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	
	public User getUser(String name) {
		try {
			PreparedStatement prep = con.prepareStatement(
					"SELECT Type " +
					"FROM Users " +
					"WHERE Name = ?"
			);
			
			prep.setString(1, name);
			ResultSet res = prep.executeQuery();
			
			if(!res.next())
			{
				prep.close();
				return null;
			}
			
			int type = res.getInt(1);
			prep.close();
			
			return new User(name, UserType.values()[type]);
		} catch (SQLException e) {
			throw new Error(e);
		}
	}
	
	public boolean matchesUser(String name, String passwordHash) {
		try {
			PreparedStatement prep = con.prepareStatement(
					"SELECT Name " +
					"FROM Users " +
					"WHERE Name = ? AND Password = ?;"
			);
			
			prep.setString(1, name);
			prep.setString(2, passwordHash);
			ResultSet res = prep.executeQuery();
			
			boolean result = res.next();

			prep.close();
			
			return result;
		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	
	public void removeFromAllowList(String user, String room) {
		try {
			PreparedStatement prep = con.prepareStatement(
					"DELETE " +
					"FROM AllowLists " +
					"WHERE " +
						"User IN " +
							"(SELECT ID FROM Users WHERE Name = ?) " +
						"AND Room IN " +
							"(SELECT ID FROM Rooms WHERE Name = ?" +
					");"
			);
			
			prep.setString(1, user);
			prep.setString(2, room);
			prep.executeUpdate();
			
			prep.close();
		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	public void removeRoom(String name) {
		PreparedStatement prep_del_list;
		try {
			prep_del_list = con.prepareStatement(
					"DELETE " +
					"FROM AllowLists " +
					"Where Room IN (SELECT ID FROM Rooms WHERE Name = ?);"
			);

			PreparedStatement prep_del_room = con.prepareStatement(
					"DELETE FROM Rooms " +
					"WHERE Name = ?;"
			);
		
			prep_del_list.setString(1, name);
			prep_del_room.setString(1, name);
			
			prep_del_list.executeUpdate();
			prep_del_list.close();
			
			prep_del_room.executeUpdate();
			prep_del_room.close();
		} catch (SQLException e) {
			throw new Error(e);
		}

	}
	
	public void removeUser(String name) {
		PreparedStatement prep_del_list;
		try {
			prep_del_list = con.prepareStatement(
					"DELETE " +
					"FROM AllowLists " +
					"WHERE User IN (SELECT ID FROM Users WHERE Name = ?);"
			);

			PreparedStatement prep_del_user = con.prepareStatement(
					"DELETE " +
					"FROM Users " +
					"WHERE Name = ?;"
			);
		
			prep_del_list.setString(1, name);
			prep_del_user.setString(1, name);
		
			prep_del_list.executeUpdate();
			prep_del_list.close();
			
			prep_del_user.executeUpdate();
			prep_del_user.close();
		} catch (SQLException e) {
			throw new Error(e);
		}
	}
	
	public boolean userExists(String name) {
		try {
			PreparedStatement prep = con.prepareStatement(
					"SELECT Name " +
					"FROM Users " +
					"WHERE Name = ?;"
			);
			
			prep.setString(1, name);
			ResultSet res = prep.executeQuery();
			
			boolean result = res.next();	
			prep.close();
			
			return result;
		} catch (SQLException e) {
			throw new Error(e);
		}
	}
	
	public List<User> getAllowList(String roomName) {
		try {
			PreparedStatement prep = con.prepareStatement(
					"SELECT Users.Name, Users.Type " +
					"FROM Users, Rooms, AllowLists " +
					"WHERE " +
						"Rooms.Name = ? AND " +
						"Rooms.ID = AllowLists.Room AND " +
						"AllowLists.User = Users.ID;"
			);
			
			prep.setString(1, roomName);
			ResultSet res = prep.executeQuery();
						
			ArrayList<User> users = new ArrayList<User>();
			
			while(res.next())
			{
				String name = res.getString(1);
				UserType type = UserType.values()[res.getInt(1)];
				
				users.add(new User(name, type));
			}
			
			prep.close();
			
			return users;
		} catch (SQLException e) {
			throw new Error(e);
		}
	}
	
	public List<RoomData> getRoomData() {
		try {
			PreparedStatement prep = con.prepareStatement("SELECT Name, Public FROM Rooms;");
			ResultSet res = prep.executeQuery();
						
			ArrayList<RoomData> rooms = new ArrayList<RoomData>();
			
			while(res.next())
			{
				rooms.add(new RoomData(res.getString(1), res.getBoolean(2)));
			}
			
			prep.close();
			
			return rooms;
		} catch (SQLException e) {
			throw new Error(e);
		}
	}
	
	/**
	 * Remove all data in the database and recreate the tables.
	 * 
	 * @throws SQLException
	 */
	private void resetDatabase() throws SQLException {
		Statement state = con.createStatement();

		//Remove all tables.
		state.executeUpdate("drop table if exists Users;");
		state.executeUpdate("drop table if exists Rooms;");
		state.executeUpdate("drop table if exists AllowLists;");

		//Recreate the tables.
		state.executeUpdate("CREATE TABLE Users(" +
				"ID INTEGER PRIMARY KEY," + 
				"Name VARCHAR(50) NOT NULL," + 
				"Type INTEGER NOT NULL," + 
				"Password VARCHAR(50) NOT NULL" + ");");

		state.executeUpdate("CREATE TABLE Rooms(" + 
				"ID INTEGER PRIMARY KEY," + 
				"Name VARCHAR(50) NOT NULL," + 
				"Public BOOLEAN NOT NULL" + 
				");"
				);

		state.executeUpdate("CREATE TABLE AllowLists(" + 
				"ID INTEGER PRIMARY KEY," + 
				"User INTEGER NOT NULL," + 
				"Room INTEGER NOT NULL" + 
				");"
				);
		
		state.close();
	}
}
