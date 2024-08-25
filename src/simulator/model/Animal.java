package simulator.model;

import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

import java.util.Objects;

public abstract class Animal implements Entity, AnimalInfo {
    protected String _genetic_code;
    protected Diet _diet;
    protected State _state = State.NORMAL;
    protected Vector2D _pos;
    protected Vector2D _dest = null;
    protected Double _energy = 100.0;
    protected Double _speed;
    protected Double _age = 0.0;
    protected Double _desire = 0.0;
    protected Double _sight_range;
    protected Animal _mate_target = null;
    protected Animal _baby = null;
    protected AnimalMapView _region_mngr = null;
    protected SelectionStrategy _mate_strategy;

    //CONSTANTS FOR THE ANIMALS
    protected static final double MAX_ENERGY = 100.0;       // Maximum energy of the animal
    protected static final double MAX_DESIRE = 100.0;       // Maximum desire of the animal
    protected static final double MATE_DESIRE = 65.0;       // Desire level at which the animal starts to mate
    protected static final double INTERACTION_DISTANCE = 8.0;   // Distance at which the animal interacts with other animals
    protected static final double BIRTH_PROBABILITY = 0.9;  // Probability of giving birth

    protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed, SelectionStrategy mate_strategy, Vector2D pos) throws IllegalArgumentException, NullPointerException {

        if (genetic_code.isEmpty() || sight_range <= 0 || init_speed <= 0) {
            throw new IllegalArgumentException("Invalid arguments");
        }
        if (mate_strategy == null) {
            throw new NullPointerException("Mate strategy can't be null");
        }
        _genetic_code = genetic_code;
        _diet = diet;
        _sight_range = sight_range;
        _pos = pos;
        _mate_strategy = mate_strategy;
        _speed = Utils.get_randomized_parameter(init_speed, 0.1);
    }

    protected Animal(Animal p1, Animal p2) {
        _genetic_code = p1.get_genetic_code();
        _diet = p1.get_diet();

        _mate_strategy = p2._mate_strategy;

        _energy = (p1.get_energy() + p2.get_energy()) / 2;

        _pos = p1.get_position().plus(Vector2D.get_random_vector(-1, 1).scale(60.0 * (Utils._rand.nextGaussian() + 1)));

        _sight_range = Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2, 0.2);
        _speed = Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2, 0.2);

    }

    protected void init(AnimalMapView reg_mngr) {
        // Every time an animal is added to the region manager, it should be initialized
        _region_mngr = reg_mngr;

        if (_pos == null) {
            // Create a random position
            _pos = random_vector_in_map();
        }

        _pos = clamp_pos();        // Make sure the position is within the map
        _dest = random_vector_in_map();
    }

    public State get_state() {
        return _state;
    }

    public Vector2D get_position() {
        return _pos;
    }

    public String get_genetic_code() {
        return _genetic_code;
    }

    public Diet get_diet() {
        return _diet;
    }

    public double get_speed() {
        return _speed;
    }

    public double get_sight_range() {
        return _sight_range;
    }

    public double get_energy() {
        return _energy;
    }

    public double get_age() {
        return _age;
    }

    public Vector2D get_destination() {
        return _dest;
    }

    public boolean is_pregnant() {
        return _baby != null;
    }

    protected Vector2D random_vector_in_map() {
        return Vector2D.get_random_vector(_region_mngr.get_width(), _region_mngr.get_height());
    }

    protected Animal deliver_baby() {        // return the baby (for pregnant animals only)
        Animal baby = _baby;
        _baby = null;
        return baby;
    }

    private void move(double speed) {
        _pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
    }

    protected void move_randomly(double dt, double speed_mul, double enery_mul, double desire_mul) {
        if (_pos.distanceTo(_dest) < INTERACTION_DISTANCE) {
            _dest = random_vector_in_map();
        }

        move_and_clamp(dt, speed_mul, enery_mul, desire_mul);
    }

    protected void move_and_clamp(double dt, double speed_mul, double energy_mul, double desire_mul) {
        move(speed_mul * dt * Math.exp((_energy - 100.0) * 0.007));

        _age += dt;

        _energy -= energy_mul * dt;
        if (_energy < 0.0) _energy = 0.0;
        if (_energy >= MAX_ENERGY) _energy = MAX_ENERGY;

        _desire += desire_mul * dt;
        if (_desire < 0.0) _desire = 0.0;
        if (_desire >= MAX_DESIRE) _desire = MAX_DESIRE;
    }

    protected Animal search_new_mate_target() {
        // Search only for animals inside the sight range (_region_mngr.get_animals_in_range)
        return _mate_strategy.select(this, _region_mngr.get_animals_in_range(this, a -> a.get_genetic_code().equals(_genetic_code)));
    }

    protected boolean pos_in_bounds() {
        return _pos.getX() >= 0 && _pos.getX() < _region_mngr.get_width() && _pos.getY() >= 0 && _pos.getY() < _region_mngr.get_height();
    }

    protected Vector2D clamp_pos() {
        // Make sure the position is within the map (0,0) - (width, height)
        double x = _pos.getX();
        double y = _pos.getY();
        double width = _region_mngr.get_width();
        double height = _region_mngr.get_height();

        while (x >= width) x = x - width;
        while (x < 0) x = x + width;
        while (y >= height) y = y - height;
        while (y < 0) y = y + height;

        return new Vector2D(x, y);
    }

    public JSONObject as_JSON() {
        JSONObject js = new JSONObject();

        js.put("pos", _pos.asJSONArray());
        js.put("gcode", _genetic_code);
        js.put("diet", _diet.toString());
        js.put("state", _state.toString());

        return js;
    }
}
