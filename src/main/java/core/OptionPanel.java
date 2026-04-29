package core;

import optionspanels.HistogramPanel.HistogramMode;
import optionspanels.HistogramPanel;

import javax.swing.*;
import java.awt.*;

/**
 * The sidebar panel for additional user input for various image editing features.
 * At the top, it displays a Tool Area for the particular image editing feature selected from Edit menu.
 * At the bottom, it displays an image histogram (whose version depends on whatever the user selects from Display menu).
 */
public class OptionPanel extends JPanel {

    /**
     * Defines how convolution filters handle pixels outside the image.
     */
    public enum BoundaryMode {
        CROP, KEEP_ORIGINAL, PAD_BLACK, PAD_WHITE, PAD_GRAY, REPLICATE, MIRROR
    }

    private PhotoPanel photoPanel;
    private EditMenu editMenu;
    private BoundaryMode currentBoundaryMode = BoundaryMode.REPLICATE;
    private JPanel toolArea;
    private HistogramPanel histogramPanel;

    /**
     * Constructs the sidebar panel with a top tool area and a bottom histogram.
     *
     * @param photoPanel The image panel controlled / referenced by this option panel.
     */
    public OptionPanel(PhotoPanel photoPanel) {
        this.photoPanel = photoPanel;

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(400, 0));

        toolArea = new JPanel(new BorderLayout());
        this.add(toolArea, BorderLayout.NORTH);

        histogramPanel = new HistogramPanel(photoPanel, this);
        this.add(histogramPanel, BorderLayout.CENTER);
    }

    /**
     * Links the EditMenu to this panel.
     *
     * @param editMenu The application's Edit menu.
     */
    public void setEditMenu(EditMenu editMenu) {
        this.editMenu = editMenu;
    }

    /**
     * Updates the current boundary handling mode.
     *
     * @param mode The selected BoundaryMode enum.
     */
    public void setBoundaryMode(BoundaryMode mode) {
        this.currentBoundaryMode = mode;
    }

    /**
     * Retrieves the current boundary handling mode.
     *
     * @return The active BoundaryMode enum.
     */
    public BoundaryMode getBoundaryMode() {
        return currentBoundaryMode;
    }

    /**
     * Helper method to push a new undo state to the Edit menu.
     *
     * @param matrixToSave The image matrix before the tool applies changes.
     */
    public void saveUndoState(int[][][] matrixToSave) {
        if (editMenu != null && matrixToSave != null) {
            editMenu.setLastImageMatrix(matrixToSave);
        }
    }

    /**
     * Clears the tool area and forces the histogram to recalculate when a new image is loaded.
     */
    public void refreshOnImport() {
        toolArea.removeAll();
        if (histogramPanel != null) {
            histogramPanel.refreshHistograms();
        }
        this.revalidate();
        this.repaint();
    }

    /**
     * Swaps out the currently active tool interface for a new one when the user selects a new one from the EditMenu.
     *
     * @param toolPanel The JPanel containing the UI for the newly selected tool.
     */
    public void loadToolPanel(JPanel toolPanel) {
        toolArea.removeAll();

        // always reset width to 400
        //this.setPreferredSize(new Dimension(400, 0));

        toolArea.add(toolPanel, BorderLayout.CENTER);

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.revalidate();
        }

        toolArea.revalidate();
        toolArea.repaint();
    }

    /**
     * Redraws the histogram based on the current image matrix.
     */
    public void updateHistogram() {
        if (histogramPanel != null) {
            histogramPanel.refreshHistograms();
        }
        photoPanel.updateProjections();
    }

    /**
     * Changes the display mode of the histogram (Brightness vs. RGB).
     *
     * @param mode The selected HistogramMode.
     */
    public void setHistogramMode(HistogramMode mode) {
        if (histogramPanel != null) {
            histogramPanel.setMode(mode);
        }
        updateHistogram();
    }
}