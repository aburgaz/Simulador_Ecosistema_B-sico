package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal> {
	Factory<SelectionStrategy> _strategy_factory;

	public WolfBuilder(Factory<SelectionStrategy> selection_factory) {
		super("wolf", "A wolf");
		_strategy_factory = selection_factory;
	}

	@Override
	protected Wolf create_instance(JSONObject data) {
		// Example JSON:
		// {
		// "type": "wolf"
		// "data": {
		// "mate_strategy" : { … }
		// "hunt_strategy" : { … }
		// "pos" : {
		// "x_range" : [ 100.0, 200.0 ],
		// "y_range" : [ 100.0, 200.0 ]
		// }
		// }
		// }

		SelectionStrategy _mate_strategy;
		SelectionStrategy _hunt_strategy;
		Vector2D _pos = null;

		if (data.has("pos")) {
			JSONObject p = data.getJSONObject("pos");
			// get x range
			double x1 = p.getJSONArray("x_range").getDouble(0);
			double x2 = p.getJSONArray("x_range").getDouble(1);
			// get y range
			double y1 = p.getJSONArray("y_range").getDouble(0);
			double y2 = p.getJSONArray("y_range").getDouble(1);

			// get random pos inbetween x and y range
			_pos = new Vector2D(x1 + Utils._rand.nextDouble() * (x2 - x1), y1 + Utils._rand.nextDouble() * (y2 - y1));
		}

		_mate_strategy = data.isNull("mate_strategy") ? new SelectFirst()
				: _strategy_factory.create_instance(data.getJSONObject("mate_strategy"));

		_hunt_strategy = data.isNull("hunt_strategy") ? new SelectFirst()
				: _strategy_factory.create_instance(data.getJSONObject("hunt_strategy"));

		return new Wolf(_mate_strategy, _hunt_strategy, _pos);
	}

	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("pos", "Range for both x axys and y axys in thwo different arrays (x_range/y_range), each with a lower and upper boundary");
		o.put("mate_strategy", "Strategy to select a mate animal (first, youngest, closest)");
		o.put("danger_strategy", "Strategy to select a prey (first, youngest, closest)");
	}
}
