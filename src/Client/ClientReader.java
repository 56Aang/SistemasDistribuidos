package Client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


/**
 * Classe responsável por ler a informação proveniente do servidor.
 */
public class ClientReader implements Runnable {
    private Socket cs;
    private DataInputStream in;
    private ClientStatus status;

    public ClientReader(Socket cs, ClientStatus status) {
        this.cs = cs;
        this.status = status;
    }

    /**
     * Método para ser executado pela thread.
     */
    public void run() {
        String msg;
        String[] args;
        boolean msgs = false;
        try {
            this.in = new DataInputStream(new BufferedInputStream(cs.getInputStream()));
            while (!(args = (msg = this.in.readUTF()).split(";"))[0].equals("EXIT")) {
                switch (args[0]) {
                    case "GRANTED" -> { // GRANTED;ISINFECTED;TEMMENSAGENS;ÉESPECIAL
                        this.status.login();
                        this.status.setInfected(args[1].equals("TRUE")); // se vier a true, infected <- true
                        msgs = args[2].equals("TRUE");
                        this.status.setSpecial(args[3].equals("TRUE"));
                    }
                    case "LOGGED OUT" -> this.status.logout();
                    case "USER INFECTED" -> this.status.setInfected(true);
                    case "USER NOT INFECTED" -> this.status.setInfected(false);
                }
                if (args[0].equals("GRANTED")) { // quando dá login
                    System.out.println(args[0]);
                    if (this.status.getState())
                        System.out.println("YOU ARE INFECTED");
                    else
                        System.out.println("YOU ARE NOT INFECTED");
                    if (msgs) {
                        msg = this.in.readUTF();
                        System.out.println("MESSAGE FROM SERVER: " + msg);
                    }
                    this.status.setWaitingOFF();
                } else if (this.status.getWaiting()) { // está à espera de resposta
                    System.out.println(msg);
                    this.status.setWaitingOFF();
                } else { // mensagens do servidor só
                    System.out.println("MESSAGE FROM SERVER: " + msg);
                }
            }
            this.status.exited();
            if (this.status.getWaiting()) {
                this.status.setWaitingOFF();
            }


        } catch (IOException e) {
            System.out.println("error");
            e.printStackTrace();
        }
    }
}
