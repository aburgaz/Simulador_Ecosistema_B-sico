package simulator.factories;

import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {
    public DefaultRegionBuilder() {
        super("default", "Default region");
    }

    @Override
    protected DefaultRegion create_instance(JSONObject data) {
        // Example JSON:
        // {
        // "type" : "default",
        // "data" : { }
        // }

        if (!data.isEmpty()) throw new IllegalArgumentException("The data field for default region is not empty");

        return new DefaultRegion();
    }
}
