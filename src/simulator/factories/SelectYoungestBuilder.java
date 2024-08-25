package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectYoungest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {
	public SelectYoungestBuilder() {
		super("youngest", "Select youngest animal on the list");
	}

	@Override
	protected SelectYoungest create_instance(JSONObject data) {
		if (!data.isEmpty())
			throw new IllegalArgumentException("The JSON object is not empty");

		return new SelectYoungest();
	}
}
