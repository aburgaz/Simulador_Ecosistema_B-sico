package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import simulator.control.Controller;

public class MainWindow extends JFrame {
    private Controller _ctrl;

    public MainWindow(Controller ctrl) {
        super("[ECOSYSTEM SIMULATOR]");
        _ctrl = ctrl;
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());   // Create the main panel with a BorderLayout
        setContentPane(mainPanel);

        ControlPanel controlPanel = new ControlPanel(_ctrl);    // Create the control panel
        mainPanel.add(controlPanel, BorderLayout.PAGE_START);

        StatusBar statusBar = new StatusBar(_ctrl);   // Create the status bar
        mainPanel.add(statusBar, BorderLayout.PAGE_END);

        // Create the content panel for the tables
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // SPECIES TABLE
        InfoTable speciesTable = new InfoTable("Species", new SpeciesTableModel(_ctrl));
        contentPanel.setPreferredSize(new Dimension(500, 250));
        contentPanel.add(speciesTable, BorderLayout.BEFORE_FIRST_LINE);

        // REGIONS TABLE
        InfoTable regionsTable = new InfoTable("Regions", new RegionsTableModel(_ctrl));
        contentPanel.setPreferredSize(new Dimension(500, 250));
        contentPanel.add(regionsTable, BorderLayout.BEFORE_FIRST_LINE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ViewUtils.quit(MainWindow.this);
            }
        });

        // Window configuration
        //setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);   // Avoid closing the window directly
        pack();
        setVisible(true);
    }
}