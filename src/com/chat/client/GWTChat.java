package com.chat.client;

import com.chat.client.GUI.ChatWindow;
import com.chat.client.GUI.LoginDialogBox;
import com.chat.client.GUI.PrivateMessageDialogBox;
import com.chat.client.GUI.RegistrationDialogBox;
import com.chat.client.GUI.RoomDialogBox;
import com.chat.client.GUI.RoomList;
import com.chat.client.GUI.UserList;
import com.chat.client.exceptions.DecryptionException;
import com.chat.client.exceptions.EncryptionException;
import com.chat.client.exceptions.InsufficientPrivilege;
import com.chat.client.exceptions.InvalidSessionKey;
import com.chat.client.exceptions.NoSuchUser;
import com.chat.client.exceptions.UserNotAllowed;
import com.chat.client.exceptions.UserNotInRoom;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Main GUI class Initiates all widgets and binds functions to them
 */
public class GWTChat implements EntryPoint {

	// The RPC server
	private ServerInterfaceAsync chatServerSvc = GWT
			.create(ServerInterface.class);

	// Key representing current session with the chat server
	// When this key is invalid we must present the user with a new login dialog
	private String sessionKey;

	// Clients user name
	private String userName;

	// Current chat room the user is logged in to
	private String currentChatRoom;

	// All users belong to a class: admin, moderator or user
	private UserType userType;

	// Message count or timestamp
	// Used to check if there are new messages at the server
	private long messageCount;
	private static final int INITIAL_MESSAGE_COUNT = -1;

	// The timer that gets the room state and private messages
	private Timer statePollTimer;

	// The timer that gets all available chat rooms on the server
	private Timer roomPollTimer;

	Crypto cipher = new CryptoJS(GWT_AES_KEY);

	private static final String GWT_AES_KEY = "1234567890123456";

	private static final int REFRESH_INTERVAL = 1000; // ms
	//private static final int ROOMLIST_POLL_INTERVAL = 5000; // ms

	/*
	 * Interface widgets and panels
	 */
	private HorizontalSplitPanel mainPanel;

	// Chat window and input widgets
	private VerticalPanel chatPanel;
	private HorizontalPanel chatInputPanel;
	private DecoratorPanel chatWindowDecorator;
	private ChatWindow chatWindow;
	private Label chatLabel;
	private TextBox chatInput;
	private Button sendTextBtn;

	// Panel for user list and room list
	private HorizontalPanel listPanel;

	// Panel for user list, room list and buttons
	private VerticalPanel functionPanel;

	// The user list and decorator
	private VerticalPanel userListPanel;
	private UserList userList;
	private DecoratorPanel userListDecorator;
	private Label userListLabel;

	// The room list and decorator
	private VerticalPanel roomListPanel;
	private RoomList roomList;
	private DecoratorPanel roomListDecorator;
	private Label roomListLabel;

	// Panel and buttons for chat functions
	private VerticalPanel chatButtonPanel;
	private HorizontalPanel privilegedButtonPanel;
	private HorizontalPanel userButtonPanel;

	// User functions
	private Button loginBtn;
	private Button logoutBtn;
	private Button registerBtn;
	private Button privateMsgBtn;
	private Button enterRoomBtn;
	private Button leaveRoomBtn;

	// Privileged functions
	private Button kickBtn;
	private Button deleteUserBtn;
	private Button logoutUserBtn;
	private Button createRoomBtn;
	private Button banUserBtn;

	// Dialog boxes
	private RegistrationDialogBox regDialog;
	private LoginDialogBox loginDialog;
	private PrivateMessageDialogBox privMsgDialog;
	private RoomDialogBox createRoomDialog;
	private RoomDialogBox enterRoomDialog;

