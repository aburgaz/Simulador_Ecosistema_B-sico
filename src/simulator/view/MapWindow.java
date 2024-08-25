package simulator.view;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class MapWindow extends JFrame implements EcoSysObserver {
    private final Controller _ctrl;
    private AbstractMapViewer _viewer;
    private final Frame _parent;

    public MapWindow(Frame parent, Controller ctrl) {
        super("[MAP VIEWER]");
        _ctrl = ctrl;
        _parent = parent;
        intiGUI();
        _ctrl.addObserver(this);
    }

    private void intiGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        _viewer = new MapViewer();
        mainPanel.add(_viewer, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                _ctrl.removeObserver(MapWindow.this);
            }
        });

        pack();
        if (_parent != null)
            setLocation(
                    _parent.getLocation().x + _parent.getWidth() / 2 - getWidth() / 2,
                    _parent.getLocation().y + _parent.getHeight() / 2 - getHeight() / 2);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        SwingUtilities.invokeLater(() -> {
            _viewer.reset(time, map, animals);
            pack();
        });
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        SwingUtilities.invokeLater(() -> {
            _viewer.reset(time, map, animals);
            pack();
        });
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        SwingUtilities.invokeLater(() -> {
            _viewer.update(animals, time);
        });
    }
}