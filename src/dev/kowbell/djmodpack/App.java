package dev.kowbell.djmodpack;

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App {
    private boolean ready = false;
    private JButton buttonLaunch;
    private String buttonLaunchDefaultText = "Launch!";
    private String buttonLaunchDefaultUnreadyText = "Uhh, wait one sec...";
    private JPanel panelMain;
    private JTextPane statusText;
    private JProgressBar progressBar;

    public App() {
        buttonLaunch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Good job, DJ!");
            }
        });

//        btnCheckUpdate.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                LocalMinecraftInstall.GetModpackProfile();
//
//            }
//        });

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


//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            System.out.println("Couldn't set look and feel to system default!");
//        }


        JFrame frameMain = new JFrame("DJ's App");
        instance = new App();

        frameMain.setContentPane(instance.panelMain);
        frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frameMain.pack();
        frameMain.setSize(640, 480);
        frameMain.setLocationRelativeTo(null);
        frameMain.setVisible(true);




        new UpdateWorker().execute();

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
