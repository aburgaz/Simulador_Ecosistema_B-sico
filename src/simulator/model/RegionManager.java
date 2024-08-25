package simulator.model;

import java.util.*;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView {
    private final int _width;
    private final int _height;
    private final int _cols;
    private final int _rows;
    private final int _region_width;
    private final int _region_height;

    private Region[][] _regions;
    private Map<Animal, Region> _animal_region = new HashMap<>();

    public RegionManager(int cols, int rows, int width, int height) {
        _cols = cols;
        _rows = rows;
        _width = width;
        _height = height;

        if (cols <= 0 || rows <= 0 || width <= 0 || height <= 0)
            throw new IllegalArgumentException("Invalid parameters for the map dymensions");

        // if width/height not divisible by cols/rows, add 1 extra region
        _region_width = _width / cols + (width % cols != 0 ? 1 : 0);
        _region_height = _height / rows + (height % rows != 0 ? 1 : 0);
        _regions = new Region[rows][cols];

        for (int i = 0; i < rows; i++) {    // initialize regions
            for (int j = 0; j < cols; j++) {
                _regions[i][j] = new DefaultRegion();
            }
        }
    }

    @Override
    public int get_cols() {
        return _cols;
    }

    @Override
    public int get_rows() {
        return _rows;
    }

    @Override
    public int get_width() {
        return _width;
    }

    @Override
    public int get_height() {
        return _height;
    }

    @Override
    public int get_region_width() {
        return _region_width;
    }

    @Override
    public int get_region_height() {
        return _region_height;
    }

    @Override
    public double get_food(Animal a, double dt) {
        return _animal_region.get(a).get_food(a, dt);
    }

    @Override
    public List<Animal> get_animals_in_range(Animal a, Predicate<Animal> filter) {
        List<Animal> in_range = new ArrayList<>();
        for (Animal animal : _animal_region.keySet()) {
            if (animal != a && filter.test(animal)
                    && animal.get_position().distanceTo(a.get_position()) < a.get_sight_range()) {
                in_range.add(animal);
            }
        }
        return in_range;
    }

    protected void set_region(int row, int col, Region r) {
        // r is not null, no factory returns null regions
        List<Animal> l = _regions[row][col].getAnimals();
        for (Animal a : l) {
            r.add_animal(a);
            _animal_region.put(a, r); // update the animal region map
        }
        _regions[row][col] = r;
    }

    protected void register_animal(Animal a) {
        a.init(this);
        Region r = get_animal_region(a); // get the region where the animal is
        r.add_animal(a);
        _animal_region.put(a, r); // update the animal region map
    }

    protected void unregister_animal(Animal a) {

        _animal_region.get(a).remove_animal(a);
        _animal_region.remove(a);
    }

    protected void update_animal_region(Animal a) { // manages animal region changes
        Region cur = get_animal_region(a);
        Region last = _animal_region.get(a);

        if (last != cur) {
            last.remove_animal(a);
            cur.add_animal(a);
            _animal_region.put(a, cur);
        }
    }

    private Region get_animal_region(Animal a) { // for the moment this function stays private as only is used
        // internally
        // maybe in the future visibility will change
        int col = ((int) a.get_position().getX()) / _region_width;
        int row = ((int) a.get_position().getY()) / _region_height;

        return _regions[row][col];
    }

    void update_all_regions(double dt) {
        for (Region[] region : _regions) {
            for (Region r : region) {
                r.update(dt);
            }
        }
    }

    public JSONObject as_JSON() {
        JSONArray ja = new JSONArray();
        for (int i = 0; i < _rows; i++) {
            for (int j = 0; j < _cols; j++) {
                JSONObject jo1 = new JSONObject();
                jo1.put("row", _region_width * i);
                jo1.put("col", _region_height * j);
                jo1.put("data", _regions[i][j].as_JSON());
                ja.put(jo1);
            }
        }

        JSONObject jo2 = new JSONObject();
        jo2.put("regiones", ja);

        return jo2;
    }

    @Override
    public Iterator<MapInfo.RegionData> iterator() {        // iterator for the regions
        return new Iterator<MapInfo.RegionData>() {
            private int rowNext = 0;
            private int colNext = 0;

            @Override
            public boolean hasNext() {
                return rowNext < _rows;
            }   // if rowNext == _rows, there are no more regions

            @Override
            public RegionData next() {
                RegionData rd = new RegionData(rowNext, colNext, _regions[rowNext][colNext]);
                colNext++;
                if (colNext == _cols) {
                    colNext = 0;
                    rowNext++;
                }
                return rd;
            }
        };
    }

}
