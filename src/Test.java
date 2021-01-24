import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
                LocalDateTime now = LocalDateTime.now();
                System.out.println("["+ dtf.format(now)+"]");
            }
        }

