/*
package stat;

import au.com.bytecode.opencsv.CSVReader;
import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        try
        {
            System.out.println(System.getenv("JAVA_HOME"));
            CSVReader reader = new CSVReader(new FileReader("F:\\stat.csv"), ';');
            List list = reader.readAll();
            reader.close();

            long timeInterval = 1000L;
            int startPos = 10;
            //int startPos = 199693;

            RegionalHostParser.update();
            StatMath calc = new StatMath();
            calc.filterPackets();
            calc.calcSumPerInterval();
            System.out.println("===================================");
            calc.createPlotChart();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            String query = "netstat -s";
            final Process p = Runtime.getRuntime().exec(String.format("cmd /c %s", query));
            final ProcessResultReader stderr = new ProcessResultReader(p.getErrorStream(), "STDERR");
            final ProcessResultReader stdout = new ProcessResultReader(p.getInputStream(), "STDOUT");
            stderr.start();
            stdout.start();
            final int exitValue = p.waitFor();
            if (exitValue == 0)
            {
                //System.out.print(stdout.toString());
            }
            else
            {
                //System.err.print(stderr.toString());
            }
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
*/
