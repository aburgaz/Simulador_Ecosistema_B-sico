package simulator.view;

import java.text.DecimalFormat;
import java.util.List;
import java.awt.*;
import javax.swing.*;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class StatusBar extends JPanel implements EcoSysObserver {

    private JLabel _timeLabel;
    private JLabel _animalsLabel;
    private JLabel _dimLabel;
    private double _time;
    private int _numAnimals;
    private int _rows;
    private int _cols;
    private double _height;
    private double _width;
    private Controller _ctrl;
    public StatusBar(Controller ctrl) {
        _ctrl = ctrl;
        initGUI();
        ctrl.addObserver(this);
    }

    private void initGUI() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setBorder(BorderFactory.createBevelBorder(1));

        JSeparator s1 = new JSeparator(JSeparator.VERTICAL);
        s1.setPreferredSize(new Dimension(10, 20));
        JSeparator s2 = new JSeparator(JSeparator.VERTICAL);
        s2.setPreferredSize(new Dimension(10, 20));

        _timeLabel = new JLabel("Time: ");
        this.add(_timeLabel);
        this.add(s1);    // Adds a separator
        _animalsLabel = new JLabel("Total animals: ");
        this.add(_animalsLabel);
        this.add(s2);   // Adds a separator
        _dimLabel = new JLabel("Dimension: ");
        this.add(_dimLabel);
    }

    private void updateLabels() {
        DecimalFormat df = new DecimalFormat("#0.000");
        _timeLabel.setText(String.format("Time: " + df.format(_time)));     // Formats the time to 3 decimal places
        _animalsLabel.setText("Total animals: " + _numAnimals);     // Displays the total number of animals
        _dimLabel.setText("Dimension: " + (int)_width + " x " + (int)_height + " : " + _cols + " x " + _rows);      // Displays the dimensions of the map
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        // Updates the time, number of animals, rows, columns, height and width
        _time = time;
        _numAnimals = animals.size();
        _rows = map.get_rows();
        _cols = map.get_cols();
        _height = map.get_height();
        _width = map.get_width();
        updateLabels();
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        // Updates the time, number of animals, rows, columns, height and width
        _time = time;
        _numAnimals = animals.size();
        _rows = map.get_rows();
        _cols = map.get_cols();
        _height = map.get_height();
        _width = map.get_width();
        updateLabels();
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        // Updates the time and number of animals
        _time = time;
        _numAnimals++;
        updateLabels();
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        // Updates the rows, columns, height and width
        _rows = map.get_rows();
        _cols = map.get_cols();
        _height = map.get_height();
        _width = map.get_width();
        updateLabels();
    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        // Updates the time and number of animals
        _time = time;
        _numAnimals = animals.size();
        updateLabels();
    }
}
