package simulator.factories;

import java.util.*;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
    private Map<String, Builder<T>> _builders;
    private List<JSONObject> _builders_info;

    public BuilderBasedFactory() {
        _builders = new HashMap<>();
        _builders_info = new ArrayList<>();
    }

    public BuilderBasedFactory(List<Builder<T>> builders) {
        this();
        for (Builder<T> b : builders) {
            add_builder(b);
        }
    }

    private void add_builder(Builder<T> b) {
        _builders.put(b.get_type_tag(), b);
        _builders_info.add(b.get_info());
    }

    @Override
    public T create_instance(JSONObject info) throws IllegalArgumentException{
        if (info == null) {
            throw new IllegalArgumentException("'info' cannot be null");
        }

        String tag = info.getString("type");
        Builder<T> b = _builders.get(tag);        // get the builder associated with the tag

        if (b == null) {
            throw new IllegalArgumentException("Invalid type: " + tag);
        }

        // each builder creates an instance of its own type
        T obj = b.create_instance(info.has("data") ? info.getJSONObject("data") : new JSONObject());

        return obj;
    }

    @Override
    public List<JSONObject> get_info() {
        return Collections.unmodifiableList(_builders_info);
    }
}
