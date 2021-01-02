package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {


	public static void main (String[] args) throws IOException {
		Socket socket = new Socket("localhost",12345);
		PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String userInput;
		while ((userInput = in.readLine()) != null) {
			out.println(userInput);
		}

		socket.shutdownOutput();
		socket.close();
	}
}