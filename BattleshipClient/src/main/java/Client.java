import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{


	Socket socketClient;

	ObjectOutputStream out;
	ObjectInputStream in;

	private Consumer<Serializable> callback;
	private BattleshipClient battleshipClient; // Reference to BattleshipClient instance

	Client(Consumer<Serializable> call, BattleshipClient battleshipClient) {
		callback = call;
		this.battleshipClient = battleshipClient; // Initialize reference
	}
	Client(Consumer<Serializable> call){

		callback = call;
	}

	public void run() {

		try {
			socketClient= new Socket("127.0.0.1",5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {}

		while(true) {

			try {
				String message = in.readObject().toString();
				callback.accept(message);
				if (message.equals("OddCount")) {
					Platform.runLater(() -> {
						battleshipClient.showWaitingScene();
					});
				}
			}
			catch(Exception e) {}
		}

	}

	public void send(String data) {

		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}