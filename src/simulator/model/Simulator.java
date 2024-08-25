package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {
    private final Factory<Animal> _animal_factory;
    private final Factory<Region> _region_factory;
    private RegionManager _manager;
    private List<Animal> _animals = new ArrayList<>();
    private List<EcoSysObserver> _observers;
    private double _time;

    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory, Factory<Region> regions_factory) {
        _animal_factory = animals_factory;
        _region_factory = regions_factory;
        _manager = new RegionManager(cols, rows, width, height);
        _observers = new ArrayList<EcoSysObserver>();
        _time = 0.0;
    }

    public void default_matrix() {       // set all regions to DefaultRegion, notifies observers to update the tables from the start
        for (int i = 0; i < _manager.get_rows(); i++) {
            for (int j = 0; j < _manager.get_cols(); j++) {
                set_region(i, j, new DefaultRegion());
            }
        }
    }

    private void set_region(int row, int col, Region r) {
        _manager.set_region(row, col, r);
        notifyRegionSet(row, col, r);       // notify observers
    }

    public void set_region(int row, int col, JSONObject r_json) {
        // create the regions specified in the JSON file
        set_region(row, col, _region_factory.create_instance(r_json));
    }

    private void add_animal(Animal a) {
        _animals.add(a);
        _manager.register_animal(a);    // region manager tracks all animals
        notifyAnimalAdded(a);       // notify observers
    }

    public void add_animal(JSONObject a_json) {
        // create the animals specifies in the JSON file
        add_animal(_animal_factory.create_instance(a_json));
    }

    public MapInfo get_map_info() {
        return _manager;
    }

    public List<? extends AnimalInfo> get_animals() {
        return Collections.unmodifiableList(_animals);
    }

    public double get_time() {
        return _time;
    }

    public void advance(double dt) {
        _time += dt;

        // to not modify the list while iterating over it
        List<Animal> dead_animals = new ArrayList<>();        // save dead animals to remove them after the iteration
        List<Animal> pregnant_animals = new ArrayList<>();        // save pregnant animals to add their babies after the iteration
        for (Animal a : _animals) {
            if (a.get_state() == State.DEAD) dead_animals.add(a);
            else if (a.is_pregnant()) pregnant_animals.add(a);
        }

        for (Animal a : dead_animals) _manager.unregister_animal(a);        // remove dead animals
        _animals.removeAll(dead_animals);

        for (Animal a : _animals) {            // update all animals that are still alive, babies are not currently in the list
            a.update(dt);
            _manager.update_animal_region(a);
        }

        for (Animal a : pregnant_animals) add_animal(a.deliver_baby());    // add babies to the simulation

        _manager.update_all_regions(dt);
        notifyAdvanced(dt);     // notify observers
    }

    public void reset(int cols, int rows, int width, int height) {     // reset the simulation
        _animals = new ArrayList<>();
        _manager = new RegionManager(cols, rows, width, height);
        _time = 0.0;
        notifyReset();      // notify observers
    }

    public JSONObject as_JSON() {
        JSONObject jo = new JSONObject();
        jo.put("time", _time);
        jo.put("state", _manager.as_JSON());
        return jo;
    }

    //OBSEVER METHODS
    @Override
    public void addObserver(EcoSysObserver o) {     // add observer to the list
        if (!_observers.contains(o)) {
            _observers.add(o);
            o.onRegister(_time, _manager, Collections.unmodifiableList(_animals));     // notify observer
        }
    }

    @Override
    public void removeObserver(EcoSysObserver o) {      // remove observer from the list
        _observers.remove(o);
    }

    private void notifyAnimalAdded(Animal a) {      // notify all observers that an animal was added
        for (EcoSysObserver o : _observers) {
            o.onAnimalAdded(_time, _manager, Collections.unmodifiableList(_animals), a);
        }
    }

    private void notifyRegionSet(int row, int col, RegionInfo r) {      // notify all observers that a region was set
        for (EcoSysObserver o : _observers) {
            o.onRegionSet(row, col, _manager, r);
        }
    }

    private void notifyReset() {        // notify all observers that the simulation was reset
        for (EcoSysObserver o : _observers) {
            o.onReset(_time, _manager, Collections.unmodifiableList(_animals));
        }
    }

    private void notifyAdvanced(double dt) {        // notify all observers that the simulation advanced
        for (EcoSysObserver o : _observers) {
            o.onAvanced(_time, _manager, Collections.unmodifiableList(_animals), dt);
        }
    }
}
