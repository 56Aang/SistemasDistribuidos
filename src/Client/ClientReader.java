package Client;

import java.io.*;
import java.net.Socket;

public class ClientReader implements Runnable{
    private Socket cs;
    private DataInputStream in;
    private ClientStatus status;

    public ClientReader(Socket cs, ClientStatus status){
        this.cs = cs;
        this.status = status;
    }

    @Override
    public void run() {
        String msg;
        boolean msgs = false;
        try{
            this.in = new DataInputStream(new BufferedInputStream(cs.getInputStream()));
            while(!(msg = this.in.readUTF()).equals("EXIT")){
                if(msg.equals("GRANTED")){
                    this.status.login();
                }
                else if(msg.equals("LOGGED OUT")){
                    this.status.logout();
                }
                else if(msg.equals("USER INFECTED")){
                    this.status.setInfected(true);
                }
                else if (msg.equals("USER NOT INFECTED")){
                    this.status.setInfected(false);
                }
                else if(msg.equals("GRANTED;MSG")){
                    this.status.login();
                    msgs = true;
                }
                if(this.status.getWaiting() && msgs){
                    String[] aux = msg.split(";");
                    System.out.println(aux[0]);
                    msg = this.in.readUTF();
                    System.out.println("MESSAGE FROM SERVER: " + msg);
                    this.status.setWaitingOFF();
                }
                else if(this.status.getWaiting()){
                    System.out.println(msg);
                    this.status.setWaitingOFF();
                }
                else{
                    System.out.println("MESSAGE FROM SERVER: " + msg);
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
