package oleksii.bashuk.hash.signs.chart;

import oleksii.bashuk.hash.signs.measure.GWOTSMeasures;
import oleksii.bashuk.hash.signs.measure.record.GWOTSMeasureRecord;
import oleksii.bashuk.hash.signs.measure.record.MeasureRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GWOTSCharts extends JFrame {

    private static final String GWOTS = "gwots";
    private static final String GWOTS_PLUS = "gwots_plus";

    private static final String BUILD_REQUESTS_SERIES_NAME = "Build requests time";
    private static final String AVERAGE_RESPONSE_SERIES_NAME = "Average response time";
    private static final String TOTAL_RESPONSE_SERIES_NAME = "Total response time";
    private static final String CHECK_RESPONSE_SERIES_NAME = "Check response time";

    private static final String DATA_PER_COMMUNICATION_ROUND= "Communication round";
    private static final String DATA_PER_A_NUMBER= "A number";

    public GWOTSCharts(String title, String type, String version) {
        super(title);

        XYSeriesCollection dataset = createDataset(type, version);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title + " (" + version + ")",
                type,
                "Time (sec.)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.setBackgroundPaint(Color.lightGray);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));

        JPanel controlPanel = new JPanel();
        for (Object object: dataset.getSeries()) {
            XYSeries series = (XYSeries) object;

            JCheckBox checkBox = new JCheckBox((String) series.getKey(), true);
            controlPanel.add(checkBox);
            checkBox.addActionListener(e ->
                    plot.getRenderer().setSeriesVisible(dataset.getSeriesIndex(checkBox.getText()), checkBox.isSelected()));
        }
        if (dataset.getSeries().size() > 3) {
            plot.getRenderer().setSeriesPaint(3, Color.orange);
        }

        getContentPane().add(chartPanel, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
    }

    private XYSeriesCollection createDatasetPerCommunicationRound(String version) {
        XYSeries buildRequestSeries = new XYSeries(BUILD_REQUESTS_SERIES_NAME);
        XYSeries averageResponseSeries = new XYSeries(AVERAGE_RESPONSE_SERIES_NAME);
        XYSeries totalResponseSeries = new XYSeries(TOTAL_RESPONSE_SERIES_NAME);
        XYSeries checkResponseSeries = new XYSeries(CHECK_RESPONSE_SERIES_NAME);

        GWOTSMeasures measures = new GWOTSMeasures("data/" + version + "_SHA-512_8_64_1000.txt");

        for (int i = 0; i < measures.size(); ++i) {
            GWOTSMeasureRecord measureRecord = measures.getRecord(i);

            buildRequestSeries.add(i, measureRecord.buildRequest());
            averageResponseSeries.add(i, measureRecord.responses().stream().mapToDouble(Double::doubleValue).sum()/measureRecord.responses().size());
            totalResponseSeries.add(i, measureRecord.responses().stream().mapToDouble(Double::doubleValue).sum());
            checkResponseSeries.add(i, measureRecord.checkResponses());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(buildRequestSeries);
        dataset.addSeries(averageResponseSeries);
        dataset.addSeries(totalResponseSeries);
        dataset.addSeries(checkResponseSeries);

        return dataset;
    }

    private XYSeriesCollection createDatasetPerANumber(String version) {
        XYSeries buildRequestSeries = new XYSeries(BUILD_REQUESTS_SERIES_NAME);
        XYSeries averageResponseSeries = new XYSeries(AVERAGE_RESPONSE_SERIES_NAME);
        XYSeries checkResponseSeries = new XYSeries(CHECK_RESPONSE_SERIES_NAME);

        for (int numberOfA = 1; numberOfA <= 64; numberOfA = numberOfA << 1) {
            GWOTSMeasures measures = new GWOTSMeasures("data/" + version + "_SHA-512_8_" + numberOfA + "_1000.txt");

            double averageBuildRequestTime = measures.getRecords().stream().mapToDouble(GWOTSMeasureRecord::buildRequest).sum()/measures.size();
            double averageResponseTime = measures.getRecords().stream()
                            .mapToDouble(record -> record.responses().stream().mapToDouble(Double::doubleValue).sum()/record.responses().size()).sum()
                    / measures.size();
            double averageCheckResponseTime = measures.getRecords().stream().mapToDouble(GWOTSMeasureRecord::checkResponses).sum()/measures.size();

            buildRequestSeries.add(numberOfA, averageBuildRequestTime);
            averageResponseSeries.add(numberOfA, averageResponseTime);
            checkResponseSeries.add(numberOfA, averageCheckResponseTime);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(buildRequestSeries);
        dataset.addSeries(averageResponseSeries);
        dataset.addSeries(checkResponseSeries);

        return dataset;
    }

    private XYSeriesCollection createDataset(String type, String version) {
        return switch (type) {
            case DATA_PER_COMMUNICATION_ROUND -> createDatasetPerCommunicationRound(version);
            case DATA_PER_A_NUMBER -> createDatasetPerANumber(version);
            default -> null;
        };
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            GWOTSCharts example = new GWOTSCharts("GWOTS", DATA_PER_A_NUMBER, GWOTS_PLUS);
            example.setSize(800, 600);
            example.setLocationRelativeTo(null);
            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            example.setVisible(true);
        });
    }
}
