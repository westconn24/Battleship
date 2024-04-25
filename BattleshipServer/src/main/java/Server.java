import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<Integer> pendingPlayers = new ArrayList<>();
	HashMap<Integer, Integer> matches = new HashMap<>();
	TheServer server;
	private Consumer<Serializable> callback;


	Server(Consumer<Serializable> call){

		callback = call;
		server = new TheServer();
		server.start();
	}


	public class TheServer extends Thread{

		public void run() {

			try(ServerSocket mysocket = new ServerSocket(5555);){
				System.out.println("Server is waiting for a client!");


				while(true) {

					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);
					clients.add(c);
					c.start();

					count++;

				}
			}//end of try
			catch(Exception e) {
				callback.accept("Server socket did not launch");
			}
		}//end of while
	}


	class ClientThread extends Thread{


		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;

		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;
		}

		public void updateClients(String message) {
			for(int i = 0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				try {
					t.out.writeObject(message);
				}
				catch(Exception e) {}
			}
		}

		public void updateClient(String message, int thread) {
			ClientThread t = clients.get(thread);
			try {
				t.out.writeObject(message);
			} catch (Exception e) {
			}
		}
		public void sendClientCount() {
			try {
				out.writeObject(clients.size());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public boolean isClientCountOdd() {
			return clients.size() % 2 == 1;
		}
		public void run() {
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);

				while (true) {
					try {
						String data = in.readObject().toString();
						callback.accept("client: " + count + " sent: " + data);

						if (data.equals("getClientCount")) {
							this.sendClientCount();
							if (this.isClientCountOdd()) {
								// Notify client if count is odd
								pendingPlayers.add(count);
								this.out.writeObject("OddCount");
							} else {
								String matchup = "op" + count;
								int oppThread = pendingPlayers.remove(0);
								updateClient(matchup, oppThread - 1); //adjustment because of 0-based indexes
								matchup = "op" + oppThread;
								updateClient(matchup, count - 1);
								updateClient("wait", count - 1);
								matches.put(count, oppThread);
								matches.put(oppThread, count);
							}
						} else if (data.equals("ready")){
							updateClient("ready", matches.get(count) - 1); //both are needed
						} else if (data.charAt(0) == 'c' && data.charAt(1) == 'o' && data.charAt(2) == 'r' && data.charAt(3) == 'd'){
							updateClient(data, matches.get(count) - 1);
							updateClient("wait", count - 1);
						} else if (data.charAt(0) == 'm' && data.charAt(1) == 'i' && data.charAt(2) == 's' && data.charAt(3) == 's'){
							updateClient(data, matches.get(count) - 1);
						} else if (data.charAt(0) == 'h' && data.charAt(1) == 'i' && data.charAt(2) == 't'){
							updateClient(data, matches.get(count) - 1);
						} else if (data.equals("loser")){
							updateClient("loser", matches.get(count)-1);
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
						callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
						for (int i = 0; i < pendingPlayers.size(); i++){
							if (pendingPlayers.get(i) == count){
								pendingPlayers.remove(i);
							}
						}
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("Streams not open");
			}
		}
		}//end of run


	}//end of client thread