	/**
	 * Initiate all widgets and build the interface
	 */
	public void onModuleLoad() {
		{
			// Create the RPC server interface
			if (chatServerSvc == null) {
				chatServerSvc = GWT.create(ServerInterface.class);
				Window
						.alert("onModuleLoad: Initiated the server interface. Shouldn't happen here!");
			}

			// Initialize the message counter
			messageCount = 0;

			/********************************************************
			 * 
			 * Create chat window, chat input and add click handlers
			 * 
			 ********************************************************/

			// Holds all application widgets
			mainPanel = new HorizontalSplitPanel();

			// Initiate and assemble the chat area
			chatPanel = new VerticalPanel();
			chatInputPanel = new HorizontalPanel();
			chatLabel = new Label("Chat window");
			chatWindow = new ChatWindow();
			chatInput = new TextBox();
			sendTextBtn = new Button("Send");

			chatWindow.setHeight("500");
			chatWindow.setWidth("600");

			chatInput.setWidth("500");

			chatInputPanel.add(chatInput);
			chatInputPanel.add(sendTextBtn);

			chatWindowDecorator = new DecoratorPanel();
			chatWindowDecorator.setWidget(chatWindow);

			chatPanel.add(chatLabel);
			chatPanel.add(chatWindowDecorator);
			chatPanel.add(chatInputPanel);

			// Disable the send button by default
			sendTextBtn.setEnabled(false);

			sendTextBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					sendMessage(chatInput.getText());
					// clear input after message is sent
					chatInput.setText("");
				}
			});

			chatInput.addKeyPressHandler(new KeyPressHandler() {
				public void onKeyPress(KeyPressEvent event) {
					if (event.getCharCode() == KeyCodes.KEY_ENTER) {
						sendTextBtn.click();
					}
				}
			});

			/********************************************************
			 * * Set up lists and user buttons and add click handlers * *
			 ********************************************************/

			// Initiate and assemble the chat functions
			chatButtonPanel = new VerticalPanel();
			privilegedButtonPanel = new HorizontalPanel();
			userButtonPanel = new HorizontalPanel();
			listPanel = new HorizontalPanel();

			// Room list holds all available rooms on the server
			roomListPanel = new VerticalPanel();
			roomListLabel = new Label("Chat rooms");
			roomList = new RoomList();
			roomListDecorator = new DecoratorPanel();
			roomListDecorator.setWidget(roomList);
			roomListPanel.add(roomListLabel);
			roomListPanel.add(roomList);

			// User list holds all active users in the room
			userListPanel = new VerticalPanel();
			userListLabel = new Label("Users");
			userList = new UserList();
			userListDecorator = new DecoratorPanel();
			userListDecorator.setWidget(userList);
			userListPanel.add(userListLabel);
			userListPanel.add(userList);

			// Assemble user list and room list side by side
			listPanel.add(userListPanel);
			listPanel.add(roomListPanel);

			// Privileged buttons
			kickBtn = new Button("Kick");
			deleteUserBtn = new Button("Delete user");
			createRoomBtn = new Button("Create room");
			logoutUserBtn = new Button("Logout user");
			banUserBtn = new Button("Ban user");
			logoutUserBtn = new Button("Logout user");

			// User buttons
			loginBtn = new Button("Login");
			logoutBtn = new Button("Logout");
			registerBtn = new Button("Register");
			enterRoomBtn = new Button("Enter room");
			leaveRoomBtn = new Button("Leave room");
			privateMsgBtn = new Button("Private message");

			// disable some buttons by default
			privateMsgBtn.setEnabled(false);
			enterRoomBtn.setEnabled(false);

			privilegedButtonPanel.add(kickBtn);
			privilegedButtonPanel.add(banUserBtn);
			privilegedButtonPanel.add(deleteUserBtn);
			privilegedButtonPanel.add(createRoomBtn);

			// disable privileged commands by default
			privilegedButtonPanel.setVisible(false);

			userButtonPanel.add(loginBtn);
			userButtonPanel.add(logoutBtn);
			userButtonPanel.add(registerBtn);
			userButtonPanel.add(privateMsgBtn);
			userButtonPanel.add(enterRoomBtn);
			userButtonPanel.add(leaveRoomBtn);
			chatButtonPanel.add(userButtonPanel);
			chatButtonPanel.add(privilegedButtonPanel);

			// disable by default
			leaveRoomBtn.setEnabled(false);
			logoutBtn.setEnabled(false);

			// Assemble all widgets for user functions (right side of GUI)
			functionPanel = new VerticalPanel();
			functionPanel.add(listPanel);
			functionPanel.add(userButtonPanel);
			functionPanel.add(privilegedButtonPanel);

			// Assemble the main panel
			mainPanel.add(chatPanel);
			mainPanel.add(functionPanel);

			RootPanel.get().add(mainPanel);

			/*
			 * ClickHandlers for all the buttons
			 */

			kickBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
				}
			});

			deleteUserBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
				}
			});

			logoutUserBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
				}
			});

			createRoomBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					createRoomDialog.center();
					createRoomDialog.clearInput();
					createRoomDialog.show();
				}
			});

			loginBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					// present the login dialog
					loginDialog.clearInput();
					loginDialog.center();
					loginDialog.show();
				}
			});

			logoutBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					userList.clear();
					logout(sessionKey);
				}
			});

			privateMsgBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					createPrivateMessage();
				}
			});

			enterRoomBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					leaveRoomBtn.setEnabled(true);
					enterRoomBtn.setEnabled(false);

					// Check if a room is selected in the room list
					String selectedRoom = roomList.getSelectedItem();

					if (selectedRoom != null) {
						enterRoom(selectedRoom);
					} else {
						enterRoomDialog.center();
						enterRoomDialog.clearInput();
						enterRoomDialog.show();
					}
				}
			});

			leaveRoomBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					leaveRoomBtn.setEnabled(false);
					enterRoomBtn.setEnabled(true);
					leaveRoom(currentChatRoom);
				}
			});

			kickBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String user = userList.getSelectedItem();
					if (user != null) {
						kickUser(user);
					}
				}
			});

			banUserBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String user = userList.getSelectedItem();
					if (user != null) {
						banUser(user);
					}
				}
			});

			logoutUserBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String user = userList.getSelectedItem();
					if (user != null) {
						logoutUser(user);
					}
				}
			});

			deleteUserBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					String user = userList.getSelectedItem();
					if (user != null) {
						deleteUser(user);
					}
				}
			});

			registerBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					regDialog.clearInput();
					regDialog.center();
					regDialog.show();
				}
			});

			/**********************************************
			 * * Set up dialog boxes and add click handlers * *
			 **********************************************/

			loginDialog = new LoginDialogBox();
			regDialog = new RegistrationDialogBox();
			privMsgDialog = new PrivateMessageDialogBox();
			createRoomDialog = new RoomDialogBox();
			enterRoomDialog = new RoomDialogBox();

			/*
			 * Create room dialog
			 */

			// OK button
			createRoomDialog.setOKClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					createRoomDialog.hide();
					createChatRoom(createRoomDialog.getRoomName());
				}

			});

			/*
			 * Enter room dialog
			 */

			// OK button
			enterRoomDialog.setOKClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					enterRoomDialog.hide();
					enterRoom(enterRoomDialog.getRoomName());
				}
			});

			/*
			 * Login dialog
			 */

			// Login button
			loginDialog.setLoginClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					login(loginDialog.getUser(), loginDialog.getPass());
				}
			});

			// Register user button
			loginDialog.setRegisterClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					loginDialog.clearInput();
					loginDialog.hide();
				}
			});

			/*
			 * Registration dialog
			 */

			// Register button
			regDialog.setRegistrationClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					register(regDialog.getUser(), regDialog.getPass());
				}
			});

			// Cancel button
			regDialog.setCancelClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					regDialog.clearInput();
					regDialog.hide();
				}
			});

			/*
			 * Private message dialog
			 */

			// Send button
			privMsgDialog.setSendClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					sendPrivateMessage(sessionKey,
							privMsgDialog.getRecipient(), privMsgDialog
									.getMessage());
				}
			});

			// Cancel button
			privMsgDialog.setCancelClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					privMsgDialog.clear();
					privMsgDialog.center();
					privMsgDialog.hide();
				}
			});

			// Show login dialog
			loginDialog.center();
			loginDialog.hide();

			regDialog.center();
			regDialog.hide();

			privMsgDialog.center();
			privMsgDialog.hide();
		}

	} // End onModuleLoad()

	/**********************************************
	 * * Methods called by widgets in the interface * *
	 **********************************************/

	/**
	 * Get the state of the chat room. New messages will be shown in the chat
	 * window. The user list will be updated.
	 * 
	 * @param sessionKey
	 * @param roomName
	 */
	private void pollRoom(String sessionKey, String roomName) {
		AsyncCallback<RoomState> callback = new AsyncCallback<RoomState>() {

			public void onFailure(Throwable caught) {
				// stop polling the server
				statePollTimer.cancel();

				try {
					throw caught;
				} catch (UserNotInRoom e) {
					Window.alert("You are no longer in the chat room");
					enterRoomBtn.setEnabled(true);
					leaveRoomBtn.setEnabled(false);

				} catch (InvalidSessionKey e) {
					Window.alert("You are not logged in to the system");
					loginBtn.setEnabled(true);
					logoutBtn.setEnabled(false);

				} catch (UserNotAllowed e) {
					Window.alert("You are not allowed to enter the chat room:"
							+ currentChatRoom);

				} catch (Throwable e) {
					Window.alert("Unhandled event: " + e.getLocalizedMessage());
					e.printStackTrace();

				} finally {
					userList.clear();
					currentChatRoom = "";
				}
			}

			public void onSuccess(RoomState state) {
				long newCount = state.getLastCount();

				// Check if there are new messages and if so, update the message
				// view
				if (messageCount < newCount) {
					messageCount = newCount;
					try {
						chatWindow.update(state.getMessages(), cipher);
					} catch (DecryptionException e) {
						Window.alert("Decryption error: "
								+ e.getLocalizedMessage());
					}
				}
				// update user list
				userList.update(state.getUsers());
			}
		};
		chatServerSvc.poll(sessionKey, roomName, messageCount, callback);
	}

	/**
	 * Check for new private messages on the server. Downloaded messages are not
	 * stored on the server
	 * 
	 * @param sessionKey
	 */
	private void pollPrivateMessages(String sessionKey) {
		AsyncCallback<PrivateMessage[]> callback = new AsyncCallback<PrivateMessage[]>() {

			// We don't handle every kind of error in a nice manner
			public void onFailure(Throwable caught) {
				// stop polling
				statePollTimer.cancel();
				Window.alert("pollPrivateMessages: "
						+ caught.getLocalizedMessage());
			}

			public void onSuccess(PrivateMessage[] messages) {
				// update chat window with private messages
				if (messages.length > 0) {
					try {
						chatWindow.updateWithPrivMsg(messages, cipher);
					} catch (DecryptionException e) {
						Window.alert("Decryption error: "
								+ e.getLocalizedMessage());
					}
				}
			}
		};
		chatServerSvc.pollPrivateMessages(sessionKey, callback);
	}

	/**
	 * Open a dialog box for creating a private message if a user is selected in
	 * the user list.
	 */
	private void createPrivateMessage() {
		String selectedUser = userList.getItemText(userList.getSelectedIndex());

		if (!selectedUser.isEmpty()) {
			privMsgDialog.setRecipient(selectedUser);
			privMsgDialog.setTitle("Private message to: " + selectedUser);
			privMsgDialog.show();
		} else {
			privMsgDialog.hide(); // make sure the dialog is hidden
		}
	}

	/**
	 * Send a private message to another user in the chat system. The recipient
	 * needn't be in a chat room.
	 * 
	 * @param sessionKey
	 * @param recipientName
	 */
	private void sendPrivateMessage(String sessionKey, String recipientName,
			String message) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				Window.alert("sendMessage: " + caught.getLocalizedMessage());
				privMsgDialog.clearInput();
				privMsgDialog.hide(); // hide the dialog even if we got an error
			}

			public void onSuccess(Void result) {
			}
		};

		PrivateMessage privateMsg;

		try {
			privateMsg = new PrivateMessage(message, cipher);
			chatServerSvc.sendPrivateMessage(sessionKey, recipientName,
					privateMsg, callback);
		} catch (EncryptionException e) {
			Window.alert("Encryption error: " + e.getLocalizedMessage());
		}

		privMsgDialog.clearInput();
		privMsgDialog.hide();
	}

	/**
	 * Send a message to the chat room.
	 * 
	 * @param message
	 */
	private void sendMessage(String message) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				Window.alert("sendMessage: " + caught.getLocalizedMessage());
			}

			public void onSuccess(Void result) {
			}
		};
		try {
			Message msg = new Message(message, cipher);
			chatServerSvc.post(sessionKey, currentChatRoom, msg, callback);
		} catch (EncryptionException e) {
			Window.alert("Encryption error: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Get a list with all available chat rooms on the server
	 */
	private void getRooms() {
		AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

			// We are lazy and let pollRoom handle poll errors
			public void onFailure(Throwable caught) {
				Window.alert("RoomPoll error: " + caught.getLocalizedMessage());
			}

			public void onSuccess(String[] roomArray) {
				roomList.update(roomArray);
			}
		};
		chatServerSvc.getRooms(sessionKey, callback);
	}

	/**
	 * Login to the chat server
	 * 
	 * @param user
	 * @param password
	 */
	private void login(final String user, String password) {

		AsyncCallback<LoginToken> callback = new AsyncCallback<LoginToken>() {

			public void onFailure(Throwable caught) {
				Window.alert(caught.getLocalizedMessage());
			}

			public void onSuccess(LoginToken token) {
				userName = user;
				sessionKey = token.getSessionKey();
				userType = token.getUserType();
				messageCount = INITIAL_MESSAGE_COUNT;
				loginDialog.hide();

				// make sure the buttons are in the right state
				sendTextBtn.setEnabled(true);
				logoutBtn.setEnabled(true);
				loginBtn.setEnabled(false);
				enterRoomBtn.setEnabled(true);
				leaveRoomBtn.setEnabled(false);
				privateMsgBtn.setEnabled(true);

				// Check if user has the right to use the privileged functions
				if (userType == UserType.Administrator
						|| userType == UserType.Moderator) {
					privilegedButtonPanel.setVisible(true);
				}

				// Set up the timer to poll which chat rooms are available
				if (roomPollTimer != null) {
					roomPollTimer.cancel();
				}

				roomPollTimer = new Timer() {
					@Override
					public void run() {
						getRooms();
					}
				};
				roomPollTimer.scheduleRepeating(REFRESH_INTERVAL);
			}
		};

		// Login to the server
		chatServerSvc.login(user, password, callback);
	}

	/**
	 * Logout from the chat server
	 * 
	 * @param sessionKey
	 */
	private void logout(String sessionKey) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				Window.alert(caught.getLocalizedMessage());
			}

			public void onSuccess(Void result) {
				// Kill both timers
				if (statePollTimer != null)
					statePollTimer.cancel();
				if (roomPollTimer != null)
					roomPollTimer.cancel();

				currentChatRoom = "";
				userName = "";

				// make sure buttons are in the right state
				privilegedButtonPanel.setVisible(false);
				logoutBtn.setEnabled(false);
				loginBtn.setEnabled(true);
				privateMsgBtn.setEnabled(false);
				enterRoomBtn.setEnabled(false);
				sendTextBtn.setEnabled(false);

				Window.alert("You have been logged out");
			}

		};

		chatServerSvc.logout(sessionKey, userName, callback);
	}

	/**
	 * Enter a chat room
	 * 
	 * @param roomName
	 */
	private void enterRoom(final String roomName) {

		if (sessionKey == null) {
			Window.alert("Your session key isn't valid. Please login again");
			return;
		}

		AsyncCallback<Long> lastMessageCallback = new AsyncCallback<Long>() {

			public void onFailure(Throwable caught) {
				Window.alert("EnterRoom: " + caught.getLocalizedMessage());
				enterRoomBtn.setEnabled(true);
				leaveRoomBtn.setEnabled(false);
			}

			public void onSuccess(Long lastMessage) {
				// Get the id for the last message in the room
				messageCount = lastMessage;
				currentChatRoom = roomName;
				chatWindow.setText(chatWindow.getText() + "Chat room: "
						+ roomName + "\n\n");
				// Start polling the room and private messages
				statePollTimer.scheduleRepeating(REFRESH_INTERVAL);
			}
		};
		chatServerSvc.enterRoom(sessionKey, roomName, lastMessageCallback);

		// A new polltimer is created for a single chat room
		if (statePollTimer != null) {
			statePollTimer.cancel();
		}
		statePollTimer = new Timer() {
			@Override
			public void run() {
				pollRoom(sessionKey, roomName); // bound to the session and room
				pollPrivateMessages(sessionKey);
			}
		};

	}

	/**
	 * Leave a chat room
	 * 
	 * @param roomName
	 */
	private void leaveRoom(String roomName) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(Void result) {
			}
		};
		chatServerSvc.leaveRoom(sessionKey, userName, roomName, callback);
		currentChatRoom = "";
	}

	/**
	 * Register a new user in the system
	 * 
	 * @param user
	 * @param pass
	 */
	private void register(final String user, String pass) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				Window.alert(caught.getLocalizedMessage());
			}

			public void onSuccess(Void result) {
				Window.alert("User " + user + " successfully registered");
				regDialog.hide();
				regDialog.clearInput();
			}
		};

		chatServerSvc.registerUser(user, pass, callback);
	}

	/**
	 * Create a chat room on the server
	 * 
	 * @param roomName
	 */
	private void createChatRoom(final String roomName) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				Window.alert("createChatRoom: " + caught.getLocalizedMessage());
			}

			public void onSuccess(Void result) {
			}
		};
		chatServerSvc.createRoom(sessionKey, roomName, true, callback);
	}

	/**
	 * Logout another user from the chat server
	 * 
	 * @param user
	 */
	private void logoutUser(String user) {

	}

	/**
	 * Kick a user from the chat room
	 * 
	 * @param user
	 */
	private void kickUser(String user) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(Void result) {
			}
		};

		PrivateMessage privateMsg;
		try {
			privateMsg = new PrivateMessage(
					"You have been kicked from the chat room "
							+ currentChatRoom, cipher);

			Message msg = new Message(user + " is kicked from the chat room",
					cipher);

			// Send a message to the person being kicked
			chatServerSvc.post(sessionKey, currentChatRoom, msg, callback);
			chatServerSvc.sendPrivateMessage(sessionKey, user, privateMsg,
					callback);
			chatServerSvc
					.leaveRoom(sessionKey, user, currentChatRoom, callback);

		} catch (EncryptionException e) {
			Window.alert("Encryption error: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Delete a user from the system
	 * 
	 * @param user
	 */
	private void deleteUser(String user) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchUser e) {
					Window.alert("DeleteUser: NoSuchUser "
							+ e.getLocalizedMessage());
				} catch (InsufficientPrivilege e) {
					Window.alert("DeleteUser: Insufficient privilege "
							+ e.getLocalizedMessage());
				} catch (Throwable e) {
					Window.alert("DeleteUser: " + e.getLocalizedMessage());
				}
			}

			public void onSuccess(Void result) {
			}
		};
		chatServerSvc.removeUser(sessionKey, user, callback);
	}

	/**
	 * Ban a user from a chat room
	 * 
	 * @param user
	 */
	private void banUser(String user) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				try {
					throw caught;
				} catch (NoSuchUser e) {
					Window.alert("BanUser: NoSuchUser "
							+ e.getLocalizedMessage());
				} catch (InsufficientPrivilege e) {
					Window.alert("BanUser: Insufficient privilege "
							+ e.getLocalizedMessage());
				} catch (Throwable e) {
					Window.alert("BanUser: " + e.getLocalizedMessage());
				}
			}

			public void onSuccess(Void result) {
			}
		};
		chatServerSvc.banUser(sessionKey, user, currentChatRoom, callback);
	}
} // end class

