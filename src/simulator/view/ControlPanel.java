package simulator.view;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;

import org.json.JSONObject;
import org.json.JSONTokener;
import resources.examples.Examples;
import resources.icons.Icons;
import simulator.control.Controller;
import simulator.launcher.Main;

class ControlPanel extends JPanel {
    private Controller _ctrl;
    private ChangeRegionsDialog _changeRegionsDialog;
    private JToolBar _toolaBar;
    private JFileChooser _fc;
    private boolean _stopped = true; // utilizado en los botones de run/stop
    private JButton _quitButton;
    private JButton _openButton;
    private JButton _regionsButton;
    private JButton _viewerButton;
    private JButton _runButton;
    private JButton _stopButton;

    private JSpinner _stepsSpinner;
    private JTextField _deltaTimeField;
    private int _steps = 10000;

    public ControlPanel(Controller ctrl) {
        _ctrl = ctrl;
        _changeRegionsDialog = new ChangeRegionsDialog(_ctrl);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        _toolaBar = new JToolBar();
        add(_toolaBar, BorderLayout.PAGE_START);

        _toolaBar.add(Box.createGlue()); // this aligns the button to the right
        // Open Button
        _openButton = new JButton();
        _openButton.setToolTipText("Load a file");
        _openButton.setIcon(new ImageIcon(Icons.class.getResource("open.png")));    // Path for jar file
        _openButton.addActionListener((e) -> {
            try {
                load_file();
            } catch (Exception ex) {
                ViewUtils.showErrorMsg(this, "Unable to read the JSON file");
            }
        });
        _toolaBar.add(_openButton);

        _toolaBar.addSeparator();

        // Viewer Button
        _viewerButton = new JButton();
        _viewerButton.setToolTipText("Show Map");
        _viewerButton.setIcon(new ImageIcon(Icons.class.getResource("viewer.png")));    // Path for jar file
        _viewerButton.addActionListener((e) -> mapWindow());
        _toolaBar.add(_viewerButton);

        // Regions Button
        _regionsButton = new JButton();
        _regionsButton.setToolTipText("Change Regions");
        _regionsButton.setIcon(new ImageIcon(Icons.class.getResource("regions.png")));      // Path for jar file
        _regionsButton.addActionListener((e) -> changeRegions());
        _toolaBar.add(_regionsButton);

        _toolaBar.addSeparator();

        // Run Button
        _runButton = new JButton();
        _runButton.setToolTipText("Run");
        _runButton.setIcon(new ImageIcon(Icons.class.getResource("run.png")));      // Path for jar file
        _runButton.addActionListener((e) -> run());
        _toolaBar.add(_runButton);

        // Stop Button
        _stopButton = new JButton();
        _stopButton.setToolTipText("Stop");
        _stopButton.setIcon(new ImageIcon(Icons.class.getResource("stop.png")));    // Path for jar file
        _stopButton.setEnabled(false);
        _stopButton.addActionListener((e) -> stop_sim());
        _toolaBar.add(_stopButton);

        // JSpinner Steps
        _stepsSpinner = new JSpinner(new SpinnerNumberModel(_steps, 1000, 60000, 100));
        _stepsSpinner.setToolTipText("Simulation steps between each update");
        JComponent editor = _stepsSpinner.getEditor();
        JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
        tf.setColumns(4);
        _stepsSpinner.setMaximumSize(new Dimension(100, 30));
        _toolaBar.add(new JLabel("Steps:"));
        _toolaBar.add(_stepsSpinner);

        // Delta Time Field
        _deltaTimeField = new JTextField(Main._dtime.toString());
        _deltaTimeField.setToolTipText("Modify Delta Time");
        _toolaBar.add(new JLabel("Delta-Time:"));
        _toolaBar.add(_deltaTimeField);

        _toolaBar.addSeparator();

        // Quit Button
        _quitButton = new JButton();
        _quitButton.setToolTipText("Quit");
        _quitButton.setIcon(new ImageIcon(Icons.class.getResource("exit.png")));    // Path for jar file
        _quitButton.addActionListener((e) -> ViewUtils.quit(this));
        _toolaBar.add(_quitButton);

        try {    // Set the current directory to the examples folder
            _fc = new JFileChooser();
            _fc.setCurrentDirectory(new File(Examples.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        _changeRegionsDialog = new ChangeRegionsDialog(_ctrl);
    }

    private void load_file() throws Exception {
        int returnVal = _fc.showOpenDialog(ViewUtils.getWindow(this));     // Open the file chooser
        if (returnVal == JFileChooser.APPROVE_OPTION) {     // If the user selects a file
            File file = _fc.getSelectedFile();
            InputStream is = new FileInputStream(file);
            JSONObject jo = new JSONObject(new JSONTokener(is));    // Read the JSON file

            int width = jo.getInt("width");
            int height = jo.getInt("height");
            int rows = jo.getInt("rows");
            int cols = jo.getInt("cols");

            _ctrl.reset(cols, rows, width, height);     // Reset the controller
            _ctrl.load_data(jo);     // Load the data from the JSON file
        }
    }

    private void mapWindow() {   // Open the map window
        MapWindow map = new MapWindow(ViewUtils.getWindow(this), _ctrl);
        map.setVisible(true);
    }

    private void changeRegions() {   // Open the change regions dialog
        _changeRegionsDialog.open(ViewUtils.getWindow(this));
    }

    private void run() {    // Run the simulation
        // Step 1: disable buttons
        _openButton.setEnabled(false);
        _viewerButton.setEnabled(false);
        _regionsButton.setEnabled(false);
        _runButton.setEnabled(false);
        _stopButton.setEnabled(true);
        _stopped = false;

        // Step 2: read steps and delta-time
        try {
            _steps = (int) _stepsSpinner.getValue();
            double dt = Double.parseDouble(_deltaTimeField.getText());
            if (dt <= 0) {
                ViewUtils.showErrorMsg(this, "Delta-time must be greater than 0");
            } else {
                // Step 3: run simulation
                run_sim(_steps, dt);
            }
        } catch (Exception e) {
            ViewUtils.showErrorMsg(this, "Invalid input for steps or delta-time");
            _openButton.setEnabled(true);
            _viewerButton.setEnabled(true);
            _regionsButton.setEnabled(true);
            _runButton.setEnabled(true);
            _stopButton.setEnabled(false);
            _stopped = true;
        }
    }

    private void stop_sim() {   // Stop the simulation
        _stopped = true;
    }

    private void run_sim(int n, double dt) {    // Run the simulation for n steps
        if (n > 0 && !_stopped) {
            try {
                _ctrl.advance(dt);
                SwingUtilities.invokeLater(() -> run_sim(n - 1, dt));   // Run the simulation for n-1 steps recursively
            } catch (Exception e) {
                ViewUtils.showErrorMsg(this, e.getMessage());
                _openButton.setEnabled(true);
                _viewerButton.setEnabled(true);
                _regionsButton.setEnabled(true);
                _runButton.setEnabled(true);
                _stopButton.setEnabled(false);
                _stopped = true;
            }
        } else {
            _openButton.setEnabled(true);
            _viewerButton.setEnabled(true);
            _regionsButton.setEnabled(true);
            _runButton.setEnabled(true);
            _stopButton.setEnabled(false);
            _stopped = true;
        }
    }
}
