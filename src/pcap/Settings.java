package pcap;


import java.util.ArrayList;
import java.util.List;

public class Settings {
    public static boolean TCP_ON = true;
    public static boolean UDP_ON = true;
    public static boolean REGIONAL_ONLY = false;
    public static boolean HOSTS_ONLY = false;
    public static boolean PORTS_ONLY = false;
    public static List<String> hosts = new ArrayList<String>();
    public static List<String> ports = new ArrayList<String>();

    public static boolean PROMISCUOUS_MODE = true;
    public static int SNAPLEN = 65536;
    public static int TIMEOUT = 1000;

    public static boolean TIME_LIMIT_ON = false;
    public static int TIME_LIMIT = 0;
    public static boolean PACKET_LIMIT_ON = false;
    public static int PACKET_NUM = 20000;

    public static int DEVICE = 1;
}
