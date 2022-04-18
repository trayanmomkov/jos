package info.trekto.jos.core.impl;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.Controller.simulationLogic;
import static info.trekto.jos.core.impl.SimulationRecursiveAction.THRESHOLD;

public class CollisionCheck extends RecursiveAction {
    private final int fromIndex;
    private final int toIndex;

    public static ConcurrentMap<SimulationObject, Boolean> collisions;

    public CollisionCheck(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    public static void prepare() {
        collisions = new ConcurrentHashMap<>();
    }

    public static boolean collisionExists() {
        for (Boolean collision : collisions.values()) {
            if (collision) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void compute() {
        if (toIndex - fromIndex <= THRESHOLD) {
            outerloop:
            for (SimulationObject object : C.getSimulation().getAuxiliaryObjects().subList(fromIndex, toIndex)) {
                for (SimulationObject object1 : C.getSimulation().getAuxiliaryObjects()) {
                    if (object == object1) {
                        continue;
                    }
                    // distance between centres
                    Number distance = simulationLogic.calculateDistance(object, object1);

                    if (distance.compareTo(object.getRadius().add(object1.getRadius())) < 0) {
                        collisions.put(object, true);
                        break outerloop;
                    }
                }
            }
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            int middle = fromIndex + ((toIndex - fromIndex) / 2);
            subtasks.add(new CollisionCheck(fromIndex, middle));
            subtasks.add(new CollisionCheck(middle, toIndex));
            ForkJoinTask.invokeAll(subtasks);
        }
    }
}
