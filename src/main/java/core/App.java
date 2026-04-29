package core;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

/**
 * The main entry point for the Image Processing Application.
 * Initializes the FlatLaf dark theme, constructs the main application window,
 * and assembles the core UI components (PhotoPanel, OptionPanel, MenuBar).
 */
public class App {
    /**
     * Main method. Sets up the main app frame and initializes its core components.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Default Image");
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            PhotoPanel photoPanel = new PhotoPanel();

            OptionPanel optionPanel = new OptionPanel(photoPanel);
            optionPanel.setPreferredSize(new Dimension(400, 0));

            MenuBar menuBar = new MenuBar(frame, photoPanel, optionPanel);
            frame.setJMenuBar(menuBar);

            frame.add(photoPanel, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.add(optionPanel, BorderLayout.EAST);

            frame.addWindowStateListener(e -> {
                photoPanel.recalculateSize();
            });

            frame.setVisible(true);

            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setResizable(false);
        });
    }
}