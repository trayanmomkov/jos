package info.trekto.jos.core.impl.arbitrary_precision;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

import static info.trekto.jos.core.Controller.C;

/**
 * @author Trayan Momkov
 * 2017-Aug-5 21:22:53
 */
class SimulationRecursiveAction extends RecursiveAction {

    public static int threshold = 20;
    private final int fromIndex;
    private final int toIndex;

    public SimulationRecursiveAction(int fromIndex, int toIndex) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
    }

    @Override
    public void compute() {
        if (toIndex - fromIndex <= threshold) {
            ((SimulationAP) C.getSimulation()).getSimulationLogic().calculateNewValues(fromIndex, toIndex);
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            int middle = fromIndex + ((toIndex - fromIndex) / 2);
            subtasks.add(new SimulationRecursiveAction(fromIndex, middle));
            subtasks.add(new SimulationRecursiveAction(middle, toIndex));
            ForkJoinTask.invokeAll(subtasks);
        }
    }

}
