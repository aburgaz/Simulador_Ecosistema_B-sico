package simulator.view;

import simulator.control.Controller;
import simulator.model.*;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {
    private SortedMap<String, SortedMap<State, Integer>> _species;  // Map of species and their states (Map inside a Map)
    // Its sorted by the key (species) so the table doesn't change order when updated
    private Controller _ctrl;

    public SpeciesTableModel(Controller ctrl) {
        _ctrl = ctrl;
        _species = new TreeMap<>();
        _ctrl.addObserver(this);
    }

    @Override
    public int getColumnCount() {
        return State.values().length + 1;   // +1 for the species column
    }

    @Override
    public int getRowCount() {
        return _species.size();     // One row for each species
    }

    @Override
    public String getColumnName(int col) {
        if (col == 0) {
            return "Species";
        } else {
            return State.values()[col - 1].toString();  // Returns the name of the states
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (_species.keySet().isEmpty()) {    // If there are no species yet return null
            return null;
        }
        if (col == 0) {
            return _species.keySet().toArray()[row];    // Returns the species
        } else {
            return _species.get(_species.keySet().toArray()[row]).get(State.values()[col - 1]);     // Returns the number of animals in a certain state inside the species Map
        }

    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        fireTableDataChanged();
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        for (String k : _species.keySet()) {    // Reset the number of animals in each state to 0
            for (State s : State.values())
                _species.get(k).put(s, 0);
        }
        fireTableDataChanged();
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        if (!_species.containsKey(a.get_genetic_code())) {  // If the species is not in the map, add it
            SortedMap data = new TreeMap<State, Integer>();
            for (State s : State.values())
                data.put(s, 0);     // Initialize the number of animals in each state to 0
            _species.put(a.get_genetic_code(), data);   // Add the species to the map
        }
        SortedMap<State, Integer> data = _species.get(a.get_genetic_code());    // Get the species from the map
        data.put(a.get_state(), data.get(a.get_state()) + 1);   // Increase the number of animals in the specific state
        fireTableDataChanged();     // Update the table
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        for (var k : _species.keySet()) {   // For each species
            for (State s : State.values())
                if (s != State.DEAD)    // If the state is not DEAD
                    _species.get(k).put(s, 0);  // Reset the number of animals in each state to 0
        }
        for (AnimalInfo a : animals) {  // For each animal
            onAnimalAdded(time, map, animals, a);   // Update the number of animals in each state
        }
        fireTableDataChanged();     // Update the table
    }
}