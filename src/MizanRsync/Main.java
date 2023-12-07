/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package MizanRsync;

import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author mulyadi
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Properties prop = new Properties();
        try {
            prop.load(new FileReader("config.properties"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        String host = prop.getProperty("rsync.host");
        String username = prop.getProperty("rsync.username");
        String password = prop.getProperty("rsync.password");
        String serverPath = prop.getProperty("rsync.serverpath");
        String localPath = prop.getProperty("rsync.localpath");
        String intervalString = prop.getProperty("rsync.interval");
        int interval = Integer.parseInt(intervalString);

        String os = System.getProperty("os.name");

        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                try {

                    boolean isActiveConnection = new Main().getIsActiveConnection();

                    if (isActiveConnection == false) {

                        String command = "sshpass -p '{{password}}' rsync -avzP {{localPath}} {{username}}@{{host}}:{{serverPath}}";

                        if (os.toLowerCase().contains("windows")) {
                            command = "cmd.exe /c wsl sshpass -p '{{password}}' rsync -avzP {{localPath}} {{username}}@{{host}}:{{serverPath}}";
                        }

                        command = command.replace("{{password}}", password)
                                .replace("{{localPath}}", localPath)
                                .replace("{{username}}", username)
                                .replace("{{host}}", host)
                                .replace("{{serverPath}}", serverPath);

                        String strip = Stream.iterate(0, x -> x + 1).limit(command.length()).map(d -> "-").collect(Collectors.joining());

                        System.out.println("\n");
                        System.out.println(strip);
                        System.out.println(command);
                        System.out.println(strip);

                        Runtime rt = Runtime.getRuntime();
                        Process p = rt.exec(command);

                        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String result = br.lines().collect(Collectors.joining(System.lineSeparator()));
                            System.out.println(result);
                        }

                        try (BufferedReader brError = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                            String resultError = brError.lines().collect(Collectors.joining(System.lineSeparator()));
                            System.out.println(resultError);
                        }
                    }

                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

    }

    public boolean getIsActiveConnection() {
        try {
            URL url = new URL("http://mizancloud.com:8080/biling/user-layanan/check-active-backup-service?kodeCompany=attijaroh");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream is = connection.getInputStream();
            String result = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining(System.lineSeparator()));
            System.out.println(result);
            if (result.contains("1")) {
                return false;
            }

            return true;
        } catch (MalformedURLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
