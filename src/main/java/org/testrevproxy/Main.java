package org.testrevproxy;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    /*public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }
    }*/
    // Nom du fichier properties
    private static final String PROPERTIES_FILE = "commands.properties";

    // Listes pour stocker les commandes et leurs titres
    private static List<String> commandPaths = new ArrayList<>();
    private static List<String> commandTitles = new ArrayList<>();

    public static void main(String[] args) {
        // Vérifier si le system tray est supporté
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray n'est pas supporté");
            return;
        }

        // Charger les commandes depuis le fichier properties
        loadCommands();

        // Création de l'application Swing
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadCommands() {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            props.load(input);

            int index = 1;
            while (true) {
                String cmdPath = props.getProperty("commande" + index + ".cmd");
                String cmdTitle = props.getProperty("commande" + index + ".titre");

                if (cmdPath == null || cmdTitle == null) {
                    break; // Plus de commandes à traiter
                }

                commandPaths.add(cmdPath);
                commandTitles.add(cmdTitle);

                index++;
            }

            System.out.println("Chargement réussi: " + commandPaths.size() + " commandes trouvées");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier properties: " + e.getMessage());
        }
    }

    private static void createAndShowGUI() throws AWTException {
        // Créer le menu popup
        PopupMenu popup = new PopupMenu();

        // Créer l'icône pour le system tray
        Image image = null;//Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/icon.png"));
        // Si vous n'avez pas d'icône, vous pouvez en créer une
        if (image == null) {
            image = createDefaultImage();
        }

        // Créer le TrayIcon
        TrayIcon trayIcon = new TrayIcon(image, "Application System Tray");
        trayIcon.setImageAutoSize(true);

        // Obtenir l'instance de SystemTray
        SystemTray tray = SystemTray.getSystemTray();

        // Ajouter les commandes au menu
        for (int i = 0; i < commandPaths.size(); i++) {
            final String batFile = commandPaths.get(i);
            final String commandName = commandTitles.get(i);

            MenuItem commandItem = new MenuItem(commandName);
            commandItem.addActionListener(e -> executeBatFile(batFile));
            popup.add(commandItem);
        }

        // Ajouter un séparateur
        popup.addSeparator();

        // Ajouter l'option pour quitter
        MenuItem exitItem = new MenuItem("Quitter");
        exitItem.addActionListener(e -> System.exit(0));
        popup.add(exitItem);

        // Ajouter le popup au trayIcon
        trayIcon.setPopupMenu(popup);

        // Ajouter le trayIcon au SystemTray
        tray.add(trayIcon);
    }

    private static void executeBatFile(String batFilePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", batFilePath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Si vous voulez attendre que le processus se termine
            // int exitCode = process.waitFor();
            // System.out.println("Le processus s'est terminé avec le code: " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
            displayMessage("Erreur", "Impossible d'exécuter le fichier: " + batFilePath, TrayIcon.MessageType.ERROR);
        }
    }

    private static void displayMessage(String caption, String text, TrayIcon.MessageType messageType) {
        for (TrayIcon trayIcon : SystemTray.getSystemTray().getTrayIcons()) {
            trayIcon.displayMessage(caption, text, messageType);
        }
    }

    private static Image createDefaultImage() {
        // Créer une image par défaut (16x16 pixels)
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(4, 4, 8, 8);
        g2d.dispose();
        return image;
    }
}