/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package MizanRsync;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                try {

                    //String command = "cmd.exe /c wsl sshpass -p '{{password}}' rsync -avzP /mnt/d/test/ mulyadi@103.226.139.97:/home/mulyadi/";
                    String command = "cmd.exe /c wsl sshpass -p '{{password}}' rsync -avzP {{localPath}} {{username}}@{{host}}:{{serverPath}}";
                    command = command.replace("{{password}}", password)
                            .replace("{{localPath}}", localPath)
                            .replace("{{username}}", username)
                            .replace("{{host}}", host)
                            .replace("{{serverPath}}", serverPath);

                    System.out.println(command);
                    
                    Runtime rt = Runtime.getRuntime();
                    Process p = rt.exec(command);

                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String result = br.lines().collect(Collectors.joining(System.lineSeparator()));
                    System.out.println(result);
                    br.close();

                    BufferedReader brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String resultError = brError.lines().collect(Collectors.joining(System.lineSeparator()));
                    System.out.println(resultError);
                    brError.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        Timer t = new Timer();
        t.scheduleAtFixedRate(tt, 1000 * 10, 1000 * interval);

    }

}
