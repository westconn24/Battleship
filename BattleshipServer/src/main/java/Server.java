import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

import java.util.ArrayList;

public class Server {

	int count = 1; // Counter for clients, used for any purpose you might have beyond identification
	HashMap<String, ClientThread> clients = new HashMap<>(); // Maps a username to each client thread
	HashMap<String, ArrayList<String>> groups = new HashMap<>(); // Map that maps a group name to each list of users
	private Consumer<Serializable> callback; // Callback for UI or logging

	Server(Consumer<Serializable> call) {
		callback = call;
		new TheServer().start(); // Start the server thread
	}

	class TheServer extends Thread {
		public void run() {
			try (ServerSocket serverSocket = new ServerSocket(5555)) {
				System.out.println("Server is waiting for a client!");

				while (true) {
					Socket clientSocket = serverSocket.accept(); // Accept a client connection
					new ClientThread(clientSocket, count++).start(); // Create and start a client thread
				}
			} catch (Exception e) {
				callback.accept("Server socket did not launch: " + e.getMessage());
			}
		}
	}

	class ClientThread extends Thread {
		Socket connection;
		int count;
		String userName;
		ObjectInputStream in;
		ObjectOutputStream out;

		ClientThread(Socket connection, int count) {
			this.connection = connection;
			this.count = count;
		}

		public void run() {
			try {
				// Initialize ObjectOutputStream first and flush it
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();

				// Initialize ObjectInputStream after the ObjectOutputStream
				in = new ObjectInputStream(connection.getInputStream());

				boolean userNameSet = false;
				while (!userNameSet) {
					// Process the initial message for username validation
					Message initialMessage = (Message) in.readObject();
					userName = initialMessage.getUserName();

					if (clients.containsKey(userName)) {
						// If username is taken, inform the client without closing the connection
						Message error = new Message();
						error.setMessage("ERROR USERNAME TAKEN");
						sendMessage(error);
						// Do not close the connection, wait for the client to send a new username
					} else {
						Message success = new Message();
						success.setMessage("USERNAME GOOD");
						sendMessage(success); // Send only to the user who has just connected.
						clients.put(userName, this); // Add this client to the map

						// So server side known that the user has connected
						callback.accept("[SERVER] " + userName + " has connected.");

						// Broadcast to all other clients that a new user has connected.
						Message newUserConnected = new Message();
						newUserConnected.setMessage(userName + " has connected.");
						newUserConnected.setUserName("[SERVER]");
						newUserConnected.setIsSendAll(false);
						newUserConnected.setIsDeletedUser(false);
						newUserConnected.isServerGroupMes = false;
						newUserConnected.setIsWhisper(false);
						newUserConnected.setIsServer(true);
						broadcastMessage(newUserConnected);

						userNameSet = true;
					}

				}

				// Handle further communication after a unique username has been set
				while (true) {
					Message message = (Message) in.readObject();
					// Use callback to send message data to GUI for display or further processing
					if (message.getIsNewGroup()) {
						// TODO: TESTER DELETE, also this never sends back a message so if its not working that's why
						message.isServerGroupMes = true;
						groups.put(message.getGroupName(), message.groupNames);

						System.out.println(message.actualGroups);
						message.setIsSendAll(false);
						message.setIsServer(false);
						message.setIsDeletedUser(false);
						message.setIsWhisper(false);
						//broadcastMessage(message);
					} else {
						message.isServerGroupMes = false;
						callback.accept(userName + ": " + message.getMessage());
					}
					broadcastMessage(message);
				}
			} catch (Exception e) {
				callback.accept("[SERVER] " + "Client " + userName + " disconnected.");
				Message userDisconnected = new Message();
				userDisconnected.setMessage(userName + " has disconnected.");
				userDisconnected.setUserName("[SERVER]");
				userDisconnected.setIsSendAll(false);
				userDisconnected.setIsServer(true);
				userDisconnected.isServerGroupMes = false;
				userDisconnected.setIsDeletedUser(true);
				userDisconnected.setIsWhisper(false);
				clients.remove(userName); // Remove this client from the map
				broadcastMessage(userDisconnected);
			} finally {
				try {
					if (connection != null) {
						connection.close(); // Ensure the connection is closed on exit
					}
				} catch (Exception e) {}
			}
		}

		// Utility methods to send messages

		// this might be useless, or its use is to only send back end messages and the client should never see these
		void sendMessage(Message message) {
			try {
				out.writeObject(message);
				out.flush();
			} catch (Exception e) {
				System.out.println("Error sending message to " + userName + ": " + e.getMessage());
			}
		}

		// this is what is used to send messages to all clients
		public void broadcastMessage(Message message) {
			ArrayList<String> allUsernames = new ArrayList<>(clients.keySet());
			message.addUsers(allUsernames);
			HashMap<String, ArrayList<String>> deepCopy = new HashMap<>();
			for (HashMap.Entry<String, ArrayList<String>> entry : groups.entrySet()) {
				ArrayList<String> listCopy = new ArrayList<>(entry.getValue());
				deepCopy.put(entry.getKey(), listCopy);
			}
			message.actualGroups = deepCopy;

			if (message.isToGroup == null) {message.isToGroup = false;}

			// send to groups
			if (message.isToGroup) {
                ArrayList<String> tempArr = new ArrayList<>(message.actualGroups.get(message.getGroupName()));
				System.out.println("TESTING");
				for (ClientThread clientThread : clients.values()) {
					if (tempArr.contains(clientThread.userName)) {
						clientThread.sendMessage(message);
					}
				}
			}
			// send only to certain use if it's a whisper
			else if (message.getIsWhisper()) {
				// Send it back to the user who sent it and the user it's sent to
				clients.get(message.getUserNameToSendTo()).sendMessage(message);
				clients.get(message.getUserName()).sendMessage(message);
				System.out.println(message.getIsWhisper());
			}
			// send to whole server if sendALl is true
			else {
				for (ClientThread clientThread : clients.values()) {
					clientThread.sendMessage(message);
				}
			}
		}
	}

}
