package info.trekto.jos.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Barnes-Hut quadtree stored in arrays for the GPU kernels. It is built on the CPU on every iteration,
 * on all cores for big scenes. The nodes are in preorder: a kernel goes to nodeFirstChild to open a node
 * and to nodeSkip (the next node after the subtree) to pass it, so it needs no recursion and no stack.
 * The arrays have a fixed size. build() returns false when the tree does not fit in them.
 */
public class FlatBarnesHutTree {
    /* Must match BarnesHutTree.MAX_DEPTH */
    public static final int MAX_DEPTH = 50;

    /* Scenes below the threshold are built single-threaded - the fork-join overhead would
     * exceed the work. Bigger scenes partition by quadrant until a task's object range falls
     * under the cutoff and is built sequentially. */
    private static final int PARALLEL_THRESHOLD = 16384;
    private static final int TASK_CUTOFF = 2048;
    /* Subtrees smaller than this are emitted without forking a task for them. */
    private static final int EMIT_TASK_CUTOFF = 2048;
    /* The parallel tasks report created nodes to the shared counter in chunks of this size,
     * so a hopeless scene is abandoned early without contending on every node. */
    private static final int COUNT_FLUSH_CHUNK = 1024;

    /* Per node, indexed by preorder position. Read by the GPU kernels. */
    public final double[] nodeCenterOfMassX;
    public final double[] nodeCenterOfMassY;
    public final double[] nodeMass;
    public final double[] nodeSideSquared;
    public final double[] nodeMinX;
    public final double[] nodeMinY;
    public final double[] nodeSide;
    public final double[] nodeMaxRadius;
    /* First child in preorder; -1 for a leaf */
    public final int[] nodeFirstChild;
    /* Next node in preorder after this node's subtree; -1 at the end */
    public final int[] nodeSkip;
    /* Objects of a leaf: nodeBodyCount entries in leafBodies starting at nodeFirstBody */
    public final int[] nodeFirstBody;
    public final int[] nodeBodyCount;
    public final int[] leafBodies;

    private final int capacity;
    private int nodeCount;

    /* Current build inputs; only valid during build() */
    private double[] x;
    private double[] y;
    private double[] mass;
    private double[] radius;

    /* Ping-pong index buffers of the parallel partition; allocated on the first parallel build */
    private int[] partitionSource;
    private int[] partitionTarget;

    private static class TmpNode {
        final double minX;
        final double minY;
        final double side;
        final int depth;
        TmpNode[] children;
        int objectIndex = -1;
        List<Integer> bucket;
        /* Subtree size - needed to know the next sibling's preorder index */
        int size;
        /* Objects in the subtree and the partition marker; only set and used by the
         * parallel build, to place the children of a partition node in leafBodies. */
        int bodies;
        boolean partitioned;

        TmpNode(double minX, double minY, double side, int depth) {
            this.minX = minX;
            this.minY = minY;
            this.side = side;
            this.depth = depth;
        }
    }

    /* Counts the nodes created by one build. The parallel tasks report to a shared counter in chunks,
     * so all of them stop soon after the tree gets too big. */
    private static final class NodeBudget {
        private final int capacity;
        private final AtomicInteger shared;
        private int local;
        private boolean exceeded;

        NodeBudget(int capacity, AtomicInteger shared) {
            this.capacity = capacity;
            this.shared = shared;
        }

        boolean tryCreate() {
            if (shared == null) {
                if (local == capacity) {
                    return false;
                }
                local++;
                return true;
            }
            if (exceeded) {
                return false;
            }
            if (shared.get() > capacity) {
                exceeded = true;
                return false;
            }
            local++;
            if (local == COUNT_FLUSH_CHUNK) {
                flush();
            }
            return !exceeded;
        }

        void flush() {
            if (shared != null && local > 0) {
                if (shared.addAndGet(local) > capacity) {
                    exceeded = true;
                }
                local = 0;
            }
        }
    }

    public FlatBarnesHutTree(int n) {
        this(n, 4 * n + 64);
    }

