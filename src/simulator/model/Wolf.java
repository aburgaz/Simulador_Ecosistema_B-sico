package simulator.model;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

import java.security.PrivateKey;

public class Wolf extends Animal {
    Animal _hunt_target = null;
    SelectionStrategy _hunting_strategy;

    // CONSTANTS FOR WOLF
    private final double max_age = 14.0;                // Maximum age of the wolf
    private final double HUNGER_ENERGY = 50.0;        // Energy level at which the wolf starts to hunt
    private final double energy_multiplier = 18.0;            // Multiplier for energy
    private final double desire_multiplier = 30.0;            // Multiplier for desire

    public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
        super("Wolf", Diet.CARNIVORE, 50.0, 60.0, mate_strategy, pos);
        _hunting_strategy = hunting_strategy;
    }

    protected Wolf(Wolf p1, Animal p2) {
        super(p1, p2);
        _hunting_strategy = p1._hunting_strategy;
        _hunt_target = null;
    }

    // STATE MANAGEMENT
    private void switch_to_normal() {
        _state = State.NORMAL;
        _hunt_target = null;
        _mate_target = null;
    }

    private void switch_to_mate() {
        _state = State.MATE;
        _hunt_target = null;
    }

    private void switch_to_hunger() {
        _state = State.HUNGER;
        _mate_target = null;
    }

    private void update_according_to_state(double dt) {
        switch (_state) {
            case NORMAL -> {
                update_normal(dt);
            }
            case HUNGER -> {
                update_hunger(dt);
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
        if (_energy < HUNGER_ENERGY) {
            switch_to_hunger();
        } else {
            if (_desire > MATE_DESIRE) {
                switch_to_mate();
            }
        }
    }

    private void update_hunger(double dt) {
        // Step 1: check hunt target
        if (_hunt_target == null || (_hunt_target.get_state() == State.DEAD || _hunt_target.get_position().distanceTo(get_position()) > _sight_range)) {
            _hunt_target = search_new_hunt_target();
        }

        // Step 2: move and hunt
        if (_hunt_target == null) {
            move_randomly(dt, _speed, energy_multiplier, desire_multiplier);
        } else {
            _dest = _hunt_target.get_position();

            move_and_clamp(dt, 3.0 * _speed, energy_multiplier * 1.2, desire_multiplier);

            if (_pos.distanceTo(_hunt_target.get_position()) < INTERACTION_DISTANCE) {
                _hunt_target._state = State.DEAD;
                _hunt_target = null;
                _energy += HUNGER_ENERGY;
                if (_energy > MAX_ENERGY) {
                    _energy = MAX_ENERGY;
                }
            }
        }

        // Step 3: change state
        if (_energy > HUNGER_ENERGY) {
            if (_desire > MATE_DESIRE) {
                switch_to_mate();
            } else {
                switch_to_normal();
            }
        }
    }

    private void update_mate(double dt) {
        // Step 1: check mate target
        if (_mate_target != null && (_mate_target.get_state() == State.DEAD || _pos.distanceTo(_mate_target._pos) > _sight_range)) {
            _mate_target = null;
        }

        // Step 2: animal move
        if (_mate_target == null) {
            _mate_target = search_new_mate_target();
        }

        if (_mate_target == null) {
            move_randomly(dt, _speed, energy_multiplier, desire_multiplier);
        } else {
            _dest = _mate_target.get_position();

            move_and_clamp(dt, 3.0 * _speed, energy_multiplier * 1.2, desire_multiplier);

            if (_pos.distanceTo(_mate_target.get_position()) < INTERACTION_DISTANCE) {
                _mate_target._desire = 0.0;
                _desire = 0.0;

                if (_baby == null && Utils._rand.nextDouble() < BIRTH_PROBABILITY) {
                    _mate_target._baby = new Wolf(this, _mate_target);
                }

                _energy -= 10.0;
                if (_energy < 0.0) {
                    _energy = 0.0;
                }
                _mate_target = null;
            }
        }

        // Step 3: change state
        if (_energy < HUNGER_ENERGY) {
            switch_to_hunger();
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

        if (!pos_in_bounds()) {            // Check if the animal is out of bounds and clamp it
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

    private Animal search_new_hunt_target() {
        return _hunting_strategy.select(this, _region_mngr.get_animals_in_range(this, a -> a.get_diet() == Diet.HERBIVORE));
    }
}
