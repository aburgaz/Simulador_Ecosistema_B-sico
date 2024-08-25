package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {
    @Override
    public Animal select(Animal a, List<Animal> as) {
        if (as.isEmpty()) {     // no animals in the list
            return null;
        }
        Animal closest = as.get(0);
        for(Animal animal : as) {
            if(a != animal && a.get_position().distanceTo(animal.get_position()) < a.get_position().distanceTo(closest.get_position())) {
                closest = animal;
            }
        }

        return  closest == a ? null : closest;
    }
}
