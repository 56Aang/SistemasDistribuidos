package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientDrawer implements Runnable {
    private int menu_status;
    private Socket cs;
    private ClientStatus status;
    private PrintWriter out;

    public ClientDrawer(Socket cs, ClientStatus status) {
        menu_status = 0;
        this.status = status;
        this.cs = cs;
    }


    public void menu_draw() {
        switch (this.menu_status) {
            case 0: {
                System.out.println("1 - Log In | 2 - Registar | 0 - Sair");
                break;
            }
            case 1: {
                System.out.println(" | 4 - Sair");
                break;
            }
        }
    }


    public void read_menu_output() throws IOException, InterruptedException {
        switch (this.menu_status) {
            case 0:
                menu_one_output();
                break;
            case 1:
                menu_two_output();
                break;
            case 2:
                //menu_three_output();
                break;
            default:
                break;
        }
    }

    public void menu_one_output() throws IOException,InterruptedException{
        int option = this.readOpt();

        switch(option){
            case 0:{
                server_request("LOGOUT");
                break;
            }
            case 1:
                menu_one_login();
                break;
            case 2:
                //menu_one_signup();
                break;
            default:{
                System.out.println("Por favor insira um número das opeções dadas");
                menu_one_output();
                break;
            }
        }
    }

    public void menu_two_output() throws IOException,InterruptedException{
        int option = this.readOpt();

        switch(option){
            case 1:
                break;
            case 2:
                break;
                
            default:{
                System.out.println("Por favor insira um número das opeções dadas");
                menu_two_output();
            }
        }
    }

    public void menu_one_login() throws IOException,InterruptedException {
        String username, password;
        Scanner is = new Scanner(System.in);

        System.out.println("Username:");
        username = is.nextLine();
        System.out.println("Password:");
        password = is.nextLine();

        String result = String.join("|", "LOGIN", username, password);
        this.server_request(result);

        if (this.status.getLogin()) {
            //checkUserMsg(username);
            this.menu_status++;
        }

    }

        public void server_request(String msg) throws IOException,InterruptedException{
        this.out.println(msg);
        this.status.waitForResponse();
    }


    public int readOpt(){
        int option = -1;
        boolean valid = false;
        String msg;
        Scanner is = new Scanner(System.in);

        while(!valid){
            try{
                msg = is.nextLine();
                option = Integer.parseInt(msg);
                valid = true;
            }
            catch (NumberFormatException e){
                System.out.println("Input inválido. Insira um dígito.\n");
            }
        }

        return option;
    }

    public void run() {
        try {
            this.out = new PrintWriter(cs.getOutputStream(), true);
            while (!this.status.getLogin()) {
                menu_draw();
                read_menu_output();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("error");
            e.printStackTrace();

        }
    }
}
