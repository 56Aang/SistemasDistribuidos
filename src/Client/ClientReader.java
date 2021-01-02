package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientReader implements Runnable{
    Socket cs;
    BufferedReader in;
    ClientStatus status;

    public ClientReader(Socket cs, ClientStatus status){
        this.cs = cs;
        this.status = status;
    }

    @Override
    public void run() {
        String msg;
        try{
            InputStream in1;
            this.in = new BufferedReader(new InputStreamReader(this.cs.getInputStream()));
            while((msg = this.in.readLine()) != null){
                System.out.println(msg);

                if(this.status.getWaiting()){
                    this.status.setWaitingOFF();
                }
            }



        } catch (IOException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }
}
