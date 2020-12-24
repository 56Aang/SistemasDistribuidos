package Server;

import java.util.HashMap;
import java.util.Map;

public class EstadoPartilhado {

    private Map<String, User> users;
    private int[][] mapa;


    public EstadoPartilhado(){
        this.users = new HashMap<>();
        this.mapa = new int[5][5];
    }

    public EstadoPartilhado(int N){
        this.users = new HashMap<>();
        this.mapa = new int[N][N];
    }

    public Boolean validaLogin(String user, String password){
        return this.users.get(user).getPassword().equals(password);
    }

    public User getUser(String user){
        return this.users.get(user);
    }

    public boolean logIn(String user, String pw){
        synchronized (users){
            if(this.users.containsKey(user) && this.users.get(user).authenticate(pw)){
                return true;
            }
        }
        return false;
    }

    /**
     * Método para registar um utilizador.
     * @param user String com o nome do utilizador.
     * @param pw String com a password do utilizador.
     * @return
     */

    public boolean registerClient(String user, String pw) {
        synchronized (users){
            if(!this.users.containsKey(user)){
                this.users.put(user, new User(user, pw));
                return true;
            }
        }
        return false;
    }

}
