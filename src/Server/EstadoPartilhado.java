package Server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EstadoPartilhado {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock wl = l.writeLock();
    private final Lock rl = l.readLock();
    private Map<String, User> users;
    private int[][] mapa;
    private NotificationHandler nh;
    private Map<Character, List<String>> usersNotify;


    public EstadoPartilhado() {
        this.usersNotify = new HashMap<>();
        this.users = new HashMap<>();
        this.mapa = new int[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                this.mapa[i][j] = 0;
            }
        }
    }

    public EstadoPartilhado(NotificationHandler nh) {
        this.usersNotify = new HashMap<>();
        this.users = new HashMap<>();
        this.mapa = new int[5][5];
        this.nh = nh;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                this.mapa[i][j] = 0;
            }
        }
    }

    public EstadoPartilhado(int N) {
        this.users = new HashMap<>();
        this.mapa = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.mapa[i][j] = 0;
            }
        }
    }

    public String mapConsult() {
        try {
            rl.lock();
            StringBuilder sb = new StringBuilder();
            char a = 'A';
            int n = mapa.length;
            sb.append("CONSULTA DE MAPA\n");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sb.append(" | ").append(Character.toString(a + n * i + j)).append(" - ").append(this.mapa[i][j]);
                }
                sb.append(" |").append('\n');
            }
            return sb.toString();
        } finally {
            rl.unlock();
        }
    }

    public String changeZone(String user, char toWhere) throws IOException {
        try {
            wl.lock();

            if (this.users.containsKey(user)) {
                int n = this.mapa.length;
                int xinit = this.users.get(user).getX();
                int yinit = this.users.get(user).getY();
                int i = getZonaX(toWhere);
                //int i = (toWhere - 'A') / n;
                int j = getZonaY(toWhere);
                //int j = (toWhere - 'A') % n;
                if (i >= n || j >= n || (xinit == i && yinit == j)) return "false";
                this.users.get(user).moveTo(i, j);
                atualizaUsers(i, j, user);
                if (--this.mapa[xinit][yinit] == 0)
                    return Character.toString('A' + n * xinit + yinit);

                return "true";
            }
            System.out.println("ups");
            return "false";

        } finally {
            wl.unlock();
        }
    }

    public String writeMap() {
        try {
            rl.lock();
            StringBuilder sb = new StringBuilder();
            char a = 'A';
            int n = mapa.length;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sb.append(" | ").append(Character.toString(a + n * i + j));
                }
                sb.append(" |").append('\n');
            }
            return sb.toString();
        } finally {
            rl.unlock();
        }

    }

    public boolean isZoneValid(char zone) {
        rl.lock();
        try {
            int x = getZonaX(zone);
            int y = getZonaY(zone);
            return (x >= 0 && y >= 0 && x < this.mapa.length && y < this.mapa.length);
        } finally {
            rl.unlock();
        }
    }

    public User getUser(String user) {
        try {
            rl.lock();
            return this.users.get(user);
        } finally {
            rl.unlock();
        }
    }

    public boolean logIn(String user, String pw) {
        try {
            rl.lock();
            if (this.users.containsKey(user) && this.users.get(user).authenticate(pw)) {
                return true;
            }
            return false;
        } finally {
            rl.unlock();
        }

    }

    public char getZone(int x, int y) { // é preciso verificar
        return ((char) ('A' + this.mapa.length * x + y));
    }

    public int getZonaX(char zona) {
        int i = (zona - 'A') / this.mapa.length;
        if (i >= this.mapa.length) return -1;
        return i;
    }

    public int getZonaY(char zona) {
        int j = (zona - 'A') % this.mapa.length;
        if (j >= this.mapa.length) return -1;
        return j;
    }

    /**
     * Método para registar um utilizador.
     *
     * @param user String com o nome do utilizador.
     * @param pw   String com a password do utilizador.
     * @return
     */

    public boolean registerClient(String user, String pw, char zona) {
        try {
            wl.lock();
            if (!this.users.containsKey(user) && isZoneValid(zona)) {
                int x = getZonaX(zona);
                int y = getZonaY(zona);
                if (x == -1 || y == -1) return false;
                this.users.put(user, new User(user, pw, x, y, false));
                atualizaUsers(x, y, user);


                return true;
            }
            return false;
        } finally {
            wl.unlock();
        }
    }

    private void atualizaUsers(int x, int y, String user) {
        wl.lock();
        try {
            this.mapa[x][y]++;

            for (User u : this.users.values()) {
                if (u.getX() == x && u.getY() == y && !u.getUser().equals(user)) { // estão na mesma zona
                    this.users.get(user).addRecent(u.getUser()); // vvv
                    this.users.get(u.getUser()).addRecent(user); // update nos 2
                }
            }
        } finally {
            wl.unlock();
        }
    }

    public void setInfected(String user, boolean state) {
        try {
            wl.lock();
            this.users.get(user).setInfected(state);

        } finally {
            wl.unlock();
        }
    }

    public void notificaInfecao(String user) throws IOException {
        wl.lock();
        try {
            List<String> pInfected = this.users.get(user).getRecentlyWith();
            List<String> usersNotLogged = this.nh.alertInfected(pInfected);
            this.users.get(user).clearRecentlyWith();
            for (String s : pInfected) {
                this.users.get(s).removeRecentlyWith(user);
            }
            for (String s : usersNotLogged) {
                this.users.get(s).addMsg("YOU'VE BEEN IN CONTACT WITH AN INFECTED PERSON");
            }
        } finally {
            wl.unlock();
        }
    }

    public void notificaVaga(char local) throws IOException {
        rl.lock();
        try {
            if(this.usersNotify.containsKey(local)) {
                this.nh.alertFreeZone(new ArrayList<>(this.usersNotify.get(local)),local);
            }
        }finally {
            rl.unlock();
        }

    }

    //public void warnLoggedOutUsers(List<String> users){
    //    for(String s : this.users.keySet()){
    //        if(!users.contains(s)){ // warn
    //            this.users.get(s).addMsg("");
    //        }
    //    }
    //}

    public void addNewHandler(String user, Socket s) {
        wl.lock();
        try {
        this.nh.addClient(user, s);
        }finally {
            wl.unlock();
        }
    }

    public void removeHandler(String user) {
        wl.lock();
        try {
            this.nh.removeClient(user);
        }finally {
            wl.unlock();
        }
    }

    public boolean addNotifyUser(String user, char zone) {
        wl.lock();
        try {
            if (!isZoneValid(zone)) return false;
            this.usersNotify.putIfAbsent(zone,new ArrayList<>());
            this.usersNotify.get(zone).add(user);
            return true;
        } finally {
            wl.unlock();
        }

    }

}
