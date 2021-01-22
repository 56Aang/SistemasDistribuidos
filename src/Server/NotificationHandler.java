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

public class NotificationHandler {

    private Map<String, Socket> clientes;
    private final Lock l = new ReentrantLock();

    public NotificationHandler() {
        this.clientes = new HashMap<>();
    }

    public void addClient(String user, Socket s) {
        l.lock();
        try {
            this.clientes.put(user, s);
        } finally {
            l.unlock();
        }
    }

    public void removeClient(String user){
        l.lock();
        try {
            this.clientes.remove(user);
        }finally {
            l.unlock();
        }
    }

    public List<String> alertInfected(List<String> pInfected) throws IOException {
        List<String> usersNotLogged = new ArrayList<>();
        for(String s : pInfected) {
            if (this.clientes.containsKey(s)) {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(this.clientes.get(s).getOutputStream()));
                out.writeUTF("YOU'VE BEEN IN CONTACT WITH AN INFECTED PERSON");
                out.flush();
            }
            else{
                usersNotLogged.add(s);
            }
        }
        return usersNotLogged;
    }

}
