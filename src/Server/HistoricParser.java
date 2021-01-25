package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class HistoricParser {
    private static final Lock l = new ReentrantLock();


    /**
     * Método para inicializar o ficheiro historico.txt.
     */
    public static void inicializa() {
        try {
            l.lock();
            BufferedWriter hw = new BufferedWriter(new FileWriter("src/historico.txt"));
            hw.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            l.unlock();
        }
    }

    /**
     * Método para atualizar estado do utilizador no ficheiro de logs.
     *
     * @param user String com o nome do utilizador.
     * @param zone   String com a zona do utilizador.
     * @param isInfected boolean com o estado de infeção do utilizador.
     * @param toDelete boolean com informação para apagar historico do utilizador.
     */
    public static void addC(String user, char zone, boolean isInfected, boolean toDelete) {
        try {
            l.lock();
            boolean exists = false;
            BufferedReader hr = new BufferedReader(new FileReader("src/historico.txt"));
            String line;
            StringBuilder inputBuffer = new StringBuilder();

            while ((line = hr.readLine()) != null) {
                String[] args = line.split(";");
                if (args[0].equals(user)) {
                    if (toDelete) args[1] = "";
                    exists = true;
                    if (!args[1].contains(Character.toString(zone))) // se mudou a zona
                        line = String.join(";", args[0], args[1] + zone, Boolean.toString(isInfected));
                    else if (!args[2].equals(Boolean.toString(isInfected))) // se só mudou o estado
                        line = String.join(";", args[0], args[1], Boolean.toString(isInfected));
                    else return; // não mudou nada
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
        } finally {
            l.unlock();
        }
    }

    /**
     * Método para transformar as informações do ficheiro num mapa.
     *
     * @param N int com a dimensão do mapa.
     * @return String que contém o Mapa com as informações de estatística.
     */
    public static String statisticsMapFile(int N) {
        try {
            l.lock();
            String nameFile = "src/" + geraNome() + ".txt";

            List<String> lines;
            int[][] statistics = new int[N * N][2]; // 0 - not infected, 1 - infected
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

            StringBuilder sb = new StringBuilder();
            for (int i = 0, k = 0; i < N; i++) {
                for (int j = 0; j < N; j++, k++) {
                    sb.append(" | ")
                            .append((char) ('A' + k))
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

            return nameFile;

        } catch (IOException exc) {
            System.out.println(exc.getMessage());
        }finally {
            l.unlock();
        }
        return null;
    }
    /**
     * Método para gerar nome aleatório para o ficheiro.
     *
     * @return String com o nome gerado
     */
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
