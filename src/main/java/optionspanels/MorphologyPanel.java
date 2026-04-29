package optionspanels;

import core.*;

import javax.swing.*;
import java.awt.*;

public class MorphologyPanel extends JPanel {
    private PhotoPanel photoPanel;
    private OptionPanel parentPanel;
    private JCheckBox[][][] checkBoxesHolder = new JCheckBox[1][][];

    public MorphologyPanel(PhotoPanel photoPanel, OptionPanel parentPanel) {
        this.photoPanel = photoPanel;
        this.parentPanel = parentPanel;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        buildUI();
    }

    private void buildUI() {
        JLabel titleLabel = new JLabel("Morphology operations:");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel opPanel = new JPanel();
        opPanel.setMaximumSize(new Dimension(300, 40));
        String[] operations = {"Erosion", "Dilation", "Opening", "Closing"};
        JComboBox<String> opComboBox = new JComboBox<>(operations);
        opPanel.add(new JLabel("Operation: "));
        opPanel.add(opComboBox);

        JPanel sizePanel = new JPanel();
        sizePanel.setMaximumSize(new Dimension(300, 40));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(3, 3, 9, 2)); // tylko nieparzyste
        sizePanel.add(new JLabel("Structuring Element Size (NxN): "));
        sizePanel.add(sizeSpinner);

        JPanel gridContainer = new JPanel();
        gridContainer.setLayout(new BoxLayout(gridContainer, BoxLayout.Y_AXIS));

        Runnable buildGrid = () -> {
            gridContainer.removeAll();
            int size = (int) sizeSpinner.getValue();
            JPanel gridPanel = new JPanel(new GridLayout(size, size, 2, 2));

            int requiredGridWidth = size * 30;
            int requiredPanelWidth = Math.max(400, requiredGridWidth + 60);
            parentPanel.setPreferredSize(new Dimension(requiredPanelWidth, 0));

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.revalidate();

            gridPanel.setMaximumSize(new Dimension(requiredGridWidth, requiredGridWidth));

            JCheckBox[][] checkBoxes = new JCheckBox[size][size];
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    checkBoxes[r][c] = new JCheckBox();
                    checkBoxes[r][c].setHorizontalAlignment(SwingConstants.CENTER);
                    checkBoxes[r][c].setSelected(true);
                    gridPanel.add(checkBoxes[r][c]);
                }
            }

            checkBoxesHolder[0] = checkBoxes;

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
            boolean[][] se = new boolean[size][size];
            JCheckBox[][] checkBoxes = checkBoxesHolder[0];

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    se[r][c] = checkBoxes[r][c].isSelected();
                }
            }

            parentPanel.saveUndoState(photoPanel.getImageMatrix());
            int[][][] currentMatrix = photoPanel.getImageMatrix();
            int[][][] newMatrix = null;

            OptionPanel.BoundaryMode boundaryMode = parentPanel.getBoundaryMode();
            String selectedOp = (String) opComboBox.getSelectedItem();

            if ("Erosion".equals(selectedOp)) {
                newMatrix = ImageProcessor.applyErosion(currentMatrix, se, boundaryMode);
            }
            else if ("Dilation".equals(selectedOp)) {
                newMatrix = ImageProcessor.applyDilation(currentMatrix, se, boundaryMode);
            }
            else if ("Opening".equals(selectedOp)) {
                int[][][] temp = ImageProcessor.applyErosion(currentMatrix, se, boundaryMode);
                newMatrix = ImageProcessor.applyDilation(temp, se, boundaryMode);
            }
            else if ("Closing".equals(selectedOp)) {
                int[][][] temp = ImageProcessor.applyDilation(currentMatrix, se, boundaryMode);
                newMatrix = ImageProcessor.applyErosion(temp, se, boundaryMode);
            }

            if (newMatrix != null) {
                photoPanel.setImageMatrix(newMatrix);
                parentPanel.updateHistogram();
            }
        });

        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));
        this.add(opPanel);
        this.add(Box.createVerticalStrut(10));
        this.add(sizePanel);
        this.add(Box.createVerticalStrut(15));
        this.add(gridContainer);
        this.add(Box.createVerticalStrut(20));
        this.add(applyBtn);
        this.add(Box.createVerticalGlue());
    }
}