package Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Servidor {

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        EstadoPartilhado estado = new EstadoPartilhado();

        while(true){
            Socket socket = serverSocket.accept();
            System.out.println("Conexão aceite");
            Thread worker = new Thread(new ClientHandler(socket,estado));
            worker.start();
        }


    }

}