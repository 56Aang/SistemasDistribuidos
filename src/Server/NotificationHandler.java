package Server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe responsável por notificar os utilizadores.
 */
public class NotificationHandler {

    private Map<String, Socket> clientes;
    private final Lock l = new ReentrantLock();

    public NotificationHandler() {
        this.clientes = new HashMap<>();
    }
    /**
     * Método que adiciona uma Socket a um utilizador.
     *
     * @param user String com o nome do utilizador.
     * @param s Socket do utilizador.
     */
    public void addClient(String user, Socket s) {
        l.lock();
        try {
            this.clientes.put(user, s);
        } finally {
            l.unlock();
        }
    }
    /**
     * Método que remove uma Socket de um utilizador.
     *
     * @param user String com o nome do utilizador.
     */
    public void removeClient(String user) {
        l.lock();
        try {
            this.clientes.remove(user);
        } finally {
            l.unlock();
        }
    }
    /**
     * Método que notifica utilizadores em risco.
     *
     * @param pInfected Lista com os nomes dos utilizadores a notificar.
     * @return List com os nomes dos utilizadores que se encontram offline.
     */
    public List<String> alertInfected(List<String> pInfected) throws IOException {
        l.lock();
        try {
            List<String> usersNotLogged = new ArrayList<>();
            for (String s : pInfected) {
                if (this.clientes.containsKey(s)) {
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.clientes.get(s).getOutputStream()));
                    out.writeUTF("YOU'VE BEEN IN CONTACT WITH AN INFECTED PERSON");
                    out.flush();
                } else {
                    usersNotLogged.add(s);
                }
            }
            return usersNotLogged;
        } finally {
            l.unlock();
        }
    }
    /**
     * Método que notifica utilizadores que uma zona ficou livre.
     *
     * @param usersToNotify Lista com os nomes dos utilizadores a notificar.
     * @param a char com a zona.
     */
    public void alertFreeZone(List<String> usersToNotify,char a) throws IOException {
        l.lock();
        try {
            String output = "ZONE " + a + " IS FREE";

            for (String s : usersToNotify) {
                if(this.clientes.containsKey(s)) {
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.clientes.get(s).getOutputStream()));
                    out.writeUTF(output);
                    out.flush();
                }
            }
        } finally {
            l.unlock();
        }
    }

}
