package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.CpuSimulation;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.Number;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static info.trekto.jos.core.impl.arbitrary_precision.SimulationRecursiveAction.threshold;

class CollisionCheckRecursiveAction extends RecursiveAction {
    private final int fromIndex;
    private final int toIndex;
    private final CpuSimulation simulation;

    public CollisionCheckRecursiveAction(int fromIndex, int toIndex, CpuSimulation simulation) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.simulation = simulation;
    }

    @Override
    public void compute() {
        if (simulation.isCollisionExists()) {
            return;
        }
        if (toIndex - fromIndex <= threshold) {
            outerloop:
            for (SimulationObject object : simulation.getAuxiliaryObjects().subList(fromIndex, toIndex)) {
                if (simulation.isCollisionExists()) {
                    break;
                }
                for (SimulationObject object1 : simulation.getAuxiliaryObjects()) {
                    if (object == object1) {
                        continue;
                    }
                    // distance between centres
                    Number distance = simulation.calculateDistance(object, object1);

                    if (distance.compareTo(object.getRadius().add(object1.getRadius())) <= 0) {
                        simulation.upCollisionExists();
                        break outerloop;
                    }
                }
            }
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            int middle = fromIndex + ((toIndex - fromIndex) / 2);
            subtasks.add(new CollisionCheckRecursiveAction(fromIndex, middle, simulation));
            subtasks.add(new CollisionCheckRecursiveAction(middle, toIndex, simulation));
            ForkJoinTask.invokeAll(subtasks);
        }
    }
}
