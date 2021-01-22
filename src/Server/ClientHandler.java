package Server;

import java.io.*;
import java.net.Socket;

/**
 * Classe responsável por intrepertar os pedidos feitos por um cliente ao servidor.
 */

public class ClientHandler implements Runnable {
    private Socket cs;
    private DataOutputStream out;
    private DataInputStream in;
    private String active_user;
    //private User utilizador;
    private EstadoPartilhado estado;

    public ClientHandler(Socket cs, EstadoPartilhado estado) {
        this.cs = cs;
        this.estado = estado;
        this.active_user = null;
    }

    @Override
    public void run() {
        String msg;

        try {
            out = new DataOutputStream(new BufferedOutputStream(cs.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(cs.getInputStream()));
            while (!(msg = in.readUTF()).equals("EXIT")) {
                System.out.println(msg);
                command(msg);
            }

            out.writeUTF("EXIT");
            out.flush();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("sai");


    }

    /**
     * Método que recebe uma mensagem do cliente e reencaminha para o método correto.
     *
     * @param msg Pedido recebido.
     * @return Valor booleano se a ligação ao servidor tem que ser cortada.
     */

    private boolean command(String msg) throws IOException {
        String[] args = msg.split(";");

        switch (args[0]) {
            case "LOGIN":
                commandLogin(msg);
                break;

            case "REGISTER":
                commandSign(msg);
                break;

            case "LOGOUT":
                commandLogout();
                break;

            case "WRITEMAP":
                out.writeUTF(this.estado.writeMap());
                out.flush();
                break;
            case "CHANGEZONE":
                commandChangeZone(msg);
                break;
            case "CONSULTMAP":
                commandConsultMap();
                break;
            case "INFORMSTATE":
                commandInformState(msg);
                break;
            case "SERVER":
                commandServerNotify(msg);
                break;
            case "CONSULTZONE":
                commandConsultZone(msg);
                break;
            default: {
                this.out.writeUTF("Erro");
                out.flush();
                break;
            }
        }

        return args[0].equals("EXIT");
    }


    /**
     * Método que inicia o processo de término de uma conexão.
     */
    private void commandLogout() throws IOException {
        if (this.active_user != null) {
            this.estado.removeHandler(active_user);
            this.active_user = null;
        }
        out.writeUTF("LOGGED OUT");
        out.flush();
    }

    /**
     * Método reponsável por fazer login de um utilizador.
     *
     * @param msg Pedido ao servidor.
     */

    private void commandLogin(String msg) throws IOException {
        String[] args = msg.split(";");
        if (this.estado.logIn(args[1], args[2])) {
            this.active_user = args[1];
            this.estado.addNewHandler(active_user, cs); // adicionar socket
            if(this.estado.getUser(active_user).hasMsgs()) {
                out.writeUTF("GRANTED;MSG");
                out.flush();

                StringBuilder sb = new StringBuilder();
                sb.append("YOU HAVE NOTIFICATIONS PENDING:");
                for(String s : this.estado.getUser(active_user).getMsgs()){
                    sb.append('\n').append(s);
                }
                out.writeUTF(sb.toString());
                out.flush();
            }
            else{
                out.writeUTF("GRANTED");
                out.flush();
            }


        } else {
            out.writeUTF("DENIED");
            out.flush();
        }
    }

    private void commandChangeZone(String msg) throws IOException {
        String[] args = msg.split(";");
        String ret;
        if ((ret = this.estado.changeZone(this.active_user, args[1].charAt(0))).equals("true")) {
            out.writeUTF("UPDATED SUCCESSFULLY");
            out.flush();
        } else if(ret.equals("false")){
            out.writeUTF("WRONG ZONE");
            out.flush();
        }else {
            out.writeUTF("UPDATED SUCCESSFULLY");
            out.flush();
            this.estado.notificaVaga(ret.charAt(0));
        }
    }

    private void commandConsultMap() throws IOException {
        out.writeUTF(this.estado.mapConsult());
        out.flush();
    }


    /**
     * Método reponsável por registar um utilizador.
     *
     * @param msg Pedido ao servidor.
     */

    private void commandSign(String msg) throws IOException {
        String[] args = msg.split(";");
        System.out.println(args[1] + args[2]);
        if (this.estado.registerClient(args[1], args[2], args[3].charAt(0))) {
            out.writeUTF("USER REGISTERED");
            out.flush();
        } else {
            out.writeUTF("USER ALREADY REGISTERED");
            out.flush();
        }
    }

    private void commandInformState(String msg) throws IOException {
        String[] args = msg.split(";");
        System.out.println(args[1]);
        boolean state;
        if (args[1].equals("TRUE")) {
            state = true;
        } else state = false;
        this.estado.setInfected(active_user, state);
        if (state) {
            estado.notificaInfecao(this.active_user);
            out.writeUTF("USER INFECTED");
            out.flush();
        } else {
            out.writeUTF("USER NOT INFECTED");
            out.flush();
        }

    }

    private void commandServerNotify(String msg) throws IOException{
        String[] args = msg.split(";");
        System.out.println(args[1]);
        if(args[1].equals("RISK-INFECTED")){
            out.writeUTF("YOU'VE BEEN IN CONTACT WITH AN INFECTED PERSON");
            out.flush();
        }
    }

    private void commandConsultZone(String msg) throws IOException {
        String[] args = msg.split(";");
        System.out.println(msg);
        char zone = args[1].charAt(0);
        if(this.estado.addNotifyUser(this.active_user,zone)){
            String output = "YOU WILL RECEIVE NOTIFICATION WHEN ZONE " +  zone + " IS EMPTY";
            out.writeUTF(output);
            out.flush();
        }
        else {
            out.writeUTF("INVALID ZONE");
            out.flush();
        }
    }

}

