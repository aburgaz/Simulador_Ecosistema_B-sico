	package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic supply region");
	}

	@Override
	protected DynamicSupplyRegion create_instance(JSONObject data) {
		// Example JSON
		// {
		// "type" : "dynamic",
		// "data" : {
		// "factor" : 2.5,
		// "food" : 1250.0
		// }
		double _factor = 2.0;
		double _food = 1000.0;

		if (!data.isNull("factor") && !data.get("factor").equals(""))
			_factor = data.getDouble("factor");
		if (!data.isNull("food") && !data.get("food").equals(""))
			_food = data.getDouble("food");

		return new DynamicSupplyRegion(_factor, _food);
	}

	@Override
	protected void fill_in_data(JSONObject o) {		// This method is used by the dialog to show the user the fields that can be filled in
		o.put("factor", "Food increase factor (optional with default 2.0)");
		o.put("food", "Initial amount of food (optional with default 1000.0)");
	}
}
