package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EstadoPartilhado {
    ReadWriteLock wl;
    ReadWriteLock rl;
    private Map<String, User> users;
    private int[][] mapa;


    public EstadoPartilhado() {
        this.wl = new ReentrantReadWriteLock();
        this.rl = new ReentrantReadWriteLock();
        this.users = new HashMap<>();
        this.mapa = new int[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                this.mapa[i][j] = 0;
            }
        }
    }

    public EstadoPartilhado(int N) {
        this.wl = new ReentrantReadWriteLock();
        this.rl = new ReentrantReadWriteLock();
        this.users = new HashMap<>();
        this.mapa = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.mapa[i][j] = 0;
            }
        }
    }

    public String mapConsult(){
        try {
            rl.readLock().lock();
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
            rl.readLock().unlock();
        }
    }

    public boolean changeZone(String user, char toWhere) {
        try {
            wl.writeLock().lock();

            if (this.users.containsKey(user)) {
                int n = this.mapa.length;
                int xinit = this.users.get(user).getX();
                int yinit = this.users.get(user).getY();
                int i = getZonaX(toWhere);
                //int i = (toWhere - 'A') / n;
                int j = getZonaY(toWhere);
                //int j = (toWhere - 'A') % n;
                if (i >= n || j >= n || (xinit == i && yinit == j)) return false;
                this.users.get(user).moveTo(i, j);
                this.mapa[xinit][yinit]--;
                atualizaUsers(i,j,user);


                return true;
            }
            System.out.println("ups");
            return false;

        } finally {
            wl.writeLock().unlock();
        }
    }

    public String writeMap() {
        try {
            rl.readLock().lock();
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
            rl.readLock().unlock();
        }

    }

    public Boolean validaLogin(String user, String password) {
        try {
            rl.readLock().lock();
            return this.users.get(user).getPassword().equals(password);

        } finally {
            rl.readLock().unlock();
        }
    }

    public User getUser(String user) {
        try {
            rl.readLock().lock();
            return this.users.get(user);
        } finally {
            rl.readLock().unlock();
        }
    }

    public boolean logIn(String user, String pw) {
        try {
            rl.readLock().lock();
            if (this.users.containsKey(user) && this.users.get(user).authenticate(pw)) {
                return true;
            }
            return false;
        } finally {
            rl.readLock().unlock();
        }

    }

    public char getZone(int x, int y){ // é preciso verificar
        return ((char)('A' + this.mapa.length * x + y));
    }

    public int getZonaX(char zona){
        int i = (zona - 'A') / this.mapa.length;
        if(i >= this.mapa.length) return -1;
        return i;
    }

    public int getZonaY(char zona){
        int j = (zona - 'A') % this.mapa.length;
        if(j >= this.mapa.length) return -1;
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
            wl.writeLock().lock();
            if (!this.users.containsKey(user)) {
                int x = getZonaX(zona);
                int y = getZonaY(zona);
                if(x == -1 || y == -1) return false;
                this.users.put(user, new User(user, pw,x,y,false));
                atualizaUsers(x,y,user);


                return true;
            }
            return false;
        } finally {
            wl.writeLock().unlock();
        }
    }

    private void atualizaUsers(int x, int y,String user){
        this.mapa[x][y]++;

        for(User u : this.users.values()){
            if(u.getX() == x && u.getY() == y && !u.getUser().equals(user)){ // estão na mesma zona
                this.users.get(user).addRecent(u.getUser()); // vvv
                this.users.get(u.getUser()).addRecent(user); // update nos 2
            }
        }
    }

    public void setInfected(String user,boolean state){
        try{
            wl.writeLock().lock();
            this.users.get(user).setInfected(state);

        }finally {
            wl.writeLock().unlock();
        }
    }

}
