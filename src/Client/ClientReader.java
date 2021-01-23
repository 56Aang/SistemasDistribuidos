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
        String[] args;
        boolean msgs = false;
        try{
            this.in = new DataInputStream(new BufferedInputStream(cs.getInputStream()));
            while(!(args = (msg = this.in.readUTF()).split(";"))[0].equals("EXIT")){
                if(args[0].equals("GRANTED")){
                    this.status.login();
                    this.status.setInfected(args[1].equals("TRUE")); // se vier a true, infected <- true
                    msgs = args[2].equals("TRUE");
                }
                else if(args[0].equals("LOGGED OUT")){
                    this.status.logout();
                }
                else if(args[0].equals("USER INFECTED")){
                    this.status.setInfected(true);
                }
                else if (args[0].equals("USER NOT INFECTED")){
                    this.status.setInfected(false);
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
