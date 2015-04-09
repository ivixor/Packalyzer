package stat;

import pcap.Settings;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;

public class StatMath {
    Queue<Integer> queue = new LinkedList<Integer>();
    static List<String[]> filtered = new ArrayList<String[]>();

    List<String[]> list = null;
    int startPos;
    long timeInterval;
    int endPos = 0;
    static ArrayList<Long> times = new ArrayList<Long>();
    static ArrayList<Integer> sums = new ArrayList<Integer>();
    static ArrayList<Integer> amount = new ArrayList<>();
    static public ArrayList<Integer> tcpPositions = new ArrayList<Integer>();
    static public ArrayList<Integer> udpPositions = new ArrayList<Integer>();
    static public ArrayList<Integer> regionalPositions = new ArrayList<Integer>();
    static public ArrayList<Integer> hostsPositions = new ArrayList<Integer>();
    static public ArrayList<Integer> portsPositions = new ArrayList<Integer>();
    static public ArrayList<Integer> errorPositions = new ArrayList<Integer>();
    int listSize;

    boolean isTCP = true;
    boolean isUDP = true;
    boolean isICMP = true;
    boolean regionalOnly = false;
    boolean hostOnly = false;
    boolean portOnly = false;

    static int amountTCP = 0;
    static int amountUDP = 0;
    static int amountGlobal = 0;
    static int amountRegional = 0;
    static int amountHostOnly = 0;
    static int amountPortOnly = 0;
    static int amountError = 0;

    int packetAmount = 0;
    static int regionalSum = 0;
    static int globalSum = 0;
    static int errorSum = 0;
    static int tcpSum = 0;
    static int udpSum = 0;

    static String chartName;

    public void setFilters() {
        isTCP = Settings.TCP_ON;
        isUDP = Settings.UDP_ON;
        regionalOnly = Settings.REGIONAL_ONLY;
        hostOnly = Settings.HOSTS_ONLY;
        portOnly = Settings.PORTS_ONLY;
    }

    public void setData(List _list, int _startPos, long _timeInterval, String _chartName) {
        list = _list;
        startPos = _startPos;
        timeInterval = _timeInterval;
        chartName = _chartName;
    }

    public void setChartName() {

        if (isTCP) {
            chartName += "tcp-";
        }
        if (isUDP) {
            chartName += "udp-";
        }
        if (hostOnly) {
            for (String host : Settings.hosts) {
                chartName += host + "-";
            }
        }
        if (portOnly) {
            for (String port : Settings.ports) {
                chartName += port + "-";
            }
        }
        chartName += regionalOnly ? "regional" : "global";
    }

    public void createPlotChart() throws ParseException {
        ChartBuilder builder = new ChartBuilder();
        builder.createPlotChart();
        builder.createPlotChartPercent();
    }

    public void createPieChart(String dataType) throws UnknownHostException {
        ChartBuilder builder = new ChartBuilder();
        builder.createPieChart(dataType);
    }

    public static void getErrorSum() {
        globalSum = 0;
        errorSum = 0;
        int sum = 0;
        for (int i = 0; i < filtered.size(); i++) {
            String[] row = filtered.get(i);
            if (row[8].equals("duplicated")) {
                errorSum += Integer.parseInt(row[0]);
            } else {
                globalSum += Integer.parseInt(row[0]);
            }
            sum += Integer.parseInt(row[0]);
        }
        System.out.println(sum);
    }

