package gui;

import pcap.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CaptureSettings {
    private JTextField textField1;
    private JTextField textField2;
    private JCheckBox promiscousCheckBox;
    private JButton OKButton;
    private JButton cancelButton;
    public JPanel panel1;
    private JTextField textField3;
    private JTextField textField4;
    private JCheckBox stopAfterCapturingTimeCheckBox;
    private JCheckBox stopAfterPacketsAmountCheckBox;
    private JButton defaultButton;

    public CaptureSettings() {

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Settings.PROMISCUOUS_MODE = promiscousCheckBox.isSelected();
                Settings.TIME_LIMIT_ON = stopAfterCapturingTimeCheckBox.isSelected();
                Settings.PACKET_LIMIT_ON = stopAfterPacketsAmountCheckBox.isSelected();

                try {
                    if (!textField1.getText().isEmpty()) {
                        int snaplen = Integer.parseInt(textField1.getText());
                        if (snaplen >= 1 && snaplen <= 65536)
                            Settings.SNAPLEN = snaplen;
                        else {
                            JOptionPane.showMessageDialog(new JFrame(), "Snaplen must be between 1 and 65536.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    if (!textField2.getText().isEmpty()) {
                        int timeout = Integer.parseInt(textField2.getText());
                        if (timeout >= 0) {
                            Settings.TIMEOUT = timeout;
                        } else {
                            JOptionPane.showMessageDialog(new JFrame(), "Negative value", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    if (!textField3.getText().isEmpty()) {
                        int time_limit = Integer.parseInt(textField3.getText());
                        if (time_limit > 0) {
                            Settings.TIME_LIMIT = time_limit;
                        } else {
                            JOptionPane.showMessageDialog(new JFrame(), "Negative value", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    if (!textField4.getText().isEmpty()) {
                        int packet_limit = Integer.parseInt(textField4.getText());
                        if (packet_limit > 0) {
                            Settings.PACKET_NUM = packet_limit;
                        } else {
                            JOptionPane.showMessageDialog(new JFrame(), "Negative value", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                    JOptionPane.showMessageDialog(new JFrame(), "Numbers only", "Error", JOptionPane.ERROR_MESSAGE);
                }

                Packalyzer.frame3.setVisible(false);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Packalyzer.frame3.setVisible(false);
            }
        });

        stopAfterPacketsAmountCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                textField4.setEnabled(stopAfterPacketsAmountCheckBox.isSelected());
            }
        });

        stopAfterCapturingTimeCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                textField3.setEnabled(stopAfterCapturingTimeCheckBox.isSelected());
            }
        });

        defaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textField4.setEnabled(false);
                textField4.setText("" + 10000);
                stopAfterPacketsAmountCheckBox.setSelected(true);
                Settings.PACKET_LIMIT_ON = true;
                Settings.PACKET_NUM = 10000;
                textField3.setEnabled(false);
                textField3.setText("");
                stopAfterCapturingTimeCheckBox.setSelected(false);
                Settings.TIME_LIMIT_ON = false;
                Settings.TIME_LIMIT = 0;
                textField2.setText("" + 1000);
                Settings.TIMEOUT = 1000;
                textField1.setText("" + 65536);
                Settings.SNAPLEN = 65536;

                promiscousCheckBox.setSelected(true);
                Settings.PROMISCUOUS_MODE = true;
            }
        });
    }
}
