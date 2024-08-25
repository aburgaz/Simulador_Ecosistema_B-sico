package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {
    protected List<Animal> _animals;

    public Region() {
        _animals = new ArrayList<>();
    }

    final void add_animal(Animal a) {
        _animals.add(a);
    }

    final void remove_animal(Animal a) {
        _animals.remove(a);
    }

    final List<Animal> getAnimals() {
        return Collections.unmodifiableList(_animals);
    }

    public JSONObject as_JSON() {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();

        for (Animal a : _animals) {
            ja.put(a.as_JSON());
        }

        jo.put("animals", ja);

        return jo;
    }

    protected int get_num_herbivores() {
        int num_herbivores = 0;
        for (Animal a : _animals) {
            if (a.get_diet() == Diet.HERBIVORE) {
                num_herbivores++;
            }
        }
        return num_herbivores;
    }

    public List<AnimalInfo> getAnimalsInfo() {
        return new ArrayList<>(_animals); // se puede usar Collections.unmodifiableList(_animals);
    }
}
