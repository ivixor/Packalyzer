package stat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class RegionalHostParser {
    public static List<String> ipLow = new ArrayList<String>();
    public static List<String> ipHigh = new ArrayList<String>();

    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }

        return result;
    }

    public static void update() throws IOException {
        String url = "http://ipdiapazon.16mb.com/Ukraine.html";
        Document doc = Jsoup.connect(url).get();
        Elements e = doc.select("textarea");
        String str = e.text();
        BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\regional.txt"));
        bw.write(str);
        bw.close();
    }

    public static void read() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("D:\\regional.txt"));
        String line;
        int i = 0;
        while ((line = br.readLine()) != null) {
            ipLow.add(line.split(" - ")[0]);
            ipHigh.add(line.split(" - ")[1]);
        }
        br.close();
    }
}
