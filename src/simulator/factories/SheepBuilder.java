package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.*;

public class SheepBuilder extends Builder<Animal> {
	Factory<SelectionStrategy> _strategy_factory;

	public SheepBuilder(Factory<SelectionStrategy> selection_factory) {
		super("sheep", "A sheep");
		_strategy_factory = selection_factory;
	}

	@Override
	protected Sheep create_instance(JSONObject data) {
		// Example JSON:
		// {
		// "type": "sheep"
		// "data": {
		// "mate_strategy" : { … }
		// "danger_strategy" : { … }
		// "pos" : {
		// "x_range" : [ 100.0, 200.0 ],
		// "y_range" : [ 100.0, 200.0 ]
		// }
		// }
		// }

		SelectionStrategy _mate_strategy;
		SelectionStrategy _danger_strategy;
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

		_danger_strategy = data.isNull("danger_strategy") ? new SelectFirst()
				: _strategy_factory.create_instance(data.getJSONObject("danger_strategy"));

		return new Sheep(_mate_strategy, _danger_strategy, _pos);
	}

	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("pos", "Range for both x axys and y axys in thwo different arrays (x_range/y_range), each with a lower and upper boundary");
		o.put("mate_strategy", "Strategy to select a mate animal (first, youngest, closest)");
		o.put("danger_strategy", "Strategy to select a danger source (first, youngest, closest)");
	}
}
