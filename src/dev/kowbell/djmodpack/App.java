package dev.kowbell.djmodpack;

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;

public class App {
    private boolean ready = false;
    private JButton buttonLaunch;
    private String buttonLaunchDefaultText = "Launch!";
    private String buttonLaunchDefaultUnreadyText = "Uhh, wait one sec...";
    private JPanel panelMain;
    private JTextArea statusText;
    private JProgressBar progressBar;

    public App() {
        buttonLaunch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "jk lol, i cant launch minecraft for you. you need to open the launcher.");
            }
        });

    }

    public void ToggleReady(boolean inReady, String unreadyText) {
        ready = inReady;
        buttonLaunch.setEnabled(ready);

        if (ready) {
            buttonLaunch.setText(buttonLaunchDefaultText);
        } else {
            buttonLaunch.setText(unreadyText != null ? unreadyText : buttonLaunchDefaultUnreadyText);
        }
    }

    public void PresentText(String inText) {
        statusText.setText(inText);
    }

    public void UpdateProgressBar(float value) {
        if (value < 0)
            progressBar.setVisible(false);
        else
            progressBar.setValue((int)value);
    }

    public static void UpdateProgressStatic(double value) {
        instance.UpdateProgressBar((float)value);
    }

    private static App instance = null;
    public static App getInstance() {
        return instance;
    }


    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }


        JFrame frameMain = new JFrame("DJModpack2 Auto-Updater!");
        instance = new App();

        frameMain.setContentPane(instance.panelMain);
        frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frameMain.pack();
        frameMain.setSize(1280, 720);
        frameMain.setLocationRelativeTo(null);
        frameMain.setVisible(true);


        new UpdateWorker().execute();

    }

    public static int GetRamGb() {
        try {
            long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
            float memoryGb = memorySize / 1024f / 1024f / 1024f;

            System.out.printf("DETECTED RAM: %.2f GB\n", memoryGb);
            if (memoryGb > 7.5f)
                return 8;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 6;
    }

}
