package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectClosest;
import simulator.model.SelectionStrategy;

public class SelectClosestBuilder extends Builder<SelectionStrategy> {

    public SelectClosestBuilder() {
        super("closest", "Select closest animal to the current one");
    }

    @Override
    protected SelectClosest create_instance(JSONObject data) {
        if (!data.isEmpty()) throw new IllegalArgumentException("The JSON object is not empty");

        return new SelectClosest();
    }
}
