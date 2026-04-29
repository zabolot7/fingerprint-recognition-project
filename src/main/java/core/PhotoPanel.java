package core;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The main UI panel, responsible for displaying the image.
 * Renders the scaled image, maintains its 3D image matrix, and draws projection charts if enabled.
 */
public class PhotoPanel extends JPanel {

    private BufferedImage image;
    private Image scaledImage;
    private int[][][] imageMatrix;

    private JPanel wrapperPanel;
    private JPanel imageCanvas;
    private ChartPanel topChartPanel;
    private ChartPanel sideChartPanel;
    private boolean showProjections = false;

    /**
     * Initializes PhotoPanel.
     */
    public PhotoPanel() {
        super();
        this.setLayout(new GridBagLayout());
        this.setBackground(UIManager.getColor("Panel.background"));

        wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setOpaque(false);

        imageCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (scaledImage != null) {
                    g.drawImage(scaledImage, 0, 0, this);
                }
            }
        };
        imageCanvas.setOpaque(false);

        topChartPanel = createEmptyChart(PlotOrientation.VERTICAL);
        sideChartPanel = createEmptyChart(PlotOrientation.HORIZONTAL);

        topChartPanel.setVisible(false);
        sideChartPanel.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        wrapperPanel.add(topChartPanel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        JPanel corner = new JPanel();
        corner.setOpaque(false);
        wrapperPanel.add(corner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        wrapperPanel.add(imageCanvas, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        wrapperPanel.add(sideChartPanel, gbc);

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.anchor = GridBagConstraints.NORTHWEST;
        gbcMain.weightx = 1.0;
        gbcMain.weighty = 1.0;

        this.add(wrapperPanel, gbcMain);

        File imageFile = new File("src/testImage.jpg");
        try {
            image = ImageIO.read(imageFile);
            createImageMatrix();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalculateSize();
            }
        });
    }

    /**
     * Toggles the visibility of projection charts on the edges of the image.
     *
     * @param show True to display projections, false to hide.
     */
    public void setShowProjections(boolean show) {
        this.showProjections = show;
        topChartPanel.setVisible(show);
        sideChartPanel.setVisible(show);
        recalculateSize();
    }

    /**
     * Initializes an empty chart for displaying image projections.
     *
     * @param orientation The layout orientation (VERTICAL for top chart, HORIZONTAL for side chart).
     * @return A configured ChartPanel containing the empty projection plot.
     */
    private ChartPanel createEmptyChart(PlotOrientation orientation) {
        XYSeries series = new XYSeries("Projection");
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYAreaChart(
                null, null, null, dataset, orientation, false, false, false
        );

        chart.getPlot().setBackgroundPaint(Color.DARK_GRAY);
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));

        chart.setPadding(new RectangleInsets(0, 0, 0, 0));

        XYPlot plot = chart.getXYPlot();
        plot.setInsets(new RectangleInsets(0, 0, 0, 0));
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        plot.setOutlineVisible(false);

        plot.getDomainAxis().setVisible(false);
        plot.getRangeAxis().setVisible(false);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);
        plot.getRangeAxis().setLowerMargin(0.0);

        plot.getRenderer().setSeriesPaint(0, new Color(100, 200, 255, 180));

        ChartPanel cp = new ChartPanel(chart);
        cp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return cp;
    }

    /**
     * Updates the projection charts based on the current image.
     */
    public void updateProjections() {
        int[][] projections = ImageProcessor.getProjections(imageMatrix);
        updateProjectionCharts(projections[0], projections[1]);
    }

    /**
     * A helper function that updates the top and side projection charts.
     *
     * @param verticalProj An array representing the vertical column sums of black pixels.
     * @param horizontalProj An array representing the horizontal row sums of black pixels.
     */
    public void updateProjectionCharts(int[] verticalProj, int[] horizontalProj) {
        if (topChartPanel != null) {
            if (verticalProj != null) {
                XYSeries vSeries = new XYSeries("Vertical");
                for (int x = 0; x < verticalProj.length; x++) {
                    vSeries.add(x, verticalProj[x]);
                }
                org.jfree.chart.plot.XYPlot topPlot = topChartPanel.getChart().getXYPlot();
                topPlot.setDataset(new XYSeriesCollection(vSeries));
            } else {
                topChartPanel.getChart().getXYPlot().setDataset(null);
            }
        }

        if (sideChartPanel != null) {
            if (horizontalProj != null) {
                XYSeries hSeries = new XYSeries("Horizontal");
                for (int y = 0; y < horizontalProj.length; y++) {
                    hSeries.add(y, horizontalProj[y]);
                }
                org.jfree.chart.plot.XYPlot sidePlot = sideChartPanel.getChart().getXYPlot();
                sidePlot.setDataset(new XYSeriesCollection(hSeries));
                sidePlot.getDomainAxis().setInverted(true);
            } else {
                sideChartPanel.getChart().getXYPlot().setDataset(null);
            }
        }
    }

    /**
     * Recalculates the scaling of the image and projection charts to ensure they fit
     * the available window space with correct aspect ratio.
     */
    public void recalculateSize() {
        if (image == null) return;

        int availW = this.getWidth();
        int availH = this.getHeight();

        if (availW <= 0 || availH <= 0) return;

        int chartThicknessX = showProjections ? 150 : 0;
        int chartThicknessY = showProjections ? 100 : 0;

        int maxImgW = availW - chartThicknessX;
        int maxImgH = availH - chartThicknessY;

        if (maxImgW <= 0 || maxImgH <= 0) return;

        double scale = Math.min((double) maxImgW / image.getWidth(), (double) maxImgH / image.getHeight());

        int scaledW = (int) Math.round(image.getWidth() * scale);
        int scaledH = (int) Math.round(image.getHeight() * scale);

        scaledImage = image.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);

        Dimension imgDim = new Dimension(scaledW, scaledH);
        imageCanvas.setPreferredSize(imgDim);
        imageCanvas.setMinimumSize(imgDim);
        imageCanvas.setMaximumSize(imgDim);

        Dimension topDim = new Dimension(scaledW, chartThicknessY);
        topChartPanel.setPreferredSize(topDim);
        topChartPanel.setMinimumSize(topDim);
        topChartPanel.setMaximumSize(topDim);

        Dimension sideDim = new Dimension(chartThicknessX, scaledH);
        sideChartPanel.setPreferredSize(sideDim);
        sideChartPanel.setMinimumSize(sideDim);
        sideChartPanel.setMaximumSize(sideDim);

        wrapperPanel.revalidate();
        wrapperPanel.repaint();
    }

    /**
     * Loads a new image from the hard drive, processes it into a matrix, and renders it.
     *
     * @param filepath The absolute path to the image file.
     */
    public void changeImage(String filepath){
        File imageFile = new File(filepath);
        try {
            image = ImageIO.read(imageFile);
            createImageMatrix();
            recalculateSize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a BufferedImage into a 3D pixel array [Y][X][RGB].
     */
    private void createImageMatrix() {
        int width = image.getWidth();
        int height = image.getHeight();
        imageMatrix = new int[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                imageMatrix[y][x][0] = (rgb >> 16) & 0xFF;
                imageMatrix[y][x][1] = (rgb >> 8) & 0xFF;
                imageMatrix[y][x][2] = rgb & 0xFF;
            }
        }
    }

    /**
     * Retrieves the current image matrix.
     *
     * @return The 3D array [Y][X][RGB].
     */
    public int[][][] getImageMatrix() {
        return imageMatrix;
    }

    /**
     * Updates the image matrix and redraws the BufferedImage to screen.
     *
     * @param imageMatrix The new 3D pixel array to display.
     */
    public void setImageMatrix(int[][][] imageMatrix) {
        this.imageMatrix = imageMatrix;
        int height = imageMatrix.length;
        int width = imageMatrix[0].length;

        if (image.getWidth() != width || image.getHeight() != height) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            recalculateSize();
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = imageMatrix[y][x][0];
                int g = imageMatrix[y][x][1];
                int b = imageMatrix[y][x][2];
                int rgb = (255 << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }

        if (imageCanvas.getPreferredSize().width > 0) {
            scaledImage = image.getScaledInstance(imageCanvas.getPreferredSize().width, imageCanvas.getPreferredSize().height, Image.SCALE_SMOOTH);
        }
        if (imageCanvas != null) imageCanvas.repaint();
    }

    /**
     * Retrieves the BufferedImage object for the current image; mostly used for file saving.
     *
     * @return The current BufferedImage.
     */
    public BufferedImage getBufferedImage() {
        return this.image;
    }
}