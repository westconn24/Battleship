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
	
	Client(Consumer<Serializable> call){
		callback = call;
	}

	public void run() {
		try {
			socketClient = new Socket("127.0.0.1", 5555);
			out = new ObjectOutputStream(socketClient.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socketClient.getInputStream());
			socketClient.setTcpNoDelay(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			// for username verification
			try {
				Message message = (Message) in.readObject();
				if ("ERROR USERNAME TAKEN".equals(message.getMessage())) {
					callback.accept("USERNAME TAKEN");
				} else if ("USERNAME GOOD".equals(message.getMessage())) {
					callback.accept("USERNAME GOOD");
				}
				// Normal message flow
				else {
					callback.accept(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}


	public void send(Message data) {
		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
