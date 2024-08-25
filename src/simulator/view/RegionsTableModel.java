package simulator.view;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.*;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {
    private List<MapInfo.RegionData> _regions;  // List of regions
    private Map<RegionInfo, Map<Diet, Integer>> _regionDiet;  // Map of regions and their diets (Map inside a Map)
    private String[] _col_names = {"Row", "Col", "Desc."};
    private Controller _ctrl;

    public RegionsTableModel(Controller ctrl) {
        _ctrl = ctrl;
        _regions = new ArrayList<>();
        _regionDiet = new HashMap<>();
        _ctrl.addObserver(this);
    }

    @Override
    public String getColumnName(int col) {      // Returns the name of the column
        if (col < _col_names.length) {
            return _col_names[col];
        } else {
            return Diet.values()[col - _col_names.length].toString();
        }
    }

    @Override
    public int getColumnCount() {       // Returns the number of columns
        return Diet.values().length + _col_names.length;
    }

    @Override
    public int getRowCount() {
        return _regions.size();
    }

    @Override
    public Object getValueAt(int arg0, int arg1) {
        if (_regions.isEmpty()) {     // If there are no regions yet return null
            return null;
        }
        if (arg1 == 0) {
            return _regions.get(arg0).row();    // Returns the row of the region
        } else if (arg1 == 1) {
            return _regions.get(arg0).col();    // Returns the column of the region
        } else if (arg1 == 2) {
            return _regions.get(arg0).r();      // Returns the region as a string
        } else {
            return _regionDiet.get(_regions.get(arg0).r()).get(Diet.values()[arg1 - 3]);      // Returns the number of animals of a certain diet in the region
        }
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        fireTableDataChanged();    // Update the table
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        _regions.clear();
        _regionDiet.clear();
        fireTableDataChanged();
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        int index = 0;      // Index of the region
        for (MapInfo.RegionData region : _regions) {
            if (region.row() == row && region.col() == col) {
                index = _regions.indexOf(region);   // Get the index of the current region in the list
                _regions.remove(region);    // Remove the region
                break;
            }
        }
        _regions.add(index, new MapInfo.RegionData(row, col, r));    // Add the new region to the list in the same index as before

        update_diets(r);    // Update the diets of the region (Map of diets and number of animals of each diet)
        fireTableDataChanged();     // Update the table
    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        _regions.clear();   // Clear the list of regions
        _regionDiet.clear();    // Clear the map of regions and their diets
        Iterator<MapInfo.RegionData> it = map.iterator();   // Iterator for the regions
        while (it.hasNext()) {
            MapInfo.RegionData region = it.next();
            _regions.add(region);   // Add the region to the list
            update_diets(region.r());   // Update the diets of the region (Map of diets and number of animals of each diet)
        }
        fireTableDataChanged();     // Update the table
    }

    private void update_diets(RegionInfo r) {   // Update the number of animals for each diet inside the specified region
        Map<Diet, Integer> aux = new HashMap<>();
        for (Diet d : Diet.values()) {
            aux.put(d, 0);       // Initialize the number of animals of each diet to 0
        }
        for (AnimalInfo a : r.getAnimalsInfo()) {   // For each animal in the region
            aux.put(a.get_diet(), aux.get(a.get_diet()) + 1);    // Update the number of animals of each diet
        }
        _regionDiet.put(r, aux);     // Update the region and its diets
    }
}
