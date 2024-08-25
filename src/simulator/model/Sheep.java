package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal {
    private Animal _danger_source = null;
    private SelectionStrategy _danger_strategy;

    //CONSTANTS FOR SHEEP
    private final double max_age = 8.0;            // Maximum age of the sheep
    private final double energy_multiplier = 20.0;        // Multiplier for energy
    private final double desire_multiplier = 40.0;        // Multiplier for desire

    public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
        super("Sheep", Diet.HERBIVORE, 40.0, 35.0, mate_strategy, pos);
        _danger_strategy = danger_strategy;
    }

    protected Sheep(Sheep p1, Animal p2) {
        super(p1, p2);
        _danger_strategy = p1._danger_strategy;
        _danger_source = null;
    }

    // STATE MANAGEMENT
    private void switch_to_normal() {
        _state = State.NORMAL;
        _danger_source = null;
        _mate_target = null;
    }

    private void switch_to_mate() {
        _state = State.MATE;
        _danger_source = null;
    }

    private void switch_to_danger() {
        _state = State.DANGER;
        _mate_target = null;
    }

    private void update_according_to_state(double dt) {
        switch (_state) {
            case NORMAL -> {
                update_normal(dt);
            }
            case DANGER -> {
                update_danger(dt);
            }
            case MATE -> {
                update_mate(dt);
            }
            default -> {
                return;
            }
        }
    }

    private void update_normal(double dt) {
        // Step 1: animal moves
        move_randomly(dt, _speed, energy_multiplier, desire_multiplier);

        // Step 2: change state
        if (_danger_source == null) {
            if (_desire > MATE_DESIRE) {
                switch_to_mate();
            } else _danger_source = search_new_danger_source();

        } else {
            switch_to_danger();
        }
    }

    private void update_danger(double dt) {
        // Step 1: check if danger source
        if (_danger_source != null && _danger_source.get_state() == State.DEAD) {
            switch_to_normal();
        }

        // Paso 2: avanzar el animal
        if (_danger_source == null) {
            move_randomly(dt, _speed, energy_multiplier, desire_multiplier);
        } else {
            _dest = _pos.plus(_pos.minus(_danger_source.get_position()).direction());

            move_and_clamp(dt, _speed * 2.0, energy_multiplier * 1.2, desire_multiplier);
        }

        // Paso 3: cambio de estado
        if (_danger_source == null || _danger_source.get_position().distanceTo(_pos) > _sight_range) {
            _danger_source = search_new_danger_source();

            if (_danger_source == null) {
                if (_desire < MATE_DESIRE) {
                    switch_to_normal();
                } else {
                    switch_to_mate();
                }
            }
        }
    }

    private void update_mate(double dt) {
        // Step 1: check mate target
        if (_mate_target != null && (_mate_target.get_state() == State.DEAD || _mate_target.get_position().distanceTo(_pos) > _sight_range)) {
            _mate_target = null;
        }

        // Step 2: move the animal
        if (_mate_target == null) {
            _mate_target = search_new_mate_target();
        }

        if (_mate_target == null) {
            move_randomly(dt, _speed, energy_multiplier, desire_multiplier);
        } else {
            _dest = _mate_target.get_position();

            move_and_clamp(dt, 2.0 * _speed, energy_multiplier * 1.2, desire_multiplier);

            if (_pos.distanceTo(_mate_target.get_position()) < INTERACTION_DISTANCE) {
                _desire = 0.0;
                _mate_target._desire = 0.0;

                if (_baby == null && Utils._rand.nextDouble() < BIRTH_PROBABILITY) {
                    _baby = new Sheep(this, _mate_target);
                }

                _mate_target = null;
            }
        }

        // Step 3: search for danger source
        if (_danger_source == null) {
            _danger_source = search_new_danger_source();
        }

        // Step 4: change state
        if (_danger_source != null) {
            switch_to_danger();
        } else {
            if (_desire < MATE_DESIRE) {
                switch_to_normal();
            }
        }
    }


    @Override
    public void update(double dt) {
        if (_state == State.DEAD) {
            return;
        }

        update_according_to_state(dt);

        if (!pos_in_bounds()) {        // Check if the animal is out of bounds and clamp it
            _pos = clamp_pos();
            switch_to_normal();            // If the animal is out of bounds, it will be set to normal state
        }

        if (_energy == 0.0 || _age > max_age) {
            _state = State.DEAD;
        }

        if (_state != State.DEAD) {
            _energy += _region_mngr.get_food(this, dt);
            if (_energy > MAX_ENERGY) {
                _energy = MAX_ENERGY;
            }
            if (_energy < 0.0) {
                _energy = 0.0;
            }
        }
    }

    private Animal search_new_danger_source() {
        return _danger_strategy.select(this, _region_mngr.get_animals_in_range(this, a -> a.get_diet() == Diet.CARNIVORE));
    }
}
