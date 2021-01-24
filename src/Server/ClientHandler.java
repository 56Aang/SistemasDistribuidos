package Server;

import Exceptions.BadZoneException;
import Exceptions.InvalidUserException;
import Exceptions.UserAlreadyExistingException;

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
    private EstadoPartilhado estado;

    public ClientHandler(Socket cs, EstadoPartilhado estado) {
        this.cs = cs;
        this.estado = estado;
        this.active_user = null;
    }

    /**
     * Método para ser executado pela thread.
     */
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
     * @param msg Pedido recebido.
     * @throws IOException
     */
    private void command(String msg) throws IOException{
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
            case "CONSULTZONE":
                commandConsultZone(msg);
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
            case "CONSULTZONENOTIFY":
                commandConsultZoneNotify(msg);
                break;
            case "DOWNLOADPLZ":
                commandDownloadMap();
                break;
            default: {
                this.out.writeUTF("Erro");
                out.flush();
                break;
            }
        }

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
     * @param msg Pedido ao servidor.
     * @throws IOException
     */
    private void commandLogin(String msg) throws IOException{
        String[] args = msg.split(";");
        String pw = (args.length > 2) ? args[2] : ""; // permitir pass's vazias
        if (this.estado.logIn(args[1], pw)) {
            this.active_user = args[1];
            this.estado.addNewHandler(active_user, cs); // adicionar socket

            String state = this.estado.getUser(active_user).isInfected() ? "TRUE" : "FALSE";
            String special = this.estado.getUser(active_user).isSpecial() ? "TRUE" : "FALSE";

            if (this.estado.getUser(active_user).hasMsgs()) {
                out.writeUTF("GRANTED;" + state + ";TRUE;" + special);
                out.flush();

                StringBuilder sb = new StringBuilder();
                sb.append("YOU HAVE NOTIFICATIONS PENDING:");
                for (String s : this.estado.getUser(active_user).getMsgs()) {
                    sb.append('\n').append(s);
                }
                this.estado.getUser(active_user).clearMsgs();
                out.writeUTF(sb.toString());
                out.flush();
            } else {
                out.writeUTF("GRANTED;" + state + ";FALSE;" + special);
                out.flush();
            }


        } else {
            out.writeUTF("DENIED");
            out.flush();
        }
    }
    /**
     * Método que altera localização de um utilizador.
     * @param msg Pedido ao servidor.
     * @throws IOException
     */
    private void commandChangeZone(String msg) throws IOException {
        String[] args = msg.split(";");
        String ret;
        try {
            if ((ret = this.estado.changeZone(this.active_user, args[1].charAt(0))).equals("true")) {
                out.writeUTF("UPDATED SUCCESSFULLY");
                out.flush();
            } else {
                out.writeUTF("UPDATED SUCCESSFULLY");
                out.flush();
                this.estado.notificaVaga(ret.charAt(0));
            }
            HistoricParser.addC(this.active_user, args[1].charAt(0), this.estado.getUser(active_user).isInfected(), false);
        } catch (BadZoneException e) {
            out.writeUTF("BAD ZONE");
            out.flush();
        } catch (InvalidUserException e) { // não deve calhar aqui
            out.writeUTF("INVALID USER");
            out.flush();
        }
    }
    /**
     * Método que consulta uma dada zona inserida pelo utilizador
     * @param msg Pedido ao servidor.
     * @throws IOException
     */
    private void commandConsultZone(String msg) throws IOException {
        try {
            String[] args = msg.split(";");
            String res;
            char zone = args[1].charAt(0);
            int qt = this.estado.zoneConsult(zone);
            if (qt == 0) {
                res = "THERE IS NOBODY IN ZONE " + zone;
            }
            else if (qt == 1){
                res = "THERE IS 1 PERSON IN ZONE " + zone;
            }
            else{
                res = "THERE ARE " + qt + " PEOPLE IN ZONE " + zone;
            }
            out.writeUTF(res);
            out.flush();
        } catch (BadZoneException e) {
            out.writeUTF("INVALID ZONE");
            out.flush();
        }
    }
    /**
     * Método que imprime para o utilizador um mapa com a quantidade de utilizadores nas várias zonas.
     * @throws IOException
     */
    private void commandConsultMap() throws IOException {
        out.writeUTF(this.estado.mapConsult());
        out.flush();
    }


    /**
     * Método reponsável por registar um utilizador.
     * @param msg Pedido ao servidor.
     */
    private void commandSign(String msg) throws IOException {
        String[] args = msg.split(";");
        System.out.println(args[1] + args[2]);
        try {
            if (this.estado.registerClient(args[1], args[2], args[3])) {
                HistoricParser.addC(args[1], args[3].charAt(0), false, false);
                out.writeUTF("USER REGISTERED");
            } else {
                out.writeUTF("USER ALREADY REGISTERED");
            }
            out.flush();
        } catch (BadZoneException e) {
            out.writeUTF("COULDN'T REGISTER: WRONG ZONE");
            out.flush();
        } catch (UserAlreadyExistingException e) {
            out.writeUTF("COULDN'T REGISTER: User already exists: " + e.getMessage());
            out.flush();
        }
    }

    /**
     * Método que altera estado de infeção do utilizador.
     * @param msg Pedido ao servidor.
     * @throws IOException
     */
    private void commandInformState(String msg) throws IOException{
        String[] args = msg.split(";");
        System.out.println(args[1]);
        boolean state;

        state = args[1].equals("TRUE");

        if (state) { // está infetado
            this.estado.setInfected(active_user, state);
            estado.notificaInfecao(this.active_user);
            out.writeUTF("USER INFECTED");
            HistoricParser.addC(active_user, this.estado.getZone(this.estado.getUser(active_user).getX(), this.estado.getUser(active_user).getY()), true, false);
        } else {
            if (this.estado.getUser(active_user).isInfected()) { // mudou de infetado -> não infetado
                this.estado.setInfected(active_user, state); // atualiza estado para não infetado


                //verificar isto
                this.estado.atualizaUsers(this.estado.getUser(active_user).getX(),this.estado.getUser(active_user).getY(),active_user); // atualiza lista de recently with


                HistoricParser.addC(active_user, this.estado.getZone(this.estado.getUser(active_user).getX(), this.estado.getUser(active_user).getY()), false, true);
            }
            out.writeUTF("USER NOT INFECTED");
        }
        out.flush();
    }
    /**
     * Método que notifica todos os utilizadores que estão em risco de estarem infetados.
     * @param msg Pedido ao servidor do NotificationHandler.
     * @throws IOException
     */
    private void commandServerNotify(String msg) throws IOException {
        String[] args = msg.split(";");
        System.out.println(args[1]);
        if (args[1].equals("RISK-INFECTED")) {
            out.writeUTF("YOU'VE BEEN IN CONTACT WITH AN INFECTED PERSON");
            out.flush();
        }
    }
    /**
     * Método que adiciona utilizador à lista de utilizadores a notificar por zona.
     * @param msg Pedido ao servidor.
     * @throws IOException
     */
    private void commandConsultZoneNotify(String msg) throws IOException {
        try {
            String[] args = msg.split(";");
            System.out.println(msg);
            char zone = args[1].charAt(0);
            if (this.estado.addNotifyUser(this.active_user, zone)) {
                String output = "YOU WILL RECEIVE NOTIFICATION WHEN ZONE " + zone + " IS EMPTY";
                out.writeUTF(output);
                out.flush();
            } else {
                out.writeUTF("ZONE ALREADY ADDED");
                out.flush();
            }
        } catch (BadZoneException e) {
            out.writeUTF("INVALID ZONE");
            out.flush();
        }
    }
    /**
     * Método que faz download de um mapa com estatísticas dos utilizadores.
     * @throws IOException
     */
    private void commandDownloadMap() throws IOException {
        out.writeUTF("DOWNLOAD SUCCESSFUL.\nYOU CAN FIND IT AT: " + HistoricParser.statisticsMapFile(this.estado.getMapaLength()));
        out.flush();
    }

}

