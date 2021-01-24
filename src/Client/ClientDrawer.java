package Client;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientDrawer implements Runnable {
    private int menu_status;
    private Socket cs;
    private ClientStatus status;
    private DataOutputStream out;

    public ClientDrawer(Socket cs, ClientStatus status) {
        menu_status = 0;
        this.status = status;
        this.cs = cs;
    }


    public void menu_draw() {
        switch (this.menu_status) {
            case 0:
                System.out.println("1 - Log In\n2 - Registar\n0 - Sair");
                break;

            case 1: // está logged
                if(this.status.isSpecial())
                    System.out.println("1 - Atualizar Localização\n2 - Consultar Localização\n3 - Informar Estado Covid\n4 - Consultar Localização\n5 - Descarregar Mapa\n6 - Consultar Mapa\n0 - Logout");
                else
                    System.out.println("1 - Atualizar Localização\n2 - Consultar Localização\n3 - Informar Estado Covid\n4 - Consultar Localização\n0 - Logout");

                break;

            case 2: // está infetado
                System.out.println("1 - Informar Estado Covid\n0 - Logout");
                break;
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
                menu_three_output();
                break;
            default:
                break;
        }
    }

    public void menu_one_output() throws IOException, InterruptedException {
        int option = this.readOpt();

        switch (option) {
            case 0:
                server_request("EXIT");
                break;
            case 1:
                menu_one_login();
                break;
            case 2:
                menu_one_signup();
                break;
            default: {
                System.out.println("Por favor insira um número das opeções dadas");
                menu_one_output();
                break;
            }
        }
    }

    public void menu_two_output() { // LOGGED IN
        int option = this.readOpt();

        switch (option) {
            case 0:
                menu_two_logout();
                break;
            case 1:
                menu_two_update();
                break;
            case 2:
                menu_two_zoneConsult();
                break;
            case 3:
                menu_two_covidState();
                break;
            case 4:
                menu_two_zoneConsultNotify();
                break;
            case 5:
                if(this.status.isSpecial()) {
                    menu_two_mapDownload();
                    break;
                }
            case 6:
                if(this.status.isSpecial()){
                    menu_two_consulta();
                    break;
                }
            default: {
                System.out.println("Por favor insira um número das opções dadas");
                menu_two_output();
            }
        }
    }

    public void menu_three_output() {
        int option = this.readOpt();

        switch (option) {
            case 0:
                menu_two_logout();
                break;
            case 1:
                menu_two_covidState();
                break;
            default:
                System.out.println("Por favor insira um número das opeções dadas");
                menu_three_output();
        }
    }

    public void menu_one_login() throws IOException, InterruptedException {
        String username, password;
        Scanner is = new Scanner(System.in);

        System.out.print("Username: ");
        username = is.nextLine();
        if(username.isEmpty()) return;

        System.out.print("Password: ");
        password = is.nextLine();

        String result = String.join(";", "LOGIN", username, password);
        this.server_request(result);

        if (this.status.getLogin()) {
            //checkUserMsg(username); // para fazer os avisos
            this.menu_status++;
        }

    }

    public void menu_one_signup() throws IOException, InterruptedException {
        String username, password, zona;
        Scanner is = new Scanner(System.in);

        System.out.print("Username: ");
        username = is.nextLine();
        if (username.isEmpty()) return;

        System.out.print("Password: ");
        password = is.nextLine();

        server_request("WRITEMAP");
        System.out.print("Zona: ");
        zona = is.nextLine().toUpperCase();
        while (zona.isEmpty()) {
            System.out.println("Zona Inválida!");
            System.out.print("Zona: ");
            zona = is.nextLine().toUpperCase();
        }

        String result = String.join(";", "REGISTER", username, password, zona);
        this.server_request(result);

    }

    public void menu_two_logout() {
        try {
            server_request("LOGOUT");
            this.menu_status = 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void menu_two_update() {
        try {
            server_request("WRITEMAP");
            Scanner is = new Scanner(System.in);
            System.out.print("Zona: ");
            String loc = is.nextLine().toUpperCase();
            if(loc.isEmpty()) return;
            String result = String.join(";", "CHANGEZONE", loc);
            this.server_request(result);

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void menu_two_zoneConsult(){
        try {
            server_request("WRITEMAP");
            Scanner is = new Scanner(System.in);
            System.out.print("Zona: ");
            String loc = is.nextLine().toUpperCase();
            if(loc.isEmpty()) return;
            String result = String.join(";","CONSULTZONE",loc);
            this.server_request(result);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void menu_two_consulta() {
        try {
            server_request("CONSULTMAP");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void menu_two_covidState() {
        try {
            Scanner is = new Scanner(System.in);
            System.out.println("Está infetado? (S/N)");
            String inp = is.nextLine().toUpperCase();
            String state;
            if (inp.equals("S")) {
                state = "TRUE";
            } else {
                state = "FALSE";
            }
            String result = String.join(";", "INFORMSTATE", state);
            this.server_request(result);

            if (this.status.getState()) {
                this.menu_status = 2;
            } else this.menu_status = 1;

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void menu_two_zoneConsultNotify() {
        try {
            server_request("WRITEMAP");
            Scanner is = new Scanner(System.in);
            System.out.print("Zona: ");
            String loc = is.nextLine().toUpperCase();
            String result = String.join(";", "CONSULTZONENOTIFY", loc);
            server_request(result);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void menu_two_mapDownload(){
        try {
            server_request("DOWNLOADPLZ");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }


    public void server_request(String msg) throws IOException, InterruptedException {
        this.out.writeUTF(msg);
        this.out.flush();
        this.status.waitForResponse();
    }


    public int readOpt() {
        int option = -1;
        boolean valid = false;
        String msg;
        Scanner is = new Scanner(System.in);

        while (!valid) {
            try {
                msg = is.nextLine();
                option = Integer.parseInt(msg);
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Input inválido. Insira um dígito.\n");
            }
        }

        return option;
    }

    public void run() {
        try {
            this.out = new DataOutputStream(new BufferedOutputStream(cs.getOutputStream()));

            while (!this.status.isExited()) {
                if (this.status.getLogin() && this.status.getState()) {
                    this.menu_status = 2;
                } else if (this.status.getLogin())
                    this.menu_status = 1;
                menu_draw();
                read_menu_output();
            }
            System.out.println("Exiting ...");
        } catch (IOException | InterruptedException e) {
            System.out.println("error");
            e.printStackTrace();

        }
    }
}
