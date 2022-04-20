package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static info.trekto.jos.core.impl.SimulationRecursiveAction.THRESHOLD;

public class CollisionCheck extends RecursiveAction {
    private final int fromIndex;
    private final int toIndex;
    private final Simulation simulation;
    private static ConcurrentMap<SimulationObject, Boolean> collisions;

    public CollisionCheck(int fromIndex, int toIndex, Simulation simulation) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.simulation = simulation;
    }

    public void prepare() {
        collisions = new ConcurrentHashMap<>();
    }

    public boolean collisionExists() {
        return collisions.values().stream().anyMatch(Boolean::booleanValue);
    }

    @Override
    protected void compute() {
        if (toIndex - fromIndex <= THRESHOLD) {
            outerloop:
            for (SimulationObject object : simulation.getAuxiliaryObjects().subList(fromIndex, toIndex)) {
                for (SimulationObject object1 : simulation.getAuxiliaryObjects()) {
                    if (object == object1) {
                        continue;
                    }
                    // distance between centres
                    Number distance = simulation.calculateDistance(object, object1);

                    if (distance.compareTo(object.getRadius().add(object1.getRadius())) < 0) {
                        collisions.put(object, true);
                        break outerloop;
                    }
                }
            }
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            int middle = fromIndex + ((toIndex - fromIndex) / 2);
            subtasks.add(new CollisionCheck(fromIndex, middle, simulation));
            subtasks.add(new CollisionCheck(middle, toIndex, simulation));
            ForkJoinTask.invokeAll(subtasks);
        }
    }
}