    public FlatBarnesHutTree(int n, int capacity) {
        this.capacity = capacity;
        nodeCenterOfMassX = new double[capacity];
        nodeCenterOfMassY = new double[capacity];
        nodeMass = new double[capacity];
        nodeSideSquared = new double[capacity];
        nodeMinX = new double[capacity];
        nodeMinY = new double[capacity];
        nodeSide = new double[capacity];
        nodeMaxRadius = new double[capacity];
        nodeFirstChild = new int[capacity];
        nodeSkip = new int[capacity];
        nodeFirstBody = new int[capacity];
        nodeBodyCount = new int[capacity];
        leafBodies = new int[Math.max(n, 1)];
    }

    public int getNodeCount() {
        return nodeCount;
    }

    /* Builds the tree of the not deleted objects. Returns false when it needs more nodes than the capacity;
     * the arrays must not be used then. */
    public boolean build(double[] x, double[] y, double[] mass, double[] radius, boolean[] deleted, int n) {
        this.x = x;
        this.y = y;
        this.mass = mass;
        this.radius = radius;

        /* Bounding square of all alive objects. min/max mirror Number.min/max:
         * the first operand wins on equality. */
        int alive = 0;
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        for (int i = 0; i < n; i++) {
            if (!deleted[i]) {
                if (alive == 0) {
                    minX = x[i];
                    maxX = x[i];
                    minY = y[i];
                    maxY = y[i];
                } else {
                    minX = Double.compare(minX, x[i]) <= 0 ? minX : x[i];
                    maxX = Double.compare(maxX, x[i]) >= 0 ? maxX : x[i];
                    minY = Double.compare(minY, y[i]) <= 0 ? minY : y[i];
                    maxY = Double.compare(maxY, y[i]) >= 0 ? maxY : y[i];
                }
                alive++;
            }
        }

        if (alive == 0) {
            /* Sentinel: an empty leaf on which both kernel walks safely do nothing. */
            nodeCount = 1;
            nodeCenterOfMassX[0] = 0;
            nodeCenterOfMassY[0] = 0;
            nodeMass[0] = 0;
            nodeSideSquared[0] = 0;
            nodeMinX[0] = 0;
            nodeMinY[0] = 0;
            nodeSide[0] = 0;
            nodeMaxRadius[0] = 0;
            nodeFirstChild[0] = -1;
            nodeSkip[0] = -1;
            nodeFirstBody[0] = 0;
            nodeBodyCount[0] = 0;
            return true;
        }

        double sideX = maxX - minX;
        double sideY = maxY - minY;
        double side = Double.compare(sideX, sideY) >= 0 ? sideX : sideY;
        if (Double.compare(side, 0.0) == 0) {
            /* All objects are at the same point. Any positive side works. */
            side = 1;
        }

        if (alive >= PARALLEL_THRESHOLD) {
            return buildParallel(deleted, n, alive, minX, minY, side);
        }

        NodeBudget budget = new NodeBudget(capacity, null);
        TmpNode root = new TmpNode(minX, minY, side, 0);
        if (!budget.tryCreate()) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (!deleted[i]) {
                if (!insert(root, i, budget)) {
                    return false;
                }
            }
        }

