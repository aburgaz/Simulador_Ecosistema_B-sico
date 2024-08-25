package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {
    public SelectFirstBuilder() {
        super("first", "Select first animal on the on the list");
    }

    @Override
    protected SelectFirst create_instance(JSONObject data) {
        if (!data.isEmpty()) throw new IllegalArgumentException("The JSON object is not empty");

        return new SelectFirst();
    }
}
