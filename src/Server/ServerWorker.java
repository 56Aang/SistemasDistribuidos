package Server;

import Server.EstadoPartilhado;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private Socket socket;
    private EstadoPartilhado estado;

    public ServerWorker (Socket socket, EstadoPartilhado estado) {
        this.socket = socket;
        this.estado = estado;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));


            socket.shutdownInput();
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}