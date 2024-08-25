package simulator.control;

import java.io.*;

import org.json.*;

import java.util.ArrayList;
import java.util.List;

import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {
    private final Simulator _sim;

    public Controller(Simulator sim) {
        this._sim = sim;
    }

    public void load_data(JSONObject data) {
        // Sets the regions to DefaultRegion
        _sim.default_matrix();

        // Load regions (optional field) from the JSON file
        set_regions(data);

        // Load animals (required field) from the JSON file
        JSONArray animals = data.getJSONArray("animals");

        if (animals != null && !animals.isEmpty()) {
            for (int i = 0; i < animals.length(); i++) {
                // add animals to the simulator
                JSONObject animal = animals.getJSONObject(i);
                int amount = animal.getInt("amount");            // amount of each specific animal
                JSONObject spec = animal.getJSONObject("spec");    // JSON with the animal specification

                for (int j = 0; j < amount; j++) {
                    _sim.add_animal(spec);
                }
            }
        } else
            throw new IllegalArgumentException("No animals found in the JSON file");

    }

    public void run(double t, double dt, boolean sv, OutputStream out) {
        PrintStream ps = new PrintStream(out);

        SimpleObjectViewer view = null;
        if (sv) {
            MapInfo m = _sim.get_map_info();
            view = new SimpleObjectViewer("[ECOSYSTEM]",
                    m.get_width(), m.get_height(),
                    m.get_cols(), m.get_rows());
            view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
        }

        JSONObject jo = new JSONObject();
        jo.put("in", _sim.as_JSON());        // save the initial state of the simulator

        while (_sim.get_time() <= t) {        // main loop
            _sim.advance(dt);
            if (sv)
                view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
        }

        if (sv)
            view.close();

        jo.put("out", _sim.as_JSON());        // save the final state of the simulator

        ps.println(jo);
    }

    private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
        // list with all the animals info for the Object Viewer
        List<ObjInfo> ol = new ArrayList<>(animals.size());
        for (AnimalInfo a : animals) {
            ol.add(new ObjInfo(a.get_genetic_code(),
                    (int) a.get_position().getX(),
                    (int) a.get_position().getY(), (int) Math.round(a.get_age()) + 2));
        }
        return ol;
    }

    public void reset(int cols, int rows, int width, int height) {
        _sim.reset(cols, rows, width, height);
    }

    public void set_regions(JSONObject rs) {
        // Load regions (optional field) from the JSON file
        JSONArray reg = rs.optJSONArray("regions");

        if (reg != null && !reg.isEmpty()) {
            for (int j = 0; j < reg.length(); j++) {
                JSONObject region = reg.getJSONObject(j);

                JSONArray row = region.getJSONArray("row");
                JSONArray col = region.getJSONArray("col");
                JSONObject spec = region.getJSONObject("spec");    // JSON with the region specification

                int rowFrom = row.getInt(0);
                int rowTo = row.getInt(1);
                int colFrom = col.getInt(0);
                int colTo = col.getInt(1);

                for (int r = rowFrom; r <= rowTo; r++) {
                    for (int c = colFrom; c <= colTo; c++) {
                        _sim.set_region(r, c, spec);
                    }
                }
            }
        }
    }

    public void advance(double dt) {
        _sim.advance(dt);
    }

    // OBSERVER METHODS
    public void addObserver(EcoSysObserver o) {
        _sim.addObserver(o);
    }

    public void removeObserver(EcoSysObserver o) {
        _sim.removeObserver(o);
    }

}
