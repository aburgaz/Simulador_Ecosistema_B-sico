package simulator.model;

import java.util.List;

public class SelectFirst implements SelectionStrategy {
    @Override
    public Animal select(Animal a, List<Animal> as) {
        for (Animal animal : as) {
            if (a != animal) {
                return animal;
            }
        }
        return null;
    }
}
