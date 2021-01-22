package Client;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {


	public static void main (String[] args) throws IOException {
		Socket socket = new Socket("localhost",12345);
		ClientStatus cStatus = new ClientStatus();
		Lock st = new ReentrantLock();
		Thread t1 = new Thread(new ClientDrawer(socket,cStatus));
		Thread t2 = new Thread(new ClientReader(socket,cStatus));
		t1.start();
		t2.start();
	}
}