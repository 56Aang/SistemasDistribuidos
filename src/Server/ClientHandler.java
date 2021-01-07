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
            case "LOGIN": {
                commandLogin(msg);
                break;
            }
            case "REGISTER": {
                commandSign(msg);
                break;
            }
            case "LOGOUT": {
                commandLogout();
                break;
            }
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
            out.writeUTF("GRANTED");
            out.flush();
        } else {
            out.writeUTF("DENIED");
            out.flush();
        }
    }


    /**
     * Método reponsável por registar um utilizador.
     *
     * @param msg Pedido ao servidor.
     */

    private void commandSign(String msg) throws IOException {
        String[] args = msg.split(";");
        System.out.println(args[1] + args[2]);
        if (this.estado.registerClient(args[1], args[2])) {
            out.writeUTF("USER REGISTED");
            out.flush();
        } else {
            out.writeUTF("USER ALREADY REGISTED");
            out.flush();
        }
    }
}

