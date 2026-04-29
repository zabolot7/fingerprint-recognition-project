package optionspanels;

import core.*;
import javax.swing.*;
import java.awt.*;

public class GrayscalePanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;

    private JComboBox<String> typeComboBox;
    private JComboBox<String> decompComboBox;
    private JComboBox<String> colorComboBox;
    private JSpinner shadesSpinner;
    private JCheckBox ditherCheckBox;

    public GrayscalePanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Grayscale:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel typePanel = new JPanel();
        typePanel.setMaximumSize(new Dimension(300, 40));
        String[] options = {"Averaging", "Luminance", "Desaturation", "Decomposition", "Single color channel", "Custom"};
        typeComboBox = new JComboBox<>(options);
        typePanel.add(new JLabel("Algorithm: "));
        typePanel.add(typeComboBox);

        // Decomposition Panel
        JPanel decompPanel = new JPanel();
        decompPanel.setMaximumSize(new Dimension(300, 40));
        String[] decompOptions = {"Maximum", "Minimum"};
        decompComboBox = new JComboBox<>(decompOptions);
        decompPanel.add(new JLabel("Type: "));
        decompPanel.add(decompComboBox);
        decompPanel.setVisible(false);

        // Color Channel Panel
        JPanel colorPanel = new JPanel();
        colorPanel.setMaximumSize(new Dimension(300, 40));
        String[] colorOptions = {"Red", "Green", "Blue"};
        colorComboBox = new JComboBox<>(colorOptions);
        colorPanel.add(new JLabel("Color channel: "));
        colorPanel.add(colorComboBox);
        colorPanel.setVisible(false);

        // Custom Panel
        JPanel customPanel = new JPanel();
        customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.Y_AXIS));
        customPanel.setMaximumSize(new Dimension(300, 70));

        JPanel shadesPanel = new JPanel();
        shadesPanel.add(new JLabel("Number of shades: "));
        shadesSpinner = new JSpinner(new SpinnerNumberModel(10, 2, 256, 1));
        shadesPanel.add(shadesSpinner);

        JPanel ditherPanel = new JPanel();
        ditherCheckBox = new JCheckBox("Dithered");
        ditherPanel.add(ditherCheckBox);

        customPanel.add(shadesPanel);
        customPanel.add(ditherPanel);
        customPanel.setVisible(false);

        typeComboBox.addActionListener(e -> {
            String selected = (String) typeComboBox.getSelectedItem();

            decompPanel.setVisible("Decomposition".equals(selected));
            colorPanel.setVisible("Single color channel".equals(selected));
            customPanel.setVisible("Custom".equals(selected));

            this.revalidate();
            this.repaint();
        });

        // Apply
        JButton applyBtn = new JButton("Apply");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        applyBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            String operator = (String) typeComboBox.getSelectedItem();
            GrayscaleOptions option = GrayscaleOptions.LUMINANCE;

            int shades = 256;

            if (operator.equals("Luminance")) {
                option = GrayscaleOptions.LUMINANCE;
            } else if (operator.equals("Averaging")) {
                option = GrayscaleOptions.AVERAGING;
            } else if (operator.equals("Desaturation")) {
                option = GrayscaleOptions.DESATURATION;
            } else if (operator.equals("Decomposition")) {
                String decompType = (String) decompComboBox.getSelectedItem();
                if ("Maximum".equals(decompType)) {
                    option = GrayscaleOptions.DECOMPOSITION_MAX;
                } else if ("Minimum".equals(decompType)) {
                    option = GrayscaleOptions.DECOMPOSITION_MIN;
                }
            } else if (operator.equals("Single color channel")) {
                String color = (String) colorComboBox.getSelectedItem();
                if ("Red".equals(color)) {
                    option = GrayscaleOptions.SINGLE_RED;
                } else if ("Green".equals(color)) {
                    option = GrayscaleOptions.SINGLE_GREEN;
                } else if ("Blue".equals(color)) {
                    option = GrayscaleOptions.SINGLE_BLUE;
                }
            } else if (operator.equals("Custom")) {
                shades = (Integer) shadesSpinner.getValue();
                if (ditherCheckBox.isSelected()) {
                    option = GrayscaleOptions.CUSTOM_DITHERED;
                } else {
                    option = GrayscaleOptions.CUSTOM;
                }
            }

            int[][][] lastImageMatrix = photoPanel.getImageMatrix();
            int[][][] newMatrix;

            if (option == GrayscaleOptions.CUSTOM || option == GrayscaleOptions.CUSTOM_DITHERED) {
                newMatrix = ImageProcessor.applyGrayscale(lastImageMatrix, option, shades);
            } else {
                newMatrix = ImageProcessor.applyGrayscale(lastImageMatrix, option);
            }

            photoPanel.setImageMatrix(newMatrix);
            parentPanel.updateHistogram();
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(typePanel);
        this.add(decompPanel);
        this.add(colorPanel);
        this.add(customPanel);
        this.add(Box.createVerticalStrut(10));
        this.add(applyBtn);
        this.add(Box.createVerticalGlue());
    }

    public enum GrayscaleOptions {
        AVERAGING,
        LUMINANCE,
        DESATURATION,
        DECOMPOSITION_MAX,
        DECOMPOSITION_MIN,
        SINGLE_RED,
        SINGLE_GREEN,
        SINGLE_BLUE,
        CUSTOM,
        CUSTOM_DITHERED
    }
}