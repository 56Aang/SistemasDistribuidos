package Server;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String user;
    private String password;
    private List<String> recentlyWith;
    private int pos_x;
    private int pos_y;
    private Boolean isInfected;

    public User(String user, String password, int pos_x, int pos_y, Boolean isInfected){
        this.user = user;
        this.password = password;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.isInfected = isInfected;
        this.recentlyWith = new ArrayList<>();
    }

    public User(String user, String pw){
        this.user = user;
        this.password = pw;
        this.recentlyWith = new ArrayList<>();
        this.pos_x = this.pos_y = -1;
        this.isInfected = false;
    }

    public void moveTo(int x, int y){
        this.pos_x = x;
        this.pos_y = y;
    }

    public String getPassword(){
        return this.password;
    }

    public boolean authenticate(String pw){
        return (this.password.equals(pw));
    }

    public void setInfected(boolean infected){
        this.isInfected = infected;
    }

    public boolean isInfected(){
        return this.isInfected;
    }

    public void addRecent(String user){
        this.recentlyWith.add(user);
    }

    public String getUser(){
        return this.user;
    }

    public void updatePos(int x, int y){
        this.pos_x = x;
        this.pos_y = y;
    }

    public int getX(){
        return this.pos_x;
    }
    public int getY(){
        return this.pos_y;
    }



}
