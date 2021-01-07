package Client;

import java.io.*;
import java.net.Socket;

public class ClientReader implements Runnable{
    Socket cs;
    DataInputStream in;
    ClientStatus status;

    public ClientReader(Socket cs, ClientStatus status){
        this.cs = cs;
        this.status = status;
    }

    @Override
    public void run() {
        String msg;
        try{
            this.in = new DataInputStream(new BufferedInputStream(cs.getInputStream()));
            while(!(msg = this.in.readUTF()).equals("EXIT")){
                System.out.println(msg);
                if(msg.equals("GRANTED")){
                    this.status.login();
                }
                else if(msg.equals("LOGGED OUT")){
                    this.status.logout();
                }
                if(this.status.getWaiting()){
                    this.status.setWaitingOFF();
                }
            }
            this.status.exited();
            if(this.status.getWaiting()){
                this.status.setWaitingOFF();
            }



        } catch (IOException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }
}
