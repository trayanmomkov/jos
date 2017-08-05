package info.trekto.jos.core.impl;

import info.trekto.jos.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 *
 * @author Trayan Momkov
 * @date 2017-Aug-5 21:22:53
 */
public class SimulationRecursiveAction extends RecursiveAction {

    public static int THRESHOLD = 5;
    private final int fromIndex;
    private final int toIndex;

    public SimulationRecursiveAction(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    protected void compute() {
        if (toIndex - fromIndex <= THRESHOLD) {
            Container.getSimulationLogic().calculateNewValues(Container.getSimulation(), fromIndex, toIndex);
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            subtasks.add(new SimulationRecursiveAction(fromIndex, fromIndex + ((toIndex - fromIndex) / 2)));
            subtasks.add(new SimulationRecursiveAction(fromIndex + ((toIndex - fromIndex) / 2), toIndex));
            ForkJoinTask.invokeAll(subtasks);
        }
    }

}
