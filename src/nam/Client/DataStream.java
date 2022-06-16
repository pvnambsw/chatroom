package nam.Client;

import java.io.DataInputStream;
import java.io.IOException;

import nam.model.Message;

public class DataStream extends Thread {
	private boolean run;
	private DataInputStream dis;
	private Client client;

	public DataStream(Client client, DataInputStream dis) {
		
		this.client = client;
		this.dis = dis;
		this.start();
		
	}

	public void run() {
		run = true;
		String msg1, msg2;
		while (run) {
			try {
//				msg1 = dis.readUTF();
//				msg2 = dis.readUTF();
//				client.getMSG(msg1, msg2);
				Message message = client.getMSG();
				client.getMSG(message);
				System.out.println(message.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopThread() {
		this.run = false;
	}
}
