package gui;

import com.sun.javafx.tk.quantum.PaintRenderJob;
import pcap.PacketCapture;
import pcap.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class StartCapture {

    public JPanel panel1;
    private JComboBox comboBox1;
    private JButton OKButton;
    private JButton cancelButton;
    private JButton captureSettingsButton;

    Thread t = null;

    public StartCapture() {

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    start();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Packalyzer.frame1.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Packalyzer.frame1.setVisible(false);
            }
        });

        captureSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Packalyzer.frame3.setVisible(true);
            }
        });
    }

    public void createUIComponents() {
        String[] devices = PacketCapture.findDevices();
        if (devices != null) {
            comboBox1 = new JComboBox(devices);
        }
    }

    public void start() throws IOException {
        Settings.DEVICE = comboBox1.getSelectedIndex();

        PacketCapture.running = true;
        System.out.println("start thread");
        PacketCapture pc = new PacketCapture();
        t = new Thread(pc);
        t.start();
    }

    public void stop() throws IOException {
        System.out.println("stop thread");
        PacketCapture.running = false;
        //PacketCapture.stopCapture();
        if (t != null) {
            try {
                t.join();
            } catch (InterruptedException e1) { e1.printStackTrace(); }
        }
    }
}
