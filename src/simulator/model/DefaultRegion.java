package simulator.model;

public class DefaultRegion extends Region {
    @Override
    public void update(double dt) {
        // Do nothing
    }

    @Override
    public double get_food(Animal a, double dt) {
        if (a.get_diet() == Diet.CARNIVORE) {
            return 0.0;
        }

        return 60.0 * Math.exp(-Math.max(0, get_num_herbivores() - 5.0) * 2.0) * dt;
    }
    
    public String toString() {
    	return "Default region";
    }
}
