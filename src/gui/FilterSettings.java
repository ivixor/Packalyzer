package gui;

import pcap.Settings;
import stat.RegionalHostParser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

public class FilterSettings {
    private JCheckBox TCPCheckBox;
    private JCheckBox UDPCheckBox;
    private JCheckBox regionalOnlyCheckBox;
    private JCheckBox hostsOnlyCheckBox;
    private JCheckBox portsOnlyCheckBox;
    private JTextArea textArea1;
    private JTextArea textArea2;
    public JPanel panel1;
    private JButton OKButton;
    private JButton cancelButton;
    private JButton updateButton;

    public FilterSettings() {

        hostsOnlyCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                textArea1.setEnabled(hostsOnlyCheckBox.isSelected());
            }
        });

        portsOnlyCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                textArea2.setEnabled(portsOnlyCheckBox.isSelected());
            }
        });

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.TCP_ON = TCPCheckBox.isSelected();
                Settings.UDP_ON = UDPCheckBox.isSelected();
                Settings.REGIONAL_ONLY = regionalOnlyCheckBox.isSelected();
                Settings.HOSTS_ONLY = hostsOnlyCheckBox.isSelected();
                if (textArea1.isEnabled() && !textArea1.getText().isEmpty()) {
                    String[] rows = textArea1.getText().split("\n");
                    for (String row : rows) {
                        if (!row.isEmpty()) {
                            Settings.hosts.add(row);
                        }
                    }
                }

                Settings.PORTS_ONLY = portsOnlyCheckBox.isSelected();
                if (textArea2.isEnabled() && !textArea2.getText().isEmpty()) {
                    String[] rows = textArea2.getText().split("\n");
                    for (String row : rows) {
                        if (!row.isEmpty()) {
                            Settings.ports.add(row);
                        }
                    }
                }

                Packalyzer.frame2.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Packalyzer.frame2.setVisible(false);
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    RegionalHostParser.update();
                    JOptionPane.showMessageDialog(new JFrame(), "Data has been updated successfully.", "Update regional IP-address range", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(new JFrame(), "Cannot update. An error occurred.", "Update regional IP-address range", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }
}
