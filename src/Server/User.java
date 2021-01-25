package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Classe relativa à representação de um Utilizador.
 */
public class User {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private String user;
    private String password;
    private List<String> recentlyWith;
    private int pos_x;
    private int pos_y;
    private boolean isInfected;
    private List<String> msgs;
    private boolean special;

    public User(String user, String password, int pos_x, int pos_y, boolean isInfected, boolean isSpecial) {
        this.user = user;
        this.password = password;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.isInfected = isInfected;
        this.recentlyWith = new ArrayList<>();
        this.msgs = new ArrayList<>();
        this.special = isSpecial;
    }

    /**
     * Método que altera as coordenadas de um utilizador.
     *
     * @param x int que contém a linha relativa à zona.
     * @param y int que contém a coluna relativa à zona.
     */
    public void moveTo(int x, int y) {
        wl.lock();
        try {
            this.pos_x = x;
            this.pos_y = y;
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que verifica se o utilizador é especial.
     *
     * @return boolean
     */
    public boolean isSpecial() {
        rl.lock();
        try {
            return this.special;
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que verifica se a password é válida para um utilizador.
     *
     * @param pw String com a password do utilizador.
     * @return boolean
     */
    public boolean authenticate(String pw) {
        rl.lock();
        try {
            return (this.password.equals(pw));
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que adiciona informação sobre o estado da infeção de um utilizador.
     *
     * @param infected boolean relativo ao estado do utilizador.
     */
    public void setInfected(boolean infected) {
        wl.lock();
        try {
            this.isInfected = infected;
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que verifica se o utilizador se encontra infetado.
     *
     * @return boolean
     */
    public boolean isInfected() {
        rl.lock();
        try {
            return this.isInfected;
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que apaga os utilizadores com quem esteve.
     */
    public void clearRecentlyWith() {
        wl.lock();
        try {
            this.recentlyWith = new ArrayList<>();
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que apaga um dado utilizador da lista dos utilizadores recentes.
     *
     * @param user String com o nome do utilizador a remover
     */
    public void removeRecentlyWith(String user) {
        wl.lock();
        try {
            this.recentlyWith.remove(user);
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que adiciona um dado utilizador à lista dos utilizadores recentes.
     *
     * @param user String com o nome do utilizador a adicionar.
     */
    public void addRecent(String user) {
        wl.lock();
        try {
            this.recentlyWith.add(user);
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que devolve nome do utilizador.
     *
     * @return String
     */
    public String getUser() {
        rl.lock();
        try {
            return this.user;
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que devolve a lista de utilizadores com quem o cliente esteve recentemente.
     *
     * @return List
     */
    public List<String> getRecentlyWith() {
        rl.lock();
        try {
            return new ArrayList<>(this.recentlyWith);
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que verifica se o cliente esteve com um utilizador recentemente.
     *
     * @param user String com o nome do utilizador.
     * @return boolean
     */
    public boolean wasRecentlyWith(String user) {
        rl.lock();
        try {
            return this.recentlyWith.contains(user);
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que devolve a linha onde o cliente se encontra.
     *
     * @return int
     */
    public int getX() {
        rl.lock();
        try {
            return this.pos_x;
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que devolve a coluna onde o cliente se encontra.
     *
     * @return int
     */
    public int getY() {
        rl.lock();
        try {
            return this.pos_y;
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que adiciona uma mensagem à lista de mensagens do utilizador.
     *
     * @param msg String com o conteúdo da mensagem
     */
    public void addMsg(String msg) {
        wl.lock();
        try {
            this.msgs.add(msg);
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que apaga as mensagens de um utilizador.
     */
    public void clearMsgs() {
        wl.lock();
        try {
            this.msgs = new ArrayList<>();
        } finally {
            wl.unlock();
        }
    }

    /**
     * Método que verifica se o utilizador possui mensagens para ler.
     *
     * @return boolean
     */
    public boolean hasMsgs() {
        rl.lock();
        try {
            return !this.msgs.isEmpty();
        } finally {
            rl.unlock();
        }
    }

    /**
     * Método que devolve a lista de mensagens de um utilizador.
     *
     * @return List
     */
    public List<String> getMsgs() {
        rl.lock();
        try {
            return new ArrayList<>(this.msgs);
        } finally {
            rl.unlock();
        }
    }

}
