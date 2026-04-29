package optionspanels;

import core.ImageProcessor;
import core.OptionPanel;
import core.PhotoPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class HistogramPanel extends JPanel{
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private int[][][] originalMatrix;

    private int[] histogramGray;
    private int[] histogramRed;
    private int[] histogramGreen;
    private int[] histogramBlue;

    private HistogramMode mode;

    public HistogramPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;
        this.mode = HistogramMode.BRIGHTNESS;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));

        this.originalMatrix = photoPanel.getImageMatrix();
        parentPanel.saveUndoState(originalMatrix);

        this.calculateHistograms();
        this.buildUI();
    }

    private void buildUI() {
        if (photoPanel.getImageMatrix() == null) return;
        this.add(Box.createVerticalStrut(20));

        if (mode == HistogramMode.BRIGHTNESS) {
            this.add(Box.createVerticalStrut(10));

            ChartPanel chartPanel = displayBrightnessHistogram(histogramGray, Color.WHITE, "Brightness");
            if (chartPanel != null) {
                chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(chartPanel);
            }
        }
        else if (mode == HistogramMode.COLOR) {
            ChartPanel chartRed = displayBrightnessHistogram(histogramRed, new Color(255, 100, 100), "Red Channel");
            ChartPanel chartGreen = displayBrightnessHistogram(histogramGreen, new Color(100, 255, 100), "Green Channel");
            ChartPanel chartBlue = displayBrightnessHistogram(histogramBlue, new Color(120, 180, 255), "Blue Channel");

            if (chartRed != null) {
                chartRed.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(chartRed);
                this.add(Box.createVerticalStrut(20));
            }
            if (chartGreen != null) {
                chartGreen.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(chartGreen);
                this.add(Box.createVerticalStrut(20));
            }
            if (chartBlue != null) {
                chartBlue.setAlignmentX(Component.CENTER_ALIGNMENT);
                this.add(chartBlue);
            }
        }

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setMaximumSize(new Dimension(350, 40));
        buttonsPanel.setOpaque(false);

        JButton histogramEqualizationBtn = new JButton("Histogram Equalization");
        histogramEqualizationBtn.addActionListener(e -> onHistogramEqualization());
        buttonsPanel.add(histogramEqualizationBtn);

        this.add(Box.createVerticalStrut(20));
        this.add(buttonsPanel);
        this.add(Box.createVerticalGlue());
    }

    private void calculateHistograms() {
        this.histogramGray = new int[256];
        this.histogramRed = new int[256];
        this.histogramGreen = new int[256];
        this.histogramBlue = new int[256];

        int[][][] matrix = photoPanel.getImageMatrix();
        if (matrix == null) return;

        int height = matrix.length;
        int width = matrix[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = matrix[y][x][0];
                int g = matrix[y][x][1];
                int b = matrix[y][x][2];

                this.histogramRed[r]++;
                this.histogramGreen[g]++;
                this.histogramBlue[b]++;

                int gray = (int)(r + g + b) / 3;
                this.histogramGray[gray]++;
            }
        }
    }

    private ChartPanel displayBrightnessHistogram(int[] histogram, Color color, String title) {
        XYSeries series = new XYSeries("Pixel amount");
        for (int i = 0; i < histogram.length; i++) {
            series.add(i, histogram[i]);
        }
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYAreaChart(
                title,
                "Color Value",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.DARK_GRAY);
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));
        plot.getRenderer().setSeriesPaint(0, color);

        chart.getTitle().setPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        org.jfree.chart.axis.ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelPaint(Color.WHITE);
        domainAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        domainAxis.setTickLabelPaint(Color.WHITE);
        domainAxis.setAxisLinePaint(Color.WHITE);
        domainAxis.setTickMarkPaint(Color.WHITE);

        org.jfree.chart.axis.ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelPaint(Color.WHITE);
        rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 12));
        rangeAxis.setTickLabelPaint(Color.WHITE);
        rangeAxis.setAxisLinePaint(Color.WHITE);
        rangeAxis.setTickMarkPaint(Color.WHITE);
        rangeAxis.setLowerMargin(0.0);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        if (histogram == histogramGray) {
            chartPanel.setPreferredSize(new Dimension(350, 300));
            chartPanel.setMaximumSize(new Dimension(350, 300));
        } else {
            chartPanel.setPreferredSize(new Dimension(350, 150));
            chartPanel.setMaximumSize(new Dimension(350, 150));
        }

        return chartPanel;
    }

    private void onHistogramEqualization() {
        originalMatrix = photoPanel.getImageMatrix();
        int[][][] newMatrix = ImageProcessor.applyHistogramEqualization(originalMatrix);
        photoPanel.setImageMatrix(newMatrix);
        refreshHistograms();
    }

    public void refreshHistograms() {
        this.removeAll();
        this.calculateHistograms();
        this.buildUI();
        this.revalidate();
        this.repaint();
    }

    public enum HistogramMode {
        BRIGHTNESS,
        COLOR
    }

    public void setMode(HistogramMode mode) {
        this.mode = mode;
        this.refreshHistograms();
    }
}