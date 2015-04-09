package gui;

import au.com.bytecode.opencsv.CSVReader;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import pcap.Settings;
import stat.ChartBuilder;
import stat.RegionalHostParser;
import stat.StatMath;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class Packalyzer {
    private JPanel panel1;
    private JTable table1;
    private JToolBar toolbar;
    private JCheckBox TCPCheckBox;
    private JCheckBox UDPCheckBox;
    private JCheckBox regionalOnlyCheckBox;
    private JCheckBox hostsOnlyCheckBox;
    private JCheckBox portsOnlyCheckBox;
    private JCheckBox errorCheckBox;
    private JButton enableHighlight;
    private JPanel panel3;
    private JButton disableHighlightButton;
    private JPanel panel2;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    private JLabel label5;
    private JLabel label6;
    private JLabel label7;
    private JProgressBar progressBar1;
    private JPanel panel4;
    private JLabel label8;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openItem;
    private JMenuItem exitItem;
    private JMenu editMenu;
    private JMenuItem captureSettingsItem;
    private JMenuItem filterSettingsItem;
    private JMenuItem updateRegionalItem;
    private JMenuItem loadConfigItem;
    private JMenuItem setColorItem;
    private JMenu viewMenu;
    private JCheckBoxMenuItem showToolbarItem;
    private JCheckBoxMenuItem showHighlightPanelItem;
    private JCheckBoxMenuItem showProgressBarItem;
    private JMenu captureMenu;
    private JMenuItem startItem;
    private JMenuItem stopItem;
    private JMenu analyzeMenu;
    private JMenuItem plotItem;
    private JMenuItem protocolStatItem;
    private JMenuItem regionalStatItem;
    private JMenuItem hostStatItem;
    private JMenuItem portStatItem;
    private JMenuItem errorStatItem;
    private JMenu helpMenu;
    private JMenuItem aboutItem;
    private JButton toolbarStartItem;
    private JButton toolbarStopItem;
    private JButton toolbarCaptureSettingsItem;
    private JButton toolbarFilterSettingsItem;
    private JButton toolbarPlotItem;

    DefaultTableModel model = new DefaultTableModel();
    TableColumn column;

    StartCapture startCapture = new StartCapture();
    CaptureSettings captureSettings;
    FilterSettings filterSettings;
    HighlightColor highlightColor;

    StatMath calc = new StatMath();

    List<String[]> list;

    public static JFrame frame1;
    public static JFrame frame2;
    public static JFrame frame3;
    public static JFrame frame4;

    private boolean firstChecked = false;

    public static Color tableColor = Color.GREEN;

    public JMenuBar setMenu() {

        frame1 = new JFrame("Start capture");
        frame1.setContentPane(startCapture.panel1);
        frame1.pack();

        frame2 = new JFrame("Filter settings");
        filterSettings = new FilterSettings();
        frame2.setContentPane(filterSettings.panel1);
        frame2.pack();

        frame3 = new JFrame("Capture settings");
        captureSettings = new CaptureSettings();
        frame3.setContentPane(captureSettings.panel1);
        frame3.pack();

        frame4 = new JFrame("Highlight color");
        highlightColor = new HighlightColor();
        frame4.setContentPane(highlightColor.panel1);
        frame4.pack();

        model = (DefaultTableModel) table1.getModel();
        model.addColumn("#");
        model.addColumn("Snap lenth");
        model.addColumn("Wire length");
        model.addColumn("Timestamp");
        model.addColumn("Source IP");
        model.addColumn("Destination IP");
        model.addColumn("Source port");
        model.addColumn("Destination port");
        model.addColumn("Protocol");
        model.addColumn("Duplicated");

        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open...");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(System.getenv("JAVA_HOME"));

                JFileChooser fc = new JFileChooser();
                int res = fc.showOpenDialog(new JFrame());
                if (res == JFileChooser.APPROVE_OPTION) {
                    final File file = fc.getSelectedFile();
                    class ProgressDialog extends JDialog {
                        JProgressBar progressBar;

                        public ProgressDialog() {
                            progressBar = new JProgressBar();
                            progressBar.setIndeterminate(true);

                            add(progressBar);
                        }
                    }
                    final ProgressDialog d = new ProgressDialog();
                    d.setModal(true);
                    d.setSize(300, 100);
                    d.setLocation(300, 300);
                    d.setUndecorated(true);

                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    label8.setText("PROCESSING...");
                                    progressBar1.setIndeterminate(true);
                                }
                            });

                            try {
                                CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()), ';');
                                list = reader.readAll();
                                reader.close();
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                JOptionPane.showMessageDialog(new JFrame(), "Error while opening file", "Error", JOptionPane.ERROR_MESSAGE);
                                e1.printStackTrace();
                            }

                            String str = DateTimeFormat.forPattern("HH:mm:ss:SSS").print(new DateTime());

                            if (model.getRowCount() > 0) {
                                for (int i = model.getRowCount() - 1; i > -1; i--) {
                                    model.removeRow(i);
                                }
                            }
                            model.fireTableDataChanged();

                            if (list != null) {
                                for (int i = 0; i < list.size() - 1; i++) {
                                    String[] item = list.get(i);
                                    model.addRow(new Object[] {i + 1, item[0], item[1], new Date(Long.valueOf(item[2])).toString(), item[3], item[4], item[5], item[6], item[7], item[8]});

                                }
                            }
                            model.fireTableDataChanged();

                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    label8.setText("DONE");
                                    progressBar1.setIndeterminate(false);
                                }
                            });

                            try {
                                this.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    };
                    t.start();
                }
            }
        });

        captureMenu = new JMenu("Capture");
        startItem = new JMenuItem("Start capture");
        startItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame1.setVisible(true);
            }
        });
        stopItem = new JMenuItem("Stop capture");
        stopItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(new JFrame(), "stopping capture", "stop", JOptionPane.INFORMATION_MESSAGE);
                try {
                    startCapture.stop();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        captureMenu.add(startItem);
        captureMenu.add(stopItem);

        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(openItem);
        fileMenu.add(exitItem);

        editMenu = new JMenu("Edit");
        captureSettingsItem = new JMenuItem("Capture settings...");
        captureSettingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame3.setVisible(true);
            }
        });
        updateRegionalItem = new JMenuItem("Update inner IP ranges");
        updateRegionalItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    RegionalHostParser.update();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(new JFrame(), "Cannot update inner ranges", "Error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });
        loadConfigItem = new JMenuItem("Load configuration file...");
        loadConfigItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] settings = new String[15];
                JFileChooser fc = new JFileChooser();
                int res = fc.showOpenDialog(new JFrame());
                if (res == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()));
                        String line = null;
                        int i = 0;
                        while ((line = br.readLine()) != null) {
                            settings[i] = line;
                            i++;
                        }
                        br.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    for (String setting : settings) {
                        String[] chunks = setting.trim().split("=");
                        switch (chunks[0]) {
                            case "TCP_ON":
                                Settings.TCP_ON = chunks[1].equals("true");
                                break;
                            case "UDP_ON":
                                Settings.UDP_ON = chunks[1].equals("true");
                                break;
                            case "REGIONAL_ONLY":
                                Settings.REGIONAL_ONLY = chunks[1].equals("true");
                                break;
                            case "HOSTS_ONLY":
                                Settings.HOSTS_ONLY = chunks[1].equals("true");
                                break;
                            case "PORTS_ONLY":
                                Settings.PORTS_ONLY = chunks[1].equals("true");
                                break;
                            case "hosts":
                                if (!chunks[1].equals("false")) {
                                    String[] chunkies = chunks[1].split(";");
                                    Settings.hosts.clear();
                                    for (String chunky : chunkies) {
                                        Settings.hosts.add(chunky);
                                    }
                                }
                                break;
                            case "ports":
                                if (!chunks[1].equals("false")) {
                                    String[] chunkies = chunks[1].split(";");
                                    Settings.ports.clear();
                                    for (String chunky : chunkies) {
                                        Settings.ports.add(chunky);
                                    }
                                }
                                break;
                            case "PROMISCUOUS_MODE":
                                Settings.PROMISCUOUS_MODE = chunks[1].equals("true");
                                break;
                            case "SNAPLEN":
                                if (Integer.parseInt(chunks[1]) > 0 && Integer.parseInt(chunks[1]) <= 65536) {
                                    Settings.SNAPLEN = Integer.parseInt(chunks[1]);
                                } else {
                                    JOptionPane.showMessageDialog(new JFrame(), "Error loading configuration file. Bad SNAPLEN value. Should be between 0 and 65536", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "TIMEOUT":
                                if (Integer.parseInt(chunks[1]) > 0) {
                                    Settings.TIMEOUT = Integer.parseInt(chunks[1]);
                                } else {
                                    JOptionPane.showMessageDialog(new JFrame(), "Error loading configuration file. Bad TIMEOUT value. Should be more than 0", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "TIME_LIMIT_ON":
                                Settings.TIME_LIMIT_ON = chunks[1].equals("true");
                                break;
                            case "TIME_LIMIT":
                                if (Integer.parseInt(chunks[1]) > 0) {
                                    Settings.TIME_LIMIT = Integer.parseInt(chunks[1]);
                                } else {
                                    JOptionPane.showMessageDialog(new JFrame(), "Error loading configuration file. Bad TIME_LIMIT value. Should be more than 0", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "PACKET_LIMIT_ON":
                                Settings.PACKET_LIMIT_ON = chunks[1].equals("true");
                                break;
                            case "PACKET_NUM":
                                if (Integer.parseInt(chunks[1]) > 0) {
                                    Settings.PACKET_NUM = Integer.parseInt(chunks[1]);
                                } else {
                                    JOptionPane.showMessageDialog(new JFrame(), "Error loading configuration file. Bad PACKET_NUM value. Should be more than 0", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "DEVICE":
                                if (Integer.parseInt(chunks[1]) >= 0) {
                                    Settings.PACKET_NUM = Integer.parseInt(chunks[1]);
                                } else {
                                    JOptionPane.showMessageDialog(new JFrame(), "Error loading configuration file. Bad DEVICE value. Should be equal or more than 0", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                        }

                    }
                }
            }
        });

        setColorItem = new JMenuItem("Set highlight color...");
        setColorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame4.setVisible(true);
            }
        });

        editMenu.add(captureSettingsItem);
        editMenu.addSeparator();
        editMenu.add(updateRegionalItem);
        editMenu.addSeparator();
        editMenu.add(loadConfigItem);
        editMenu.add(setColorItem);

        viewMenu = new JMenu("View");
        showToolbarItem = new JCheckBoxMenuItem("Show toolbar");
        showToolbarItem.setSelected(true);
        showToolbarItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (showToolbarItem.isSelected()) {
                    toolbar.setVisible(true);
                } else {
                    toolbar.setVisible(false);
                }
            }
        });
        showHighlightPanelItem = new JCheckBoxMenuItem("Show highlight control");
        showHighlightPanelItem.setSelected(true);
        showHighlightPanelItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (showHighlightPanelItem.isSelected()) {
                    panel2.setVisible(true);
                } else {
                    panel2.setVisible(false);
                }
            }
        });
        showProgressBarItem = new JCheckBoxMenuItem("Show progressbar");
        showProgressBarItem.setSelected(true);
        showProgressBarItem.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (showProgressBarItem.isSelected()) {
                    panel4.setVisible(true);
                } else {
                    panel4.setVisible(false);
                }
            }
        });
        viewMenu.add(showToolbarItem);
        viewMenu.add(showHighlightPanelItem);
        viewMenu.add(showProgressBarItem);

        analyzeMenu = new JMenu("Analyze");
        plotItem = new JMenuItem("Build network plot");
        plotItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialog dialog = new Dialog();
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);

                if (dialog.ok) {
                    createPlotChart(dialog.interval, dialog.start, dialog.chartName);
                }
            }
        });
        protocolStatItem = new JMenuItem("Protocol statistics...");
        protocolStatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialog dialog = new Dialog();
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);

                if (dialog.ok) {
                    createPieChart(dialog.interval, dialog.start, dialog.chartName, "protocol");
                }
            }
        });
        regionalStatItem = new JMenuItem("Inner statistics...");
        regionalStatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialog dialog = new Dialog();
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);

                if (dialog.ok) {
                    createPieChart(dialog.interval, dialog.start, dialog.chartName, "regional");
                }
            }
        });
        hostStatItem = new JMenuItem("Hosts statistics...");
        hostStatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatWindow stat = null;
                try {
                    stat = new StatWindow("host");
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                stat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                stat.setSize(600, 800);
                stat.setTitle("Stat");
                stat.setVisible(true);
            }
        });
        portStatItem = new JMenuItem("Port statistics...");
        portStatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatWindow stat = null;
                try {
                    stat = new StatWindow("port");
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                stat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                stat.setSize(600, 800);
                stat.setTitle("Stat");
                stat.setVisible(true);
            }
        });
        errorStatItem = new JMenuItem("Error statistics...");
        errorStatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialog dialog = new Dialog();
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);

                if (dialog.ok) {
                    createPieChart(dialog.interval, dialog.start, dialog.chartName, "error");
                }
            }
        });
        filterSettingsItem = new JMenuItem("Filter settings...");
        filterSettingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame2.setVisible(true);
            }
        });
        analyzeMenu.add(plotItem);
        analyzeMenu.add(protocolStatItem);
        analyzeMenu.add(regionalStatItem);
        analyzeMenu.add(hostStatItem);
        analyzeMenu.add(portStatItem);
        analyzeMenu.add(errorStatItem);
        analyzeMenu.addSeparator();
        analyzeMenu.add(filterSettingsItem);

        helpMenu = new JMenu("Help");
        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(new JFrame(), "Packalyzer 1.0\nApplication for analyzing network connection\nOleg Sushko, 2013", "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        helpMenu.add(aboutItem);

        toolbarStartItem = new JButton(new ImageIcon(new ImageIcon("start-button-hi.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        toolbarStartItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame1.setVisible(true);
            }
        });
        toolbarStopItem = new JButton(new ImageIcon(new ImageIcon("stop-button-hi.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        toolbarStopItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startCapture.stop();
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        toolbarCaptureSettingsItem = new JButton(new ImageIcon(new ImageIcon("settings.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        toolbarCaptureSettingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame3.setVisible(true);
            }
        });
        toolbarFilterSettingsItem = new JButton(new ImageIcon(new ImageIcon("filter.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        toolbarFilterSettingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame2.setVisible(true);
            }
        });
        toolbarPlotItem = new JButton(new ImageIcon(new ImageIcon("plot.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
        toolbarPlotItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialog dialog = new Dialog();
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);

                if (dialog.ok) {
                    createPlotChart(dialog.interval, dialog.start, dialog.chartName);
                }
            }
        });

        enableHighlight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkForFilters();
            }
        });

        disableHighlightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table1.clearSelection();
            }
        });

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(captureMenu);
        menuBar.add(viewMenu);
        menuBar.add(analyzeMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    public void createToolbar() {
        toolbar.add(toolbarStartItem);
        toolbar.add(toolbarStopItem);
        toolbar.addSeparator();
        toolbar.add(toolbarCaptureSettingsItem);
        toolbar.add(toolbarFilterSettingsItem);
        toolbar.addSeparator();
        toolbar.add(toolbarPlotItem);
    }

    public void highlight(ArrayList<Integer> positions) {
        if(positions.size() > 0) {
            if (!firstChecked) {
                table1.setRowSelectionInterval(positions.get(0), positions.get(0));
                firstChecked = true;
            }
            for (int i = 1; i < positions.size(); i++) {
                table1.addRowSelectionInterval(positions.get(i), positions.get(i));
            }
        }
    }

    public void checkForFilters() {
        try
        {
            RegionalHostParser.read();
            calc.setFilters();
            calc.clearCounters();
            calc.setData(list, 0, 0, "");
            calc.filterPackets();

            boolean firstChecked = false;

            table1.setRowSelectionAllowed(true);

            if (TCPCheckBox.isSelected()) {
                highlight(StatMath.tcpPositions);
                label1.setText("amount: " + StatMath.tcpPositions.size());
            }
            if (UDPCheckBox.isSelected()) {
                highlight(StatMath.udpPositions);
                label2.setText("amount: " + StatMath.udpPositions.size());
            }
            if (regionalOnlyCheckBox.isSelected()) {
                highlight(StatMath.regionalPositions);
                label4.setText("amount: " + StatMath.regionalPositions.size());
            }
            if (hostsOnlyCheckBox.isSelected()) {
                highlight(StatMath.hostsPositions);
                label5.setText("amount: " + StatMath.hostsPositions.size());
            }
            if (portsOnlyCheckBox.isSelected()) {
                highlight(StatMath.portsPositions);
                label6.setText("amount: " + StatMath.portsPositions.size());
            }
            if (errorCheckBox.isSelected()) {
                highlight(StatMath.errorPositions);
                label7.setText("amount: " + StatMath.errorPositions.size());
            }

            table1.setSelectionBackground(tableColor);
            table1.setEnabled(false);
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }

    public void createPlotChart(final int _time, final int _start, final String _chartName) {
        Thread t = new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        label8.setText("PROCESSING...");
                        progressBar1.setIndeterminate(true);
                    }
                });

                try {
                    long timeInterval = (long) (_time * 1000);
                    int startPos = _start;

                    RegionalHostParser.read();
                    calc.setFilters();
                    calc.clearCounters();
                    calc.setData(list, startPos, timeInterval, _chartName);
                    calc.filterPackets();
                    calc.calcSumPerInterval();
                    System.out.println("===================================");
                    calc.setChartName();
                    calc.createPlotChart();
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        label8.setText("DONE");
                        progressBar1.setIndeterminate(false);
                    }
                });

                try {
                    this.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void createPieChart(final int _time, final int _start, final String _chartName, final String dataType) {
        Thread t = new Thread() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        label8.setText("PROCESSING...");
                        progressBar1.setIndeterminate(true);
                    }
                });

                try
                {
                    long timeInterval = (long) (_time * 1000);
                    int startPos = _start;

                    RegionalHostParser.read();
                    calc.setFilters();
                    calc.clearCounters();
                    calc.setData(list, startPos, timeInterval, _chartName);
                    calc.filterPackets();
                    calc.calcSumPerInterval();
                    System.out.println("===================================");
                    calc.setChartName();
                    calc.createPieChart(dataType);
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        label8.setText("DONE");
                        progressBar1.setIndeterminate(false);
                    }
                });

                try {
                    this.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public static class ValueComparator implements Comparator<String> {
        Map<String, Integer> base;
        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public class StatWindow extends JFrame {
        private JPanel panel;
        private JScrollPane scrollPane;

        private JTable table1;
        private JTable table2;

        private String[][] src_data;
        private String[][] dst_data;
        private JTabbedPane tabbedPane;
        private String dataType;

        private JTable table3;

        private double expectedValue;
        private double varienceValue;
        private double deviationValue;

        Map<String, Integer> map = new HashMap<String, Integer>();

        public StatWindow(String type) throws ParseException {
            setLayout(new FlowLayout());
            tabbedPane = new JTabbedPane();

            String[] columns = {"Value", "Source", "Destination"};
            Object[][] data = new Object[3][3];

            String[] columnNames = {"#", "Name", "Bytes", "Percent"};

            dataType = type;
            String name = "";

            if (type.equals("host")) {
                src_data = fillData(3);
                name = "host";
            } else if (type.equals("port")) {
                src_data = fillData(5);
                name = "port";
            }

            table1 = new JTable(src_data, columnNames);
            table1.setPreferredScrollableViewportSize(new Dimension(500, 700));
            table1.setFillsViewportHeight(true);

            JScrollPane scrollPane1 = new JScrollPane(table1);
            double e1 = expectedValue;
            double v1 = varienceValue;
            double d1 =  deviationValue;
            tabbedPane.addTab("Source " + name + " statistics", scrollPane1);

            if (type.equals("host")) {
                dst_data = fillData(4);
                name = "host";
            } else if (type.equals("port")) {
                dst_data = fillData(6);
                name = "port";
            }

            table2 = new JTable(dst_data, columnNames);
            table2.setPreferredScrollableViewportSize(new Dimension(500, 700));
            table2.setFillsViewportHeight(true);

            JScrollPane scrollPane2 = new JScrollPane(table2);
            double e2 = expectedValue;
            double v2 = varienceValue;
            double d2 =  deviationValue;
            tabbedPane.addTab("Destination " + name + " statistics", scrollPane2);
            add(tabbedPane);

            data[0][0] = "Expected value";
            data[0][1] = e1;
            data[0][2] = e2;
            data[1][0] = "Variance value";
            data[1][1] = v1;
            data[1][2] = v2;
            data[2][0] = "Deviation value";
            data[2][1] = d1;
            data[2][2] = d2;

            table3 = new JTable(data, columns);
            table3.setPreferredScrollableViewportSize(new Dimension(500, 300));
            table3.setFillsViewportHeight(true);
            table3.getColumnModel().getColumn(0).setPreferredWidth(150);
            table3.getColumnModel().getColumn(1).setPreferredWidth(150);
            table3.getColumnModel().getColumn(2).setPreferredWidth(150);
            JFrame frame = new JFrame();
            frame.getContentPane().add(new JScrollPane(table3));
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(550, 200);
            frame.setTitle("Stat parameters");
            frame.setVisible(true);
        }

        public HashMap<String, Integer> trimMap(Map map) {
            int k = 0;
            int otherSum = 0;
            TreeMap<String, Integer> tmp = new TreeMap<>();
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                if (k < 50) {
                    Map.Entry pairs = (Map.Entry)it.next();
                    tmp.put((String) pairs.getKey(), (int) pairs.getValue());
                } else {
                    Map.Entry pairs = (Map.Entry) it.next();
                    otherSum += (int) pairs.getValue();
                }
                k++;
            }

            k = 0;
            HashMap<String, Integer> newMap = new HashMap<>();
            it = tmp.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                newMap.put((String) pairs.getKey(), (int) pairs.getValue());
            }
            newMap.put("other", otherSum);

            return newMap;
        }

        public int getMapSum(Map map) {
            int sum = 0;
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                sum += (int) pairs.getValue();
            }

            return sum;
        }

        // column = 3 or 4 for host, 5 or 6 for port
        public String[][] fillData(int column) throws ParseException {
            for (String[] row : list) {
                if (row[3].equals("skipped")) {
                    continue;
                } else if (map.containsKey(row[column])) {
                    int s = Integer.parseInt(row[0]);
                    map.put(row[column], map.get(row[column]) + s);
                } else {
                    int s = Integer.parseInt(row[0]);
                    map.put(row[column], s);
                }
            }

            int sum = getMapSum(map);

            ValueComparator bvc = new ValueComparator(map);
            TreeMap<String, Integer> sortedTableData = new TreeMap<String, Integer>(bvc);
            sortedTableData.putAll(map);

            /*ValueComparator comparator = new ValueComparator(sortedTableData);
            TreeMap<String, Integer> sortedChartData = new TreeMap<String, Integer>(bvc);
            sortedChartData.putAll(sortedTableData);*/

            //printMap(sorted_map);
            HashMap<String, Integer> chartData = trimMap(sortedTableData);
            ValueComparator bvc2 = new ValueComparator(chartData);
            TreeMap<String, Integer> shit = new TreeMap<String, Integer>(bvc2);
            shit.putAll(chartData);
            System.out.println(shit);

            createChart(column, shit);

            map.clear();

            List<Double> percents = new ArrayList<Double>();
            int i = 0;
            String[][] data = new String[sortedTableData.keySet().size()][4];
            Iterator it = sortedTableData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry) it.next();
                data[i][0] = i + 1 + "";
                data[i][1] = entry.getKey();
                data[i][2] = entry.getValue().toString();
                long v = entry.getValue();
                double percent = (v * 100) / (double) sum;
                DecimalFormat df = new DecimalFormat("###.#####");
                percent = df.parse(df.format(percent)).doubleValue();
                percents.add(percent);
                data[i][3] = percent + "";
                i++;
            }

            expectedValue = getExpectedValue(sortedTableData, percents);
            varienceValue = getVarienceValue(sortedTableData, percents);
            deviationValue = Math.sqrt(varienceValue);

            return data;
        }

        public double getVarienceValue(TreeMap map, List<Double> percents) {
            double value = 0;
            int i = 0;
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                value += Math.pow((int) pairs.getValue(), 2) * (percents.get(i) / 100);
                i++;
            }

            return value - Math.pow(getExpectedValue(map, percents), 2);
        }

        public double getExpectedValue(TreeMap map, List<Double> percents) {
            double value = 0;
            int i = 0;
            Iterator it = map.entrySet().iterator();
            System.out.println(map.values().size() + " - " + percents.size());
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                value += (int) pairs.getValue() * (percents.get(i) / 100);
                i++;
            }

            return value;
        }

        public void createChart(int column, TreeMap map) {
            ChartBuilder chartBuilder = new ChartBuilder();
            if (dataType.equals("host")) {
                if (column == 3) chartBuilder.createPieChartOverall(map, "source hosts");
                if (column == 4) chartBuilder.createPieChartOverall(map, "destination hosts");
            } else {
                if (column == 5) chartBuilder.createPieChartOverall(map, "source ports");
                if (column == 6) chartBuilder.createPieChartOverall(map, "destination ports");
            }
        }
    }

    public class Dialog extends JDialog {
        public JPanel contentPane;
        private JButton buttonOK;
        private JButton buttonCancel;
        private JTextField textField1;
        private JCheckBox startPositionCheckBox;
        private JCheckBox endPositionCheckBox;
        private JTextField textField2;
        private JTextField textField3;
        private JCheckBox defaultChartTitleCheckBox;
        private JTextField textField4;

        public String chartName = "chart-";
        public int interval = 5;
        public int start = 0;
        public int end = 0;

        public boolean ok = false;

        public Dialog() {
            setContentPane(contentPane);
            setModal(true);
            getRootPane().setDefaultButton(buttonOK);

            buttonOK.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onOK();
                }
            });

            buttonCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    onCancel();
                }
            });

            startPositionCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    textField2.setEnabled(startPositionCheckBox.isSelected());
                }
            });

            endPositionCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    textField3.setEnabled(endPositionCheckBox.isSelected());
                }
            });

            defaultChartTitleCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    textField4.setEnabled(!defaultChartTitleCheckBox.isSelected());
                }
            });

            contentPane.registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }

        private void onOK() {
            if (textField4.isEnabled()) {
                chartName = textField4.getText() + "-";
            }
            if (Integer.parseInt(textField1.getText()) > 0) {
                interval = Integer.parseInt(textField1.getText());
            } else {
                interval = 0;
            }
            if (textField2.isEnabled()) {
                if (Integer.parseInt(textField2.getText()) >= 0) {
                    start = Integer.parseInt(textField2.getText());
                }
            }
            if (textField3.isEnabled()) {
                if (Integer.parseInt(textField3.getText()) > 0) {
                    end = Integer.parseInt(textField3.getText());
                }
            }
            ok = true;

            dispose();
        }
        private void onCancel() {
            dispose();
        }
    }

    public static void main(final String[] args) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Packalyzer t = new Packalyzer();
        JFrame frame = new JFrame("Packalyzer");
        frame.setSize(1280, 700);
        frame.setJMenuBar(t.setMenu());
        t.createToolbar();
        frame.setContentPane(t.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        Dimension dim = frame.getSize();
        System.out.println(dim.getWidth());
        System.out.println(dim.getHeight());
    }
}
