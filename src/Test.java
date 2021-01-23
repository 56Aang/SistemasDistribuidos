import Server.HistoricParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        //HistoricParser.inicializa(5);
        //HistoricParser.addC("oi",'A',false);
        //HistoricParser.addC("oia",'b',false);
        //HistoricParser.addC("oib",'c',false);
        //HistoricParser.addC("oic",'A',false);
        //HistoricParser.addC("oid",'A',false);
        //HistoricParser.addC("oi",'B',false);
        //HistoricParser.addC("oi",'A',true);
        String nameFile = "src/" + HistoricParser.geraNome() + ".txt";
        try {
            BufferedWriter hw = new BufferedWriter(new FileWriter(nameFile));
            hw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
