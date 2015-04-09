package gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HighlightColor {
    public JPanel panel1;
    private JButton OKButton;
    private JButton cancelButton;
    private JColorChooser colorChooser;

    private Color color;

    public HighlightColor() {

        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                color = colorChooser.getColor();
            }
        });

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Packalyzer.tableColor = color;
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Packalyzer.frame4.setVisible(false);
            }
        });
    }

}
