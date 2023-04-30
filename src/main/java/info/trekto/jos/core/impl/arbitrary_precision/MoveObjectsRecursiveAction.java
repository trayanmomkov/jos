package info.trekto.jos.core.impl.arbitrary_precision;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

/**
 * @author Trayan Momkov
 * 2017-Aug-5 21:22:53
 */
class MoveObjectsRecursiveAction extends RecursiveAction {

    public static final int THRESHOLD = 20;
    private final int fromIndex;
    private final int toIndex;
    private final MoveObjectsLogicAP moveLogic;

    public MoveObjectsRecursiveAction(int fromIndex, int toIndex, MoveObjectsLogicAP moveLogic) {
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.moveLogic = moveLogic;
    }

    @Override
    public void compute() {
        if (toIndex - fromIndex <= THRESHOLD) {
            moveLogic.calculateNewValues(fromIndex, toIndex);
        } else {
            List<RecursiveAction> subtasks = new ArrayList<>();
            int middle = fromIndex + ((toIndex - fromIndex) / 2);
            subtasks.add(new MoveObjectsRecursiveAction(fromIndex, middle, moveLogic));
            subtasks.add(new MoveObjectsRecursiveAction(middle, toIndex, moveLogic));
            ForkJoinTask.invokeAll(subtasks);
        }
    }

}
