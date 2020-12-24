package Server;

import Server.EstadoPartilhado;

import java.io.*;
import java.net.Socket;

/**
 * Classe responsável por intrepertar os pedidos feitos por um cliente ao servidor.
 */

public class ClientHandler implements Runnable {
    private Socket cs;
    private PrintWriter out;
    private BufferedReader in;
    //    private String active_user;
    private User utilizador;
    private EstadoPartilhado estado;


    public ClientHandler(Socket cs, EstadoPartilhado estado) {
        this.cs = cs;
        this.estado = estado;
        this.utilizador = null;
    }

    @Override
    public void run() {
        String msg;

        try {
            out = new PrintWriter(cs.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            while ((msg = in.readLine()) != null) {
                System.out.println(msg);
                command(msg);
            }

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

    private boolean command(String msg) {
        String[] args = msg.split(" ");

        switch (args[0].toUpperCase()) {
            case "LOGIN": {
                commandLogin(msg);
                break;
            }
            case "SIGN": {
                commandSign(msg);
                break;
            }
            case "LOGOUT": {
                commandLogout();
                break;
            }
            default: {
                this.out.println("Erro");
                break;
            }
        }

        boolean result = args[0].equals("EXIT");

        return result;
    }


    /**
     * Método que inicia o processo de término de uma conexão.
     */
    private void commandLogout() {
        if (this.utilizador != null) {
            this.utilizador = null;
        }
        out.println("END");
    }

    /**
     * Método reponsável por fazer login de um utilizador.
     *
     * @param msg Pedido ao servidor.
     */

    private void commandLogin(String msg) {
        String[] args = msg.split(" ");
        if (this.estado.logIn(args[1], args[2])) {
            this.utilizador = this.estado.getUser(args[1]);
            out.println("GRANTED");
        } else {
            out.println("DENIED");
        }
    }


    /**
     * Método reponsável por registar um utilizador.
     *
     * @param msg Pedido ao servidor.
     */

    private void commandSign(String msg) {
        String[] args = msg.split(" ");
        if (this.estado.registerClient(args[1], args[2])) {
            out.println("USER REGISTED");
        } else {
            out.println("USER ALREADY REGISTED");
        }
    }
}

