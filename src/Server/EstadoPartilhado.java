package Server;

import Exceptions.BadZoneException;
import Exceptions.InvalidUserException;
import Exceptions.UserAlreadyExistingException;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Classe partilhada pelas threads relativas ao Servidor.
 */
public class EstadoPartilhado {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock wl = l.writeLock();
    private final Lock rl = l.readLock();
    private Map<String, User> users;
    private int[][] mapa;
    private NotificationHandler nh;
    private Map<Character, List<String>> usersNotify;


    public EstadoPartilhado(NotificationHandler nh) {
        HistoricParser.inicializa();
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

    public EstadoPartilhado(NotificationHandler nh, int N) {
        HistoricParser.inicializa();
        this.usersNotify = new HashMap<>();
        this.users = new HashMap<>();
        this.mapa = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                this.mapa[i][j] = 0;
            }
        }
        this.nh = nh;
    }
    /**
     * Método que retorna tamanho do lado do mapa.
     *
     * @return int
     */
    public int getMapaLength() {
        rl.lock();
        try {
            return this.mapa.length;
        } finally {
            rl.unlock();
        }
    }
    /**
     * Método que retorna mapa com a quantidade de utilizadores sob a forma de uma String.
     *
     * @return String
     */
    public String mapConsult() {
        rl.lock();
        try {
            StringBuilder sb = new StringBuilder();
            char a = 'A';
            int n = mapa.length;
            sb.append("CONSULTA DE MAPA\n");
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    sb.append(" | ").append((char) (a + n * i + j)).append(" - ").append(this.mapa[i][j]);
                }
                sb.append(" |").append('\n');
            }
            return sb.toString();
        } finally {
            rl.unlock();
        }
    }
    /**
     * Método que altera a zona de um dado utilizador.
     *
     * @param user String relativa ao utilizador.
     * @param toWhere char relativa à localização à qual o utilizador se quer mudar.
     * @return String - "true" se foi feita com sucesso, uma zona caso essa tenha ficado vazia
     * @throws BadZoneException
     * @throws InvalidUserException
     */
    public String changeZone(String user, char toWhere) throws BadZoneException, InvalidUserException {
        wl.lock();
        try {

            if (this.users.containsKey(user)) {
                int n = this.mapa.length;
                int xinit = this.users.get(user).getX();
                int yinit = this.users.get(user).getY();
                int i = getZonaX(toWhere);
                //int i = (toWhere - 'A') / n;
                int j = getZonaY(toWhere);
                //int j = (toWhere - 'A') % n;
                if (i >= n || j >= n || i < 0 || j < 0 || (xinit == i && yinit == j)) throw new BadZoneException();
                this.users.get(user).moveTo(i, j);
                atualizaUsers(i, j, user);
                if (this.usersNotify.containsKey(toWhere))
                    this.usersNotify.get(toWhere).remove(user);
                if (--this.mapa[xinit][yinit] == 0)
                    return Character.toString('A' + n * xinit + yinit);

                return "true";
            }
            throw new InvalidUserException();

        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que retorna o Mapa em formato de String.
     *
     * @return String
     */
    public String writeMap() {
        rl.lock();
        try {
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
    /**
     * Método que verifica se uma dada zona existe.
     *
     * @return boolean
     */
    public boolean isZoneValid(char zone) {
        rl.lock();
        try {
            int x = getZonaX(zone);
            int y = getZonaY(zone);
            return !(x < 0 || y < 0 || x >= this.mapa.length || y >= this.mapa.length);
        } finally {
            rl.unlock();
        }
    }
    /**
     * Método que retorna um User dado o username de um utilizador.
     *
     * @param user String relativa ao código de um utilizador.
     * @return User
     */
    public User getUser(String user){
        rl.lock();
        try {
            return this.users.get(user);
        } finally {
            rl.unlock();
        }
    }
    /**
     * Método valida login de um user.
     *
     * @param user String relativa ao código de um utilizador.
     * @param pw String relativa à password de um utilizador.
     * @return boolean
     */
    public boolean logIn(String user, String pw) {
        rl.lock();
        try {
            return this.users.containsKey(user) && this.users.get(user).authenticate(pw);
        } finally {
            rl.unlock();
        }

    }
    /**
     * Método que transforma coordenadas numa zona.
     *
     * @param x int correspondente à linha da zona.
     * @param y int correspondente à coluna da zona.
     * @return char
     */
    public char getZone(int x, int y) { // é preciso verificar
        rl.lock();
        try {
            return ((char) ('A' + this.mapa.length * x + y));
        } finally {
            rl.unlock();
        }
    }
    /**
     * Método que retorna a linha dada por uma zona.
     *
     * @param zona char correspondente à zona.
     * @return int
     */
    public int getZonaX(char zona) {
        rl.lock();
        try {
            int i = (zona - 'A') / this.mapa.length;
            if (i >= this.mapa.length) return -1;
            return i;
        } finally {
            rl.unlock();
        }
    }
    /**
     * Método que retorna a coluna dada por uma zona.
     *
     * @param zona char correspondente à zona.
     * @return int
     */
    public int getZonaY(char zona) {
        rl.lock();
        try {
            int j = (zona - 'A') % this.mapa.length;
            if (j >= this.mapa.length) return -1;
            return j;
        } finally {
            rl.unlock();
        }

    }

    /**
     * Método para registar um utilizador.
     *
     * @param user String com o nome do utilizador.
     * @param pw   String com a password do utilizador.
     * @param zona String com a zona do utilizador.
     * @return boolean
     * @throws BadZoneException
     * @throws UserAlreadyExistingException
     */

    public boolean registerClient(String user, String pw, String zona) throws BadZoneException, UserAlreadyExistingException {
        wl.lock();
        try {
            if (!this.users.containsKey(user)) {
                if (zona.isEmpty() || !isZoneValid(zona.charAt(0))) throw new BadZoneException();
                int x = getZonaX(zona.charAt(0));
                int y = getZonaY(zona.charAt(0));
                if (x == -1 || y == -1) return false;
                this.users.put(user, new User(user, pw, x, y, false, Character.isDigit(user.charAt(0)))); // users começados por digitos são special
                atualizaUsers(x, y, user);
                return true;
            }
            throw new UserAlreadyExistingException(user);
        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que atualiza lista de utilizadores recentes.
     *
     * @param  x    int da linha da zona.
     * @param  y    int da coluna da zona.
     * @param user  String com o nome do utilizador.
     */
    public void atualizaUsers(int x, int y, String user) {
        wl.lock();
        try {
            this.mapa[x][y]++;

            for (User u : this.users.values()) {
                if (u.getX() == x && u.getY() == y && !u.getUser().equals(user) && !this.users.get(user).wasRecentlyWith(u.getUser()) && !u.isInfected()) { // estão na mesma zona
                    this.users.get(user).addRecent(u.getUser()); // vvv
                    this.users.get(u.getUser()).addRecent(user); // update nos 2
                }
            }
        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que atualiza estado de infeção de um utilizador.
     *
     * @param user  String com o nome do utilizador.
     * @param state boolean com o estado de infeção.
     */
    public void setInfected(String user, boolean state) {
        wl.lock();
        try {
            this.users.get(user).setInfected(state);

        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que notifica utilizadores que estão em risco.
     *
     * @param user String com o nome do utilizador.
     * @throws IOException
     */
    public void notificaInfecao(String user) throws IOException {
        wl.lock();
        try {
            List<String> pInfected = this.users.get(user).getRecentlyWith();
            List<String> usersNotLogged = this.nh.alertInfected(pInfected);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            LocalDateTime now = LocalDateTime.now();
            String datetimenow = "["+ dtf.format(now)+"]";
            this.users.get(user).clearRecentlyWith();
            for (String s : pInfected) {
                this.users.get(s).removeRecentlyWith(user);
            }
            for (String s : usersNotLogged) {
                this.users.get(s).addMsg(datetimenow + ": YOU'VE BEEN IN CONTACT WITH AN INFECTED PERSON");
            }
        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que notifica utilizadores que uma localização encontra-se vazia.
     *
     * @param local char com a localização.
     * @throws IOException
     */
    public void notificaVaga(char local) throws IOException {
        rl.lock();
        try {
            List<String> usersNotLogged;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
            LocalDateTime now = LocalDateTime.now();
            String datetimenow = "["+ dtf.format(now)+"]";
            if (this.usersNotify.containsKey(local)) {
                usersNotLogged = this.nh.alertFreeZone(new ArrayList<>(this.usersNotify.get(local)), local);
                for(String s : usersNotLogged){
                    this.users.get(s).addMsg(datetimenow + ": ZONE " + local + " IS FREE");
                }
            }
        } finally {
            rl.unlock();
        }

    }

    /**
     * Método que adiciona novo socket relativo a um utilizador.
     *
     * @param user  String com o nome do utilizador.
     * @param s Socket com o socket do utilizador.
     */
    public void addNewHandler(String user, Socket s) {
        wl.lock();
        try {
            this.nh.addClient(user, s);
        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que remove socket relativo a um utilizador.
     *
     * @param user  String com o nome do utilizador.
     */
    public void removeHandler(String user) {
        wl.lock();
        try {
            this.nh.removeClient(user);
        } finally {
            wl.unlock();
        }
    }
    /**
     * Método que adiciona utilizador para receber notificação de vaga.
     *
     * @param user  String com o nome do utilizador.
     * @param zone char com a zona.
     * @return boolean
     * @throws BadZoneException
     */
    public boolean addNotifyUser(String user, char zone) throws BadZoneException {
        wl.lock();
        try {
            if (!isZoneValid(zone)) throw new BadZoneException();

            this.usersNotify.putIfAbsent(zone,new ArrayList<>());
            if (this.usersNotify.get(zone).contains(user)) return false;
            this.usersNotify.putIfAbsent(zone, new ArrayList<>());
            this.usersNotify.get(zone).add(user);
            return true;
        } finally {
            wl.unlock();
        }

    }
    /**
     * Método que retorna a quantidade de utilizadores numa dada zona.
     *
     * @param zone char com a zona.
     * @return int
     * @throws BadZoneException
     */
    public int zoneConsult(char zone) throws BadZoneException {
        rl.lock();
        try {
            if (!isZoneValid(zone)) throw new BadZoneException();
            return this.mapa[getZonaX(zone)][getZonaY(zone)];
        } finally {
            rl.unlock();
        }
    }

}
