package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {
    private double _food;
    private double _factor;

    public DynamicSupplyRegion(double food, double factor) {
        _food = food;
        _factor = factor;
    }

    @Override
    public void update(double dt) {
        if (Utils._rand.nextBoolean()) {
            _food += dt * _factor;
        }
    }

    @Override
    public double get_food(Animal a, double dt) {
        if (a.get_diet() == Diet.CARNIVORE) {
            return 0.0;
        }

        double ret = Math.min(_food, 60.0 * Math.exp(-Math.max(0, get_num_herbivores() - 5.0) * 2.0) * dt);

        _food -= ret;

        return ret;
    }
    
    public String toString() {
    	return "Dynamic region";
    }
}