    public static void getRegionalSum() throws UnknownHostException {
        globalSum = 0;
        regionalSum = 0;
        int sum = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < filtered.size(); i++) {
            String[] row = filtered.get(i);
            if (isRegional(row[3])) {
                regionalSum += Integer.parseInt(row[0]);
            } else {
                globalSum += Integer.parseInt(row[0]);
            }
            sum += Integer.parseInt(row[0]);
        }
        System.out.println(System.currentTimeMillis() - startTime);
        System.out.println(sum);
    }

    public static void getProtocolSum() {
        tcpSum = 0;
        udpSum = 0;
        int sum = 0;
        for (int i = 0; i < filtered.size(); i++) {
            String[] row = filtered.get(i);
            if (row[7].equals("tcp")) {
                tcpSum += Integer.parseInt(row[0]);
            } else if (row[7].equals("udp")) {
                udpSum += Integer.parseInt(row[0]);
            }
            sum += Integer.parseInt(row[0]);
        }
        System.out.println("tcp: " + tcpSum);
        System.out.println("udp: " + udpSum);
        System.out.println("overall: " + sum);
    }

    public void calcSumPerInterval() {
        long interval = timeInterval;
        String[] row = filtered.get(0);
        long prevDate = Long.valueOf(row[2]);
        int sum = Integer.parseInt(row[0]);
        for (int i = 1; i < filtered.size(); i++) {
            row = filtered.get(i);
            long date = Long.valueOf(row[2]);

            if (i == filtered.size() - 1) {
                System.out.println(i);
                sum += Integer.parseInt(row[0]);
                times.add(prevDate);
                sums.add(sum);
                break;
            }

            if (date - prevDate <= interval) {
                sum += Integer.parseInt(row[0]);
            } else {
                times.add(prevDate);
                sums.add(sum);
                sum = 0;
                sum += Integer.parseInt(row[0]);
                prevDate = date;
            }
        }

        for (int i = 0; i < times.size(); i++) {
            System.out.println(times.get(i) + " - " + sums.get(i));
        }
    }

    public static boolean isRegional(String ip) throws UnknownHostException {
        long ipLo;
        long ipHi;
        long ipToTest;

        for (int i = 0; i < RegionalHostParser.ipLow.size(); i++) {
            ipLo = RegionalHostParser.ipToLong(InetAddress.getByName(RegionalHostParser.ipLow.get(i)));
            ipHi = RegionalHostParser.ipToLong(InetAddress.getByName(RegionalHostParser.ipHigh.get(i)));
            ipToTest = RegionalHostParser.ipToLong(InetAddress.getByName(ip));

            if (ipToTest >= ipLo && ipToTest <= ipHi)
            {
                return true;
            }
        }
        return false;
    }

    public void clearCounters() {
        amountTCP = 0;
        amountUDP = 0;

        amountRegional = 0;
        amountGlobal = 0;

        amountHostOnly = 0;
        amountPortOnly = 0;

        amountError = 0;

        filtered.clear();
        sums.clear();
        times.clear();
        amount.clear();
        tcpPositions.clear();
        udpPositions.clear();
        regionalPositions.clear();
        hostsPositions.clear();
        portsPositions.clear();
        errorPositions.clear();
    }

    public void countPackets(boolean tcp, boolean udp, boolean icmp, boolean regional, boolean global, boolean host, boolean port, int i) {
        if (tcp) {
            amountTCP++;
            tcpPositions.add(i);
        }
        if (udp) {
            amountUDP++;
            udpPositions.add(i);
        }

        if (regional) {
            amountRegional++;
            regionalPositions.add(i);
        }
        if (global) amountGlobal++;

        if (host) {
            amountHostOnly++;
            hostsPositions.add(i);
        }
        if (port) {
            amountPortOnly++;
            portsPositions.add(i);
        }
    }

    public void filterPackets() throws IOException {
        String[] row = null;

        for (int i = 0; i < list.size() - 1; i++) {
            System.out.println(i);

            row = (String[]) list.get(i);

            if (row[3].contains("skipped") || row[4].contains("skipped") || row[5].contains("skipped") || row[6].contains("skipped")) {
                continue;
            }

            if (isTCP && row[7].equals("tcp")) {
                if (regionalOnly && isRegional(row[3])) {
                    if (hostOnly && (Settings.hosts.contains(row[3]) || Settings.hosts.contains(row[4]))) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(true, false, false, true, true, true, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(true, false, false, true, true, true, false, i);
                        } else {
                            countPackets(true, false, false, true, true, true, false, i);
                            continue;
                        }
                    } else if (!hostOnly) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(true, false, false, true, true, false, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(true, false, false, true, true, false, false, i);
                        } else {
                            countPackets(true, false, false, true, true, false, false, i);
                            continue;
                        }
                    } else {
                        countPackets(true, false, false, true, true, false, false, i);
                        continue;
                    }
                } else if (!regionalOnly) {
                    if (hostOnly && (Settings.hosts.contains(row[3]) || Settings.hosts.contains(row[4]))) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(true, false, false, false, true, true, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(true, false, false, false, true, true, false, i);
                        } else {
                            countPackets(true, false, false, false, true, true, false, i);
                            continue;
                        }
                    } else if (!hostOnly) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(true, false, false, false, true, false, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(true, false, false, false, true, false, false, i);
                        } else {
                            countPackets(true, false, false, false, true, false, false, i);
                            continue;
                        }
                    } else {
                        countPackets(true, false, false, false, true, false, false, i);
                        continue;
                    }
                } else {
                    countPackets(true, false, false, false, true, false, false, i);
                    continue;
                }
            }

            if (isUDP && row[7].equals("udp")) {
                if (regionalOnly && isRegional(row[3])) {
                    if (hostOnly && (Settings.hosts.contains(row[3]) || Settings.hosts.contains(row[4]))) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(false, true, false, true, true, true, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(false, true, false, true, true, true, false, i);
                        } else {
                            countPackets(false, true, false, true, true, true, false, i);
                            continue;
                        }
                    } else if (!hostOnly) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(false, true, false, true, true, false, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(false, true, false, true, true, false, false, i);
                        } else {
                            countPackets(false, true, false, true, true, false, false, i);
                            continue;
                        }
                    } else {
                        countPackets(false, true, false, true, true, false, false, i);
                        continue;
                    }
                } else if (!regionalOnly) {
                    if (hostOnly && (Settings.hosts.contains(row[3]) || Settings.hosts.contains(row[4]))) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(false, true, false, false, true, true, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(false, true, false, false, true, true, false, i);
                        } else {
                            countPackets(false, true, false, false, true, true, false, i);
                            continue;
                        }
                    } else if (!hostOnly) {
                        if (portOnly && (Settings.ports.contains(row[5]) || Settings.ports.contains(row[6]))) {
                            filtered.add(row);
                            countPackets(false, true, false, false, true, false, true, i);
                        } else if (!portOnly) {
                            filtered.add(row);
                            countPackets(false, true, false, false, true, false, false, i);
                        } else {
                            countPackets(false, true, false, false, true, false, false, i);
                            continue;
                        }
                    } else {
                        countPackets(false, true, false, false, true, false, false, i);
                        continue;
                    }
                } else {
                    countPackets(false, true, false, false, true, false, false, i);
                    continue;
                }
            }


        }
    }
}
