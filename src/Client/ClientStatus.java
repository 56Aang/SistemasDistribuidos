package Client;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientStatus {
    private boolean login;
    private boolean waitingForResponse;
    private boolean exited;
    private boolean isInfected;
    private boolean isSpecial;
    private final Lock l = new ReentrantLock();
    private final Condition condLog = l.newCondition();


    public ClientStatus() {
        this.login = false;
        this.waitingForResponse = false;
        this.exited = false;
        this.isInfected = false;
    }

    public boolean isSpecial() {
        l.lock();
        try {
            return this.isSpecial;
        } finally {
            l.unlock();
        }
    }

    public void setSpecial(boolean special) {
        l.lock();
        try {
            this.isSpecial = special;
        } finally {
            l.unlock();
        }
    }

    public synchronized void login() {
        this.login = true;
    }

    public synchronized void logout() {
        this.login = false;
    }

    public synchronized boolean getLogin() {
        return this.login;
    }

    public boolean getWaiting() {
        try {
            l.lock();
            return this.waitingForResponse;
        } finally {
            l.unlock();
        }
    }

    public void setWaitingOFF() {
        l.lock();
        try {
            this.waitingForResponse = false;
            condLog.signalAll();
        } finally {
            l.unlock();
        }
    }

    public void waitForResponse() throws InterruptedException {
        l.lock();
        try {
            this.waitingForResponse = true;
            while (this.waitingForResponse) {
                condLog.await();
            }
        } finally {
            l.unlock();
        }
    }

    public synchronized void exited() {
        this.exited = true;
    }

    public synchronized boolean isExited() {
        return this.exited;
    }

    public synchronized void setInfected(boolean state) {
        this.isInfected = state;
    }

    public synchronized boolean getState() {
        return this.isInfected;
    }
}
