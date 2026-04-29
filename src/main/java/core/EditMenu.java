package core;

import optionspanels.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * Constructs the "Edit" dropdown menu for the top menu bar.
 * Handles instant actions (Undo, Revert, Negative) and opens tool panels in the sidebar for operations that require
 * additional user input.
 */
public class EditMenu extends JMenu {
    private PhotoPanel photoPanel;
    private int[][][] originalImageMatrix;
    private int[][][] lastImageMatrix;
    private OptionPanel optionPanel;

    /**
     * Constructs the Edit menu.
     *
     * @param s The title of the menu (e.g., "Edit").
     * @param photoPanel The main image display panel.
     * @param lastImageMatrix The previous image state matrix (for Undo).
     * @param optionPanel The sidebar panel where tool UIs will be shown.
     * @param originalImageMatrix The matrix representing the original version of the image (for Revert).
     */
    public EditMenu(String s, PhotoPanel photoPanel, int[][][] lastImageMatrix, OptionPanel optionPanel, int[][][] originalImageMatrix) {
        super(s);
        this.photoPanel = photoPanel;
        this.lastImageMatrix = lastImageMatrix;
        this.optionPanel = optionPanel;
        this.originalImageMatrix = originalImageMatrix;

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        JMenuItem revertItem = new JMenuItem("Revert to the original state");
        JMenuItem morphologyItem = new JMenuItem("Morphology operations");

        // pixel operations menu
        JMenu pixelOperationsMenu = new JMenu("Pixel operations");
        JMenuItem grayScaleItem = new JMenuItem("Grayscale");
        JMenuItem negativeItem = new JMenuItem("Negative");
        JMenuItem brightnessItem = new JMenuItem("Brightness");
        JMenuItem contrastItem = new JMenuItem("Contrast");
        JMenuItem binarizationItem = new JMenuItem("Binarization");

        pixelOperationsMenu.add(grayScaleItem);
        pixelOperationsMenu.add(negativeItem);
        pixelOperationsMenu.add(brightnessItem);
        pixelOperationsMenu.add(contrastItem);
        pixelOperationsMenu.add(binarizationItem);

        // graphic filters menu
        JMenu graphicFiltersMenu = new JMenu("Graphic filters");
        JMenuItem blurringItem = new JMenuItem("Blurring");
        JMenuItem sharpeningItem = new JMenuItem("Sharpening");
        JMenuItem edgeDetectionItem = new JMenuItem("Edge detection");
        JMenuItem customMaskItem = new JMenuItem("Custom mask");

        graphicFiltersMenu.add(blurringItem);
        graphicFiltersMenu.add(sharpeningItem);
        graphicFiltersMenu.add(edgeDetectionItem);
        graphicFiltersMenu.add(customMaskItem);

        // instant actions
        undoItem.addActionListener(e -> onUndo());
        revertItem.addActionListener(e -> onRevert());
        negativeItem.addActionListener(e -> onNegative());

        grayScaleItem.addActionListener(e -> optionPanel.loadToolPanel(new GrayscalePanel(photoPanel, optionPanel)));
        brightnessItem.addActionListener(e -> optionPanel.loadToolPanel(new BrightnessPanel(photoPanel, optionPanel)));
        contrastItem.addActionListener(e -> optionPanel.loadToolPanel(new ContrastPanel(photoPanel, optionPanel)));
        binarizationItem.addActionListener(e -> optionPanel.loadToolPanel(new BinarizationPanel(photoPanel, optionPanel)));
        blurringItem.addActionListener(e -> optionPanel.loadToolPanel(new BlurringPanel(photoPanel, optionPanel)));
        sharpeningItem.addActionListener(e -> optionPanel.loadToolPanel(new SharpeningPanel(photoPanel, optionPanel)));
        edgeDetectionItem.addActionListener(e -> optionPanel.loadToolPanel(new EdgeDetectionPanel(photoPanel, optionPanel)));
        customMaskItem.addActionListener(e -> optionPanel.loadToolPanel(new CustomMaskPanel(photoPanel, optionPanel)));
        morphologyItem.addActionListener(e -> optionPanel.loadToolPanel(new MorphologyPanel(photoPanel, optionPanel)));

        this.add(undoItem);
        this.add(revertItem);
        this.add(pixelOperationsMenu);
        this.add(graphicFiltersMenu);
        this.add(morphologyItem);
    }

    /**
     * Restores the image to its previous state (1 step back) and updates the histogram.
     */
    private void onUndo() {
        int[][][] temp = photoPanel.getImageMatrix();
        photoPanel.setImageMatrix(lastImageMatrix);
        lastImageMatrix = temp;
        optionPanel.updateHistogram();
    }

    /**
     * Reverts the image back to its original unmodified state (as it was when first imported).
     */
    private void onRevert() {
        int[][][] temp = photoPanel.getImageMatrix();
        photoPanel.setImageMatrix(originalImageMatrix);
        lastImageMatrix = temp;
        optionPanel.updateHistogram();
        optionPanel.refreshOnImport();
    }

    /**
     * Applies a negative filter to the current image.
     */
    private void onNegative() {
        lastImageMatrix = photoPanel.getImageMatrix();
        int[][][] newMatrix = ImageProcessor.applyNegative(lastImageMatrix);
        photoPanel.setImageMatrix(newMatrix);
        optionPanel.updateHistogram();
    }

    /**
     * Updates the most recent image matrix state (saved for Undo).
     *
     * @param newMatrix The 3D array representing the most recent previous state of our image.
     */
    public void setLastImageMatrix(int[][][] newMatrix) {
        this.lastImageMatrix = newMatrix;
    }

    /**
     * Updates the saved original image (for Revert).
     *
     * @param newMatrix The 3D array representing the original image.
     */
    public void setOriginalImageMatrix(int[][][] newMatrix) {
        this.originalImageMatrix = newMatrix;
    }
}
