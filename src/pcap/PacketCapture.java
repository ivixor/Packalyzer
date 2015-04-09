package pcap;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketCapture implements Runnable {
    static StringBuilder errbuf = new StringBuilder();
    static List<PcapIf> alldevs = new ArrayList<PcapIf>();

    static Pcap pcap = null;
    static BufferedWriter bw;

    public static boolean running = false;

    public static String[] findDevices() {
        int r = Pcap.findAllDevs(alldevs, errbuf);
        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errbuf
                    .toString());
            return null;
        }

        System.out.println("Network devices found:");
        String[] devices = new String[alldevs.size()];

        int i = 0;
        for (PcapIf device : alldevs) {
            String description =
                    (device.getDescription() != null) ? device.getDescription()
                            : "No description available";
            devices[i] = device.getName();
            i++;
        }

        return devices;
    }

    public static void startCapture() throws IOException {
        PcapIf device = alldevs.get(Settings.DEVICE);
        System.out
                .printf("\nChoosing '%s' on your behalf:\n",
                        (device.getDescription() != null) ? device.getDescription()
                                : device.getName());

        final boolean time_limit_on = Settings.TIME_LIMIT_ON;
        final long time_limit = Settings.TIME_LIMIT * 1000;
        final boolean packet_limit_on = Settings.PACKET_LIMIT_ON;
        final int packet_limit = Settings.PACKET_NUM;
        int snaplen = Settings.SNAPLEN;
        int flags;
        if (Settings.PROMISCUOUS_MODE) {
            flags = Pcap.MODE_PROMISCUOUS;
        } else {
            flags = Pcap.MODE_NON_PROMISCUOUS;
        }
        int timeout = Settings.TIMEOUT;
        pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

        if (pcap == null) {
            System.err.printf("Error while opening device for capture: "
                    + errbuf.toString());
            return;
        }

        final long start = System.currentTimeMillis();
        bw = new BufferedWriter(new FileWriter("D:\\stat.csv"));

        PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
            //int iter = Settings.PACKET_NUM;
            int iter = 0;
            Ip4 ip4Prot = new Ip4();
            Tcp tcpProt = new Tcp();
            Udp udpProt = new Udp();
            Icmp icmpProt = new Icmp();

            long prev_seq = -1;
            long current_seq;

            public void nextPacket(PcapPacket packet, String user) {
                long now = System.currentTimeMillis();

                if (running) {
                    if (time_limit_on) {
                        if (now - start < time_limit) {
                            getPacketData(packet);
                            try {
                                stopCapture();
                            } catch (IOException e) { e.printStackTrace(); }
                        } else {
                            System.out.println("time closing");
                            try {
                                pcap.breakloop();
                                stopCapture();
                            } catch (IOException e) { e.printStackTrace(); }
                        }
                    } else {
                        getPacketData(packet);
                    }
                }
            }

            public void getPacketData(PcapPacket packet) {
                try {
                    System.out.println("=============================");

                    if (packet.getCaptureHeader() != null) {
                        System.out.println(packet.getCaptureHeader().caplen());
                        bw.write(packet.getCaptureHeader().caplen() + "");
                        bw.write(";");
                        bw.write(packet.getCaptureHeader().wirelen() + "");
                        bw.write(";");
                        long time = packet.getCaptureHeader().timestampInMillis();
                        bw.write(Long.toString(time));
                        bw.write(";");
                    } else {
                        bw.write("skipped");
                        bw.write(";");
                        bw.write("skipped");
                        bw.write(";");
                        bw.write("skipped");
                        bw.write(";");
                    }

                    if (packet.hasHeader(ip4Prot)) {
                        System.out.println(FormatUtils.ip(ip4Prot.source()) + " - " + FormatUtils.ip(ip4Prot.destination()));
                        bw.write(FormatUtils.ip(ip4Prot.source()));
                        bw.write(";");
                        bw.write(FormatUtils.ip(ip4Prot.destination()));
                        bw.write(";");
                    } else {
                        System.out.println("skipped");
                        bw.write("skipped");
                        bw.write(";");
                        bw.write("skipped");
                        bw.write(";");
                    }

                    // transport layer
                    if (packet.hasHeader(tcpProt)) {
                        System.out.println("TCP: " + tcpProt.source() + " - " + tcpProt.destination());
                        bw.write(tcpProt.source() + "");
                        bw.write(";");
                        bw.write(tcpProt.destination() + "");
                        bw.write(";");
                        bw.write("tcp");
                        bw.write(";");

                        current_seq = tcpProt.seq();
                        if (current_seq == prev_seq)
                        {
                            System.out.println("duplicated");
                            bw.write("duplicated");
                            bw.write(";");
                            prev_seq = current_seq;
                        } else {
                            prev_seq = current_seq;
                            bw.write("");
                            bw.write(";");
                        }

                    } else if(packet.hasHeader(udpProt)) {
                        System.out.println("UDP: " + udpProt.source() + " - " + udpProt.destination());
                        bw.write(udpProt.source() + "");
                        bw.write(";");
                        bw.write(udpProt.destination() + "");
                        bw.write(";");
                        bw.write("udp");
                        bw.write(";");
                        bw.write("");
                        bw.write(";");
                    } else if(packet.hasHeader(icmpProt)) {
                        System.out.println("icmp");
                        bw.write(icmpProt.type() + "");
                        bw.write(";");
                        bw.write(icmpProt.type() + "");
                        bw.write(";");
                        bw.write("icmp");
                        bw.write(";");
                        bw.write("");
                        bw.write(";");
                    }
                    else {
                        bw.write("skipped");
                        bw.write(";");
                        bw.write("skipped");
                        bw.write(";");
                        bw.write("skipped");
                        bw.write(";");
                        bw.write("");
                        bw.write(";");
                    }

                    bw.write("\n");
                } catch (Exception e) { }
                iter++;
                System.out.println(iter);

                /*if (packet_limit_on) {
                    if (iter < packet_limit - 1) {
                        iter++;
                    } else {
                        System.out.println("packet closing");
                        try {
                            //pcap.breakloop();
                            stopCapture();
                        } catch (Exception ee) { }
                    }

                    System.out.println(iter);
                }*/
            }
        };

        pcap.loop(packet_limit, jpacketHandler, "jNetPcap rocks!");
        pcap.close();
        stopCapture();
        long end = System.currentTimeMillis();
        System.out.println("time difference: " + (end - start));
    }

    public static void stopCapture() throws IOException {
        bw.close();
        System.out.println("file has been saved");
        running = false;
    }

    @Override
    public void run() {
        try {
            if (running) {
                startCapture();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
