package info.trekto.jos.core.impl.arbitrary_precision;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * @author Trayan Momkov
 * 2017-Aug-5 21:22:53
 */
class CheckCollisionsRecursiveAction extends RecursiveAction {

    public static int threshold = 20;
    private final int fromIndex;
    private final int toIndex;
    private final ProcessCollisionsLogicAP processCollisionsLogic;

    public CheckCollisionsRecursiveAction(int fromIndex, int toIndex, ProcessCollisionsLogicAP processCollisionsLogic) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.processCollisionsLogic = processCollisionsLogic;
    }

    @Override
    public void compute() {
        if (toIndex - fromIndex <= threshold) {
            processCollisionsLogic.processCollisions(fromIndex, toIndex);
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            int middle = fromIndex + ((toIndex - fromIndex) / 2);
            subtasks.add(new CheckCollisionsRecursiveAction(fromIndex, middle, processCollisionsLogic));
            subtasks.add(new CheckCollisionsRecursiveAction(middle, toIndex, processCollisionsLogic));
            ForkJoinTask.invokeAll(subtasks);
        }
    }

}
