package optionspanels;

import core.*;
import javax.swing.*;
import java.awt.*;

public class EdgeDetectionPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;

    private JComboBox<String> typeComboBox;
    private JPanel cannySettingsPanel;
    private JSpinner sigmaSpinner;
    private JSpinner lowThreshSpinner;
    private JSpinner highThreshSpinner;

    public EdgeDetectionPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Edge Detection:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel typePanel = new JPanel();
        typePanel.setMaximumSize(new Dimension(300, 40));

        String[] options = {"Sobel", "Roberts Cross", "Laplace", "Prewitt Compass", "Sobel Compass", "Canny"};
        typeComboBox = new JComboBox<>(options);
        typePanel.add(new JLabel("Operator: "));
        typePanel.add(typeComboBox);

        cannySettingsPanel = new JPanel();
        cannySettingsPanel.setLayout(new BoxLayout(cannySettingsPanel, BoxLayout.Y_AXIS));
        cannySettingsPanel.setMaximumSize(new Dimension(350, 120));

        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.setMaximumSize(new Dimension(350, 40));
        sigmaSpinner = new JSpinner(new SpinnerNumberModel(1.4, 0.1, 10.0, 0.1));
        p1.add(new JLabel("Blur Sigma (Noise Reduction): "));
        p1.add(sigmaSpinner);

        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.setMaximumSize(new Dimension(350, 40));
        lowThreshSpinner = new JSpinner(new SpinnerNumberModel(50, 0, 255, 1));
        p2.add(new JLabel("Low Threshold: "));
        p2.add(lowThreshSpinner);

        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p3.setMaximumSize(new Dimension(350, 40));
        highThreshSpinner = new JSpinner(new SpinnerNumberModel(150, 0, 255, 1));
        p3.add(new JLabel("High Threshold: "));
        p3.add(highThreshSpinner);

        cannySettingsPanel.add(p1);
        cannySettingsPanel.add(p2);
        cannySettingsPanel.add(p3);

        typeComboBox.addActionListener(e -> updateVisibility());
        updateVisibility();

        JButton applyBtn = new JButton("Apply");
        applyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        applyBtn.addActionListener(e -> {
            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            String operator = (String) typeComboBox.getSelectedItem();
            int[][][] newMatrix;

            if (operator.equals("Canny")) {
                newMatrix = ImageProcessor.applyCannyEdgeDetection(
                        photoPanel.getImageMatrix(),
                        (Double) sigmaSpinner.getValue(),
                        (Integer) lowThreshSpinner.getValue(),
                        (Integer) highThreshSpinner.getValue(),
                        parentPanel.getBoundaryMode()
                );
            } else {
                newMatrix = ImageProcessor.applyEdgeDetection(
                        photoPanel.getImageMatrix(),
                        operator,
                        parentPanel.getBoundaryMode()
                );
            }

            photoPanel.setImageMatrix(newMatrix);

            parentPanel.updateHistogram();
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(typePanel);
        this.add(Box.createVerticalStrut(10));
        this.add(cannySettingsPanel);
        this.add(Box.createVerticalStrut(10));
        this.add(applyBtn);
        this.add(Box.createVerticalGlue());
    }

    private void updateVisibility() {
        boolean isCanny = typeComboBox.getSelectedItem().equals("Canny");
        cannySettingsPanel.setVisible(isCanny);
        this.revalidate();
        this.repaint();
    }
}