package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class CustomMaskPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private JFormattedTextField[][][] fieldsHolder = new JFormattedTextField[1][][];

    public CustomMaskPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Custom Mask Filter:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel sizePanel = new JPanel();
        sizePanel.setMaximumSize(new Dimension(300, 40));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(3, 3, 9, 2));
        sizePanel.add(new JLabel("Mask Size (NxN): "));
        sizePanel.add(sizeSpinner);

        JPanel gridContainer = new JPanel();
        gridContainer.setLayout(new BoxLayout(gridContainer, BoxLayout.Y_AXIS));

        Runnable buildGrid = () -> {
            gridContainer.removeAll();
            int size = (int) sizeSpinner.getValue();
            JPanel gridPanel = new JPanel(new GridLayout(size, size, 2, 2));

            int requiredGridWidth = size * 50;
            int requiredPanelWidth = Math.max(400, requiredGridWidth + 60);
            parentPanel.setPreferredSize(new Dimension(requiredPanelWidth, 0));

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.revalidate();

            gridPanel.setMaximumSize(new Dimension(requiredGridWidth, requiredGridWidth));
            DecimalFormat df = new DecimalFormat("0.#");

            JFormattedTextField[][] fields = new JFormattedTextField[size][size];
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    fields[r][c] = new JFormattedTextField(df);
                    fields[r][c].setValue(0.0);
                    fields[r][c].setHorizontalAlignment(JTextField.CENTER);
                    gridPanel.add(fields[r][c]);
                }
            }
            fields[size / 2][size / 2].setValue(1.0);
            fieldsHolder[0] = fields;

            gridContainer.add(gridPanel);
            gridContainer.revalidate();
            gridContainer.repaint();

            parentPanel.updateHistogram();
        };

        sizeSpinner.addChangeListener(e -> buildGrid.run());
        buildGrid.run();

        JButton applyBtn = new JButton("Apply");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        applyBtn.addActionListener(e -> {
            int size = (int) sizeSpinner.getValue();
            double[][] customMask = new double[size][size];
            JFormattedTextField[][] fields = fieldsHolder[0];

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    Number num = (Number) fields[r][c].getValue();
                    customMask[r][c] = num != null ? num.doubleValue() : 0.0;
                }
            }

            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] newMatrix = ImageProcessor.applyConvolution(
                    photoPanel.getImageMatrix(), customMask, parentPanel.getBoundaryMode());
            photoPanel.setImageMatrix(newMatrix);

            parentPanel.updateHistogram();
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(sizePanel);
        this.add(Box.createVerticalStrut(15));
        this.add(gridContainer);
        this.add(Box.createVerticalStrut(20));
        this.add(applyBtn);
        this.add(Box.createVerticalGlue());
    }
}