package stat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.Rotation;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class ChartBuilder
{
    public void createPieChartOverall(Map map, String name) {
        int sum = 0;
        DefaultPieDataset result = new DefaultPieDataset();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            result.setValue("" + pairs.getKey(), (int) pairs.getValue());
        }

        PieDataset dataset = result;
        JFreeChart chart = ChartFactory.createPieChart("overall " + name,
                dataset,
                false,
                true,
                false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 800));
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);

        try
        {
            ChartUtilities.saveChartAsJPEG(new File("D:\\OVERALL-" + name + ".jpg"), chart, 800, 800);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void createPieChart(String dataType) throws UnknownHostException {
        int sum = 0;
        DefaultPieDataset result = new DefaultPieDataset();

        int tcp = 0;
        int udp = 0;
        int global = 0;
        int inner = 0;
        int hosts = 0;
        int ports = 0;
        int error = 0;

        switch (dataType) {
            case "protocol":
                StatMath.getProtocolSum();
                tcp = StatMath.tcpSum;
                udp = StatMath.udpSum;

                result.setValue("tcp: " + tcp, tcp);
                result.setValue("udp: " + udp, udp);
                break;
            case "regional":
                StatMath.getRegionalSum();
                global = StatMath.globalSum;
                inner = StatMath.regionalSum;

                result.setValue("global: " + global, global);
                result.setValue("inner: " + inner, inner);
                break;
            case "error":
                StatMath.getErrorSum();
                global = StatMath.globalSum;
                error = StatMath.errorSum;
                result.setValue("global: " + global, global);
                result.setValue("error: " + error, error);
                break;
            case "overall":
                break;
        }

        PieDataset dataset = result;
        JFreeChart chart = ChartFactory.createPieChart3D(StatMath.chartName,
                dataset,
                true,
                true,
                false);
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 800));
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);

        try
        {
            ChartUtilities.saveChartAsJPEG(new File("D:\\PIE-" + StatMath.chartName + ".jpg"), chart, 800, 800);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void createPlotChartPercent() throws ParseException {
        TimeSeries series = new TimeSeries("hey", Second.class);

        int globalsum = 0;
        for (int sum : StatMath.sums) {
            globalsum += sum;
        }
        System.out.println("globalsum: " + globalsum);

        ArrayList<Double> percents = new ArrayList();
        for (int i = 0; i < StatMath.sums.size(); i++) {
            double percent = ((long) StatMath.sums.get(i) * 100) / (double) globalsum;
            DecimalFormat df = new DecimalFormat("###.#####");
            percent = df.parse(df.format(percent)).doubleValue();
            percents.add(percent);
        }

        for (int i = 0; i < StatMath.times.size(); i++)
        {
            Date date = new Date(StatMath.times.get(i));
            series.addOrUpdate(new Second(date), percents.get(i).doubleValue());
        }



        System.out.println("percentage " + percents.size());
        System.out.println("times " + StatMath.times.size());
        for (int i = 0; i < StatMath.times.size(); i++)
        {
            System.out.println(StatMath.times.get(i) + " - " + percents.get(i));
        }

        double expectedValue = getExpectedValue(StatMath.sums, percents);
        double varienceValue = getVarienceValue(StatMath.sums, percents);
        double deviationValue = Math.sqrt(varienceValue);

        String[] columns = {"Parameter", "Value"};
        Object[][] data = new Object[3][2];
        data[0][0] = "Expected value";
        data[0][1] = expectedValue;
        data[1][0] = "Variance value";
        data[1][1] = varienceValue;
        data[2][0] = "Deviation value";
        data[2][1] = deviationValue;

        JTable table3 = new JTable(data, columns);
        table3.setPreferredScrollableViewportSize(new Dimension(300, 150));
        table3.setFillsViewportHeight(true);
        table3.getColumnModel().getColumn(0).setPreferredWidth(150);
        table3.getColumnModel().getColumn(1).setPreferredWidth(150);;
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JScrollPane(table3));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(350, 200);
        frame.setTitle("Stat parameters");
        frame.setVisible(true);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);
        XYDataset d = dataset;

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                StatMath.chartName,
                "time",
                "percentage",
                d,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0, 100);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 800));
        JFrame chartFrame = new JFrame();
        chartFrame.setContentPane(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);

        try
        {
            ChartUtilities.saveChartAsJPEG(new File("D:\\percentage-" + StatMath.chartName + ".jpg"), chart, 2000, 500);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public double getVarienceValue(List sums, List<Double> percents) {
        double value = 0;

        for (int i = 0; i < sums.size(); i++) {
            value += Math.pow((int) sums.get(i), 2) * (percents.get(i) / 100);
        }

        return value - Math.pow(getExpectedValue(sums, percents), 2);
    }

    public double getExpectedValue(List sums, List<Double> percents) {
        double value = 0;
        for (int i = 0; i < sums.size(); i++) {
            value += (int) sums.get(i) * (percents.get(i) / 100);
        }

        return value;
    }

    public void createPlotChart()
    {
        TimeSeries series = new TimeSeries("hey", Second.class);

        for (int i = 0; i < StatMath.times.size(); i++)
        {
            Date date = new Date(StatMath.times.get(i));
            System.out.println(date);
            series.addOrUpdate(new Second(date), StatMath.sums.get(i).doubleValue());
        }

        System.out.println(StatMath.chartName);

        int globalsum = 0;
        for (int sum : StatMath.sums) {
            globalsum += sum;
        }
        System.out.println("globalsum: " + globalsum);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                StatMath.chartName,
                "time",
                "bytes",
                dataset,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 800));
        JFrame frame = new JFrame();
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);

        try
        {
            ChartUtilities.saveChartAsJPEG(new File("D:\\PLOT-" + StatMath.chartName + ".jpg"), chart, 2000, 500);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