        computeSize(root);
        emit(root, 0, -1, 0);
        nodeCount = root.size;
        return true;
    }

    private boolean buildParallel(boolean[] deleted, int n, int alive, double minX, double minY, double side) {
        if (partitionSource == null || partitionSource.length < n) {
            partitionSource = new int[n];
            partitionTarget = new int[n];
        }
        int cursor = 0;
        for (int i = 0; i < n; i++) {
            if (!deleted[i]) {
                partitionSource[cursor++] = i;
            }
        }

        AtomicInteger created = new AtomicInteger();
        TmpNode root = new BuildTask(partitionSource, partitionTarget, 0, alive, minX, minY, side, 0, created).invoke();
        if (root == null || root.size > capacity) {
            return false;
        }
        new EmitTask(root, 0, -1, 0).invoke();
        nodeCount = root.size;
        return true;
    }

    /* Builds the subtree of one cell from bodies[from..to), which keep their original order.
     * The result is the same subtree the single-threaded build makes. */
    private final class BuildTask extends RecursiveTask<TmpNode> {
        private final int[] bodies;
        private final int[] scratch;
        private final int from;
        private final int to;
        private final double minX;
        private final double minY;
        private final double side;
        private final int depth;
        private final AtomicInteger created;

        BuildTask(int[] bodies, int[] scratch, int from, int to,
                  double minX, double minY, double side, int depth, AtomicInteger created) {
            this.bodies = bodies;
            this.scratch = scratch;
            this.from = from;
            this.to = to;
            this.minX = minX;
            this.minY = minY;
            this.side = side;
            this.depth = depth;
            this.created = created;
        }

        @Override
        protected TmpNode compute() {
            NodeBudget budget = new NodeBudget(capacity, created);
            int count = to - from;
            if (count <= TASK_CUTOFF || depth >= MAX_DEPTH) {
                TmpNode node = buildSubtreeSequentially(budget);
                budget.flush();
                return node;
            }

            /* The same geometry as childFor: strictly greater than the middle goes right/top. */
            double half = side * 0.5;
            double middleX = minX + half;
            double middleY = minY + half;

            int count0 = 0;
            int count1 = 0;
            int count2 = 0;
            int count3 = 0;
            for (int k = from; k < to; k++) {
                int i = bodies[k];
                boolean right = Double.compare(x[i], middleX) > 0;
                boolean top = Double.compare(y[i], middleY) > 0;
                if (top) {
                    if (right) {
                        count3++;
                    } else {
                        count2++;
                    }
                } else {
                    if (right) {
                        count1++;
                    } else {
                        count0++;
                    }
                }
            }

            /* Stable scatter: the index order inside every quadrant stays untouched, so the
             * leaf contents come out exactly as with one-by-one insertion. */
            int offset0 = from;
            int offset1 = offset0 + count0;
            int offset2 = offset1 + count1;
            int offset3 = offset2 + count2;
            for (int k = from; k < to; k++) {
                int i = bodies[k];
                boolean right = Double.compare(x[i], middleX) > 0;
                boolean top = Double.compare(y[i], middleY) > 0;
                if (top) {
                    if (right) {
                        scratch[offset3++] = i;
                    } else {
                        scratch[offset2++] = i;
                    }
                } else {
                    if (right) {
                        scratch[offset1++] = i;
                    } else {
                        scratch[offset0++] = i;
                    }
                }
            }

            if (!budget.tryCreate()) {
                budget.flush();
                return null;
            }
            TmpNode node = new TmpNode(minX, minY, side, depth);
            node.partitioned = true;
            node.children = new TmpNode[4];
            node.bodies = count;

            BuildTask task1 = childTask(1, from + count0, count1, middleX, minY, half);
            BuildTask task2 = childTask(2, from + count0 + count1, count2, minX, middleY, half);
            BuildTask task3 = childTask(3, from + count0 + count1 + count2, count3, middleX, middleY, half);
            if (task1 != null) {
                task1.fork();
            }
            if (task2 != null) {
                task2.fork();
            }
            if (task3 != null) {
                task3.fork();
            }
            BuildTask task0 = childTask(0, from, count0, minX, minY, half);
            boolean ok = collectChild(node, 0, count0, from, task0 == null ? null : task0.invoke(), budget);
            ok = ok && collectChild(node, 1, count1, from + count0, task1 == null ? null : task1.join(), budget);
            ok = ok && collectChild(node, 2, count2, from + count0 + count1, task2 == null ? null : task2.join(), budget);
            ok = ok && collectChild(node, 3, count3, from + count0 + count1 + count2,
                                    task3 == null ? null : task3.join(), budget);
            budget.flush();
            if (!ok) {
                return null;
            }
            node.size = 1 + childSize(node, 0) + childSize(node, 1) + childSize(node, 2) + childSize(node, 3);
            return node;
        }

        /** A task for a quadrant with two or more objects; null for empty and single-object quadrants. */
        private BuildTask childTask(int quadrant, int childFrom, int childCount,
                                    double childMinX, double childMinY, double childSide) {
            if (childCount < 2) {
                return null;
            }
            /* The buffers swap roles one level down. */
            return new BuildTask(scratch, bodies, childFrom, childFrom + childCount,
                                 childMinX, childMinY, childSide, depth + 1, created);
        }

        /* Puts the subtree of a quadrant under the node: nothing for an empty quadrant, a leaf for one object,
         * the result of the task otherwise. Returns false when there is no more capacity. */
        private boolean collectChild(TmpNode node, int quadrant, int childCount, int childFrom,
                                     TmpNode computed, NodeBudget budget) {
            if (childCount == 0) {
                return true;
            }
            if (childCount == 1) {
                if (!budget.tryCreate()) {
                    return false;
                }
                double half = node.side * 0.5;
                boolean right = quadrant == 1 || quadrant == 3;
                boolean top = quadrant == 2 || quadrant == 3;
                TmpNode leaf = new TmpNode(right ? node.minX + half : node.minX,
                                           top ? node.minY + half : node.minY, half, node.depth + 1);
                leaf.objectIndex = scratch[childFrom];
                leaf.size = 1;
                leaf.bodies = 1;
                node.children[quadrant] = leaf;
                return true;
            }
            if (computed == null) {
                return false;
            }
            node.children[quadrant] = computed;
            return true;
        }

        private TmpNode buildSubtreeSequentially(NodeBudget budget) {
            if (!budget.tryCreate()) {
                return null;
            }
            TmpNode node = new TmpNode(minX, minY, side, depth);
            for (int k = from; k < to; k++) {
                if (!insert(node, bodies[k], budget)) {
                    return null;
                }
            }
            computeSize(node);
            node.bodies = to - from;
            return node;
        }
    }

    private int childSize(TmpNode node, int quadrant) {
        return node.children[quadrant] == null ? 0 : node.children[quadrant].size;
    }

    private boolean insert(TmpNode start, int i, NodeBudget budget) {
        TmpNode node = start;
        while (true) {
            if (node.children != null) {
                /* Internal node - descend */
                node = childFor(node, i, budget);
                if (node == null) {
                    return false;
                }
            } else if (node.bucket != null) {
                node.bucket.add(i);
                return true;
            } else if (node.objectIndex == -1) {
                /* Empty leaf */
                node.objectIndex = i;
                return true;
            } else if (node.depth == MAX_DEPTH) {
                /* Cannot subdivide further - collect the objects in a list */
                node.bucket = new ArrayList<>();
                node.bucket.add(node.objectIndex);
                node.bucket.add(i);
                node.objectIndex = -1;
                return true;
            } else {
                /* Occupied leaf - subdivide and reinsert the existing object,
                 * then continue descending with the new one. */
                int existing = node.objectIndex;
                node.objectIndex = -1;
                node.children = new TmpNode[4];
                TmpNode child = childFor(node, existing, budget);
                if (child == null) {
                    return false;
                }
                child.objectIndex = existing;
            }
        }
    }

    private TmpNode childFor(TmpNode node, int i, NodeBudget budget) {
        double half = node.side * 0.5;
        double middleX = node.minX + half;
        double middleY = node.minY + half;
        boolean right = Double.compare(x[i], middleX) > 0;
        boolean top = Double.compare(y[i], middleY) > 0;
        int quadrant = (right ? 1 : 0) + (top ? 2 : 0);
        if (node.children[quadrant] == null) {
            if (!budget.tryCreate()) {
                return null;
            }
            node.children[quadrant] = new TmpNode(right ? middleX : node.minX, top ? middleY : node.minY, half, node.depth + 1);
        }
        return node.children[quadrant];
    }

    private int computeSize(TmpNode node) {
        int size = 1;
        if (node.children != null) {
            for (TmpNode child : node.children) {
                if (child != null) {
                    size += computeSize(child);
                }
            }
        }
        node.size = size;
        return size;
    }

    /* Writes a partitioned subtree to the arrays. The index ranges of the children are known in advance
     * and do not overlap, so the children are written in parallel. */
    private final class EmitTask extends RecursiveAction {
        private final TmpNode node;
        private final int index;
        private final int continuation;
        private final int bodyCursor;

        EmitTask(TmpNode node, int index, int continuation, int bodyCursor) {
            this.node = node;
            this.index = index;
            this.continuation = continuation;
            this.bodyCursor = bodyCursor;
        }

        @Override
        protected void compute() {
            if (!node.partitioned || node.size <= EMIT_TASK_CUTOFF) {
                emit(node, index, continuation, bodyCursor);
                return;
            }

            nodeMinX[index] = node.minX;
            nodeMinY[index] = node.minY;
            nodeSide[index] = node.side;
            nodeSideSquared[index] = node.side * node.side;
            nodeSkip[index] = continuation;
            nodeFirstChild[index] = index + 1;
            nodeFirstBody[index] = -1;
            nodeBodyCount[index] = 0;

            /* Find the last existing child - it continues to this node's continuation. */
            int lastQuadrant = -1;
            for (int q = 0; q < 4; q++) {
                if (node.children[q] != null) {
                    lastQuadrant = q;
                }
            }

            EmitTask[] tasks = new EmitTask[4];
            int childIndex = index + 1;
            int childCursor = bodyCursor;
            for (int q = 0; q < 4; q++) {
                TmpNode child = node.children[q];
                if (child != null) {
                    tasks[q] = new EmitTask(child, childIndex, q == lastQuadrant ? continuation : childIndex + child.size,
                                            childCursor);
                    childIndex += child.size;
                    childCursor += child.bodies;
                }
            }
            if (tasks[1] != null) {
                tasks[1].fork();
            }
            if (tasks[2] != null) {
                tasks[2].fork();
            }
            if (tasks[3] != null) {
                tasks[3].fork();
            }
            if (tasks[0] != null) {
                tasks[0].invoke();
            }
            if (tasks[1] != null) {
                tasks[1].join();
            }
            if (tasks[2] != null) {
                tasks[2].join();
            }
            if (tasks[3] != null) {
                tasks[3].join();
            }

            /* The same aggregation the single-threaded emit performs after every child. */
            double totalMass = 0;
            double weightedX = 0;
            double weightedY = 0;
            double maxRadius = 0;
            boolean maxRadiusSet = false;
            childIndex = index + 1;
            for (int q = 0; q < 4; q++) {
                TmpNode child = node.children[q];
                if (child != null) {
                    totalMass = totalMass + nodeMass[childIndex];
                    weightedX = weightedX + nodeMass[childIndex] * nodeCenterOfMassX[childIndex];
                    weightedY = weightedY + nodeMass[childIndex] * nodeCenterOfMassY[childIndex];
                    if (maxRadiusSet) {
                        maxRadius = Double.compare(maxRadius, nodeMaxRadius[childIndex]) >= 0 ? maxRadius : nodeMaxRadius[childIndex];
                    } else {
                        maxRadius = nodeMaxRadius[childIndex];
                        maxRadiusSet = true;
                    }
                    childIndex += child.size;
                }
            }
            setMassDistribution(index, totalMass, weightedX, weightedY);
            nodeMaxRadius[index] = maxRadius;
        }
    }

    /** Emits the subtree in preorder. Returns the leafBodies cursor after the subtree's objects. */
    private int emit(TmpNode node, int index, int continuation, int bodyCursor) {
        nodeMinX[index] = node.minX;
        nodeMinY[index] = node.minY;
        nodeSide[index] = node.side;
        nodeSideSquared[index] = node.side * node.side;
        nodeSkip[index] = continuation;

        if (node.children != null) {
            nodeFirstChild[index] = index + 1;
            nodeFirstBody[index] = -1;
            nodeBodyCount[index] = 0;

            /* Find the last existing child - it continues to this node's continuation. */
            int lastQuadrant = -1;
            for (int q = 0; q < 4; q++) {
                if (node.children[q] != null) {
                    lastQuadrant = q;
                }
            }

            /* Children are emitted left to right in quadrant order, so the preorder positions
             * and therefore the traversal order match the object tree recursion exactly. */
            double totalMass = 0;
            double weightedX = 0;
            double weightedY = 0;
            double maxRadius = 0;
            boolean maxRadiusSet = false;
            int childIndex = index + 1;
            for (int q = 0; q < 4; q++) {
                TmpNode child = node.children[q];
                if (child != null) {
                    int childSize = child.size;
                    bodyCursor = emit(child, childIndex, q == lastQuadrant ? continuation : childIndex + childSize, bodyCursor);

                    totalMass = totalMass + nodeMass[childIndex];
                    weightedX = weightedX + nodeMass[childIndex] * nodeCenterOfMassX[childIndex];
                    weightedY = weightedY + nodeMass[childIndex] * nodeCenterOfMassY[childIndex];
                    if (maxRadiusSet) {
                        maxRadius = Double.compare(maxRadius, nodeMaxRadius[childIndex]) >= 0 ? maxRadius : nodeMaxRadius[childIndex];
                    } else {
                        maxRadius = nodeMaxRadius[childIndex];
                        maxRadiusSet = true;
                    }
                    childIndex += childSize;
                }
            }
            setMassDistribution(index, totalMass, weightedX, weightedY);
            nodeMaxRadius[index] = maxRadius;
        } else {
            nodeFirstChild[index] = -1;
            nodeFirstBody[index] = bodyCursor;
            if (node.bucket != null) {
                double totalMass = 0;
                double weightedX = 0;
                double weightedY = 0;
                double maxRadius = 0;
                boolean maxRadiusSet = false;
                for (int k = 0; k < node.bucket.size(); k++) {
                    int i = node.bucket.get(k);
                    leafBodies[bodyCursor++] = i;
                    totalMass = totalMass + mass[i];
                    weightedX = weightedX + mass[i] * x[i];
                    weightedY = weightedY + mass[i] * y[i];
                    if (maxRadiusSet) {
                        maxRadius = Double.compare(maxRadius, radius[i]) >= 0 ? maxRadius : radius[i];
                    } else {
                        maxRadius = radius[i];
                        maxRadiusSet = true;
                    }
                }
                nodeBodyCount[index] = node.bucket.size();
                setMassDistribution(index, totalMass, weightedX, weightedY);
                nodeMaxRadius[index] = maxRadius;
            } else {
                int i = node.objectIndex;
                leafBodies[bodyCursor++] = i;
                nodeBodyCount[index] = 1;
                nodeMass[index] = mass[i];
                nodeCenterOfMassX[index] = x[i];
                nodeCenterOfMassY[index] = y[i];
                nodeMaxRadius[index] = radius[i];
            }
        }
        return bodyCursor;
    }

    private void setMassDistribution(int index, double totalMass, double weightedX, double weightedY) {
        nodeMass[index] = totalMass;
        if (Double.compare(totalMass, 0.0) == 0) {
            /* Objects without mass exert no force; just give the node a defined center. */
            double half = nodeSide[index] * 0.5;
            nodeCenterOfMassX[index] = nodeMinX[index] + half;
            nodeCenterOfMassY[index] = nodeMinY[index] + half;
        } else {
            nodeCenterOfMassX[index] = weightedX / totalMass;
            nodeCenterOfMassY[index] = weightedY / totalMass;
        }
    }

    /* Copies the nodes for the float kernels. The bounds are rounded outwards, so no colliding pair is missed. */
    public void copyNodesToFloat(float[] centerOfMassX, float[] centerOfMassY, float[] mass, float[] sideSquared,
                                 float[] minX, float[] minY, float[] side, float[] maxRadius) {
        for (int i = 0; i < nodeCount; i++) {
            centerOfMassX[i] = (float) nodeCenterOfMassX[i];
            centerOfMassY[i] = (float) nodeCenterOfMassY[i];
            mass[i] = (float) nodeMass[i];
            sideSquared[i] = (float) nodeSideSquared[i];
            minX[i] = Math.nextDown((float) nodeMinX[i]);
            minY[i] = Math.nextDown((float) nodeMinY[i]);
            side[i] = Math.nextUp((float) nodeSide[i]);
            maxRadius[i] = Math.nextUp((float) nodeMaxRadius[i]);
        }
    }
}
