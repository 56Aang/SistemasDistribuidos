package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class User {
    private final ReadWriteLock l = new ReentrantReadWriteLock();
    private final Lock rl = l.readLock();
    private final Lock wl = l.writeLock();
    private String user;
    private String password;
    private List<String> recentlyWith;
    private int pos_x;
    private int pos_y;
    private Boolean isInfected;
    private List<String> msgs;

    public User(String user, String password, int pos_x, int pos_y, Boolean isInfected) {
        this.user = user;
        this.password = password;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.isInfected = isInfected;
        this.recentlyWith = new ArrayList<>();
        this.msgs = new ArrayList<>();
    }

    public User(String user, String pw) {
        this.user = user;
        this.password = pw;
        this.recentlyWith = new ArrayList<>();
        this.pos_x = this.pos_y = -1;
        this.isInfected = false;
        this.msgs = new ArrayList<>();
    }

    public void moveTo(int x, int y) {
        wl.lock();
        try {
            this.pos_x = x;
            this.pos_y = y;
        } finally {
            wl.unlock();
        }
    }

    public String getPassword() {
        rl.lock();
        try {
            return this.password;
        } finally {
            rl.unlock();
        }
    }

    public boolean authenticate(String pw) {
        rl.lock();
        try {
            return (this.password.equals(pw));
        } finally {
            rl.unlock();
        }
    }

    public void setInfected(boolean infected) {
        wl.lock();
        try {
            this.isInfected = infected;
        } finally {
            wl.unlock();
        }
    }

    public boolean isInfected() {
        rl.lock();
        try {
            return this.isInfected;
        } finally {
            rl.unlock();
        }
    }

    public void clearRecentlyWith() {
        wl.lock();
        try {
            this.recentlyWith = new ArrayList<>();
        } finally {
            wl.unlock();
        }
    }

    public void removeRecentlyWith(String user) {
        wl.lock();
        try {
            this.recentlyWith.remove(user);
        } finally {
            wl.unlock();
        }
    }

    public void addRecent(String user) {
        wl.lock();
        try {
            this.recentlyWith.add(user);
        } finally {
            wl.unlock();
        }
    }

    public String getUser() {
        rl.lock();
        try {
            return this.user;
        } finally {
            rl.unlock();
        }
    }

    public void updatePos(int x, int y) {
        wl.lock();
        try {
            this.pos_x = x;
            this.pos_y = y;
        } finally {
            wl.unlock();
        }
    }

    public List<String> getRecentlyWith() {
        rl.lock();
        try {
            return new ArrayList<>(this.recentlyWith);
        } finally {
            rl.unlock();
        }

    }

    public int getX() {
        rl.lock();
        try {
            return this.pos_x;
        } finally {
            rl.unlock();
        }
    }

    public int getY() {
        rl.lock();
        try {
            return this.pos_y;
        } finally {
            rl.unlock();
        }
    }

    public void addMsg(String msg) {
        wl.lock();
        try {
            this.msgs.add(msg);
        } finally {
            wl.unlock();
        }
    }

    public void clearMsgs() {
        wl.lock();
        try {
            this.msgs = new ArrayList<>();
        } finally {
            wl.unlock();
        }
    }

    public boolean hasMsgs() {
        rl.lock();
        try {
            return !this.msgs.isEmpty();
        } finally {
            rl.unlock();
        }
    }

    public List<String> getMsgs() {
        rl.lock();
        try {
            return new ArrayList<>(this.msgs);
        } finally {
            rl.unlock();
        }
    }

}
