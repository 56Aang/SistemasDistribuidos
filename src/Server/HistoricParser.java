package Server;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HistoricParser {
    private final Lock l = new ReentrantLock();

    public static void inicializa(int n) {
        try {
            BufferedWriter hw = new BufferedWriter(new FileWriter("src/historico.txt"));
            hw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addC(String user, char zone, boolean isInfected, boolean toDelete) {
        try {
            boolean exists = false;
            BufferedReader hr = new BufferedReader(new FileReader("src/historico.txt"));
            String line;
            StringBuilder inputBuffer = new StringBuilder();

            while ((line = hr.readLine()) != null) {
                String[] args = line.split(";");
                if (args[0].equals(user)) {
                    if(toDelete) args[1] = "";
                    exists = true;
                    if (!args[1].contains(Character.toString(zone)))
                        line = String.join(";", args[0], args[1] + zone, Boolean.toString(isInfected));
                    else if (!args[2].equals(Boolean.toString(isInfected)))
                        line = String.join(";", args[0], args[1], Boolean.toString(isInfected));
                    else return;
                }
                inputBuffer.append(line);
                inputBuffer.append('\n');

            }
            if (!exists) {
                line = String.join(";", user, Character.toString(zone), Boolean.toString(isInfected));
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            hr.close();
            FileOutputStream fileOut = new FileOutputStream("src/historico.txt");
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String statisticsMapFile(int N) {
        String nameFile = "src/" + geraNome() + ".txt";

        List<String> lines;
        int[][] statistics = new int[N * N][2]; // 0 - not infected, 1 - infected
        try {
            BufferedWriter hw = new BufferedWriter(new FileWriter(nameFile));
            hw.close();

            lines = Files.readAllLines(Paths.get("src/historico.txt"), StandardCharsets.UTF_8);
            String[] args;
            for (String s : lines) {
                args = s.split(";");
                int infected = (args[2].equals("true")) ? 1 : 0;
                for (int i = 0; i < args[1].length(); i++) {
                    statistics[args[1].charAt(i) - 'A'][infected]++;
                }
            }
//| A - I:(numero); NI:(numero) | B - 0 | C - 0 | D - 0 | E - 0 |

            StringBuilder sb = new StringBuilder();
            for (int i=0,k = 0; i < N; i++) {
                for (int j = 0; j < N; j++,k++) {
                    sb.append(" | ")
                            .append((char)('A' + k))
                            .append(" - ")
                            .append("I:")
                            .append(statistics[k][1])
                            .append("; NI:")
                            .append(statistics[k][0]);
                }
                sb.append(" |").append('\n');
            }

            FileOutputStream fileOut = new FileOutputStream(nameFile);
            fileOut.write(sb.toString().getBytes());
            fileOut.close();

        } catch (IOException exc) {
            System.out.println(exc.getMessage());
        }
        return nameFile;
    }

    public static String geraNome() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 6;
        Random random = new Random();

        return (random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString());
    }

}
