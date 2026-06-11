package info.trekto.jos.core.impl.single_precision;

import info.trekto.jos.core.impl.FlatBarnesHutTree;

/**
 * Float copies of the node values of a {@link FlatBarnesHutTree}, for the float kernels.
 */
public class FloatBarnesHutTreeArrays {
    public final float[] nodeCenterOfMassX;
    public final float[] nodeCenterOfMassY;
    public final float[] nodeMass;
    public final float[] nodeSideSquared;
    public final float[] nodeMinX;
    public final float[] nodeMinY;
    public final float[] nodeSide;
    public final float[] nodeMaxRadius;

    public FloatBarnesHutTreeArrays(FlatBarnesHutTree tree) {
        int capacity = tree.nodeCenterOfMassX.length;
        nodeCenterOfMassX = new float[capacity];
        nodeCenterOfMassY = new float[capacity];
        nodeMass = new float[capacity];
        nodeSideSquared = new float[capacity];
        nodeMinX = new float[capacity];
        nodeMinY = new float[capacity];
        nodeSide = new float[capacity];
        nodeMaxRadius = new float[capacity];
    }

    public void refresh(FlatBarnesHutTree tree) {
        tree.copyNodesToFloat(nodeCenterOfMassX, nodeCenterOfMassY, nodeMass, nodeSideSquared,
                              nodeMinX, nodeMinY, nodeSide, nodeMaxRadius);
    }
}
