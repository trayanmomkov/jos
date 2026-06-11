package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.impl.FlatBarnesHutTree;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * The flat (GPU-ready) tree must mirror the object tree exactly: same preorder layout, same
 * geometry, mass distribution and leaf contents, and rope pointers (firstChild/skip) which
 * reproduce the recursive traversal order. Under the DOUBLE number implementation all values
 * must match bit for bit - that is what makes the GPU kernels comparable to the CPU results.
 */
public class FlatBarnesHutTreeTest {

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, 16);
    }

    @Test
    public void randomSceneMirrorsTheObjectTree() {
        Random random = new Random(11);
        int n = 200;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = (random.nextDouble() - 0.5) * 1000;
            y[i] = (random.nextDouble() - 0.5) * 1000;
            m[i] = 1e10 * (1 + random.nextDouble());
            r[i] = 1 + random.nextDouble() * 4;
        }
        assertMirrors(x, y, m, r, new boolean[n]);
    }

    @Test
    public void deletedObjectsAreExcluded() {
        Random random = new Random(12);
        int n = 90;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        boolean[] deleted = new boolean[n];
        for (int i = 0; i < n; i++) {
            x[i] = (random.nextDouble() - 0.5) * 1000;
            y[i] = (random.nextDouble() - 0.5) * 1000;
            m[i] = 1e10;
            r[i] = 2;
            deleted[i] = i % 3 == 0;
        }
        assertMirrors(x, y, m, r, deleted);
    }

    @Test
    public void coincidentObjectsFormTheSameBucket() {
        assertMirrors(new double[]{5, 5, 5, 200}, new double[]{5, 5, 5, 200},
                      new double[]{2, 3, 4, 10}, new double[]{1, 1, 1, 2}, new boolean[4]);
    }

    @Test
    public void allObjectsAtTheSamePoint() {
        assertMirrors(new double[]{7, 7, 7, 7}, new double[]{7, 7, 7, 7},
                      new double[]{1, 2, 3, 4}, new double[]{1, 1, 1, 1}, new boolean[4]);
    }

    @Test
    public void zeroMassSubtreeKeepsADefinedCenter() {
        assertMirrors(new double[]{0, 1, 500, 501}, new double[]{0, 1, 500, 501},
                      new double[]{0, 0, 1e10, 1e10}, new double[]{1, 1, 1, 1}, new boolean[4]);
    }

    @Test
    public void singleObject() {
        assertMirrors(new double[]{3}, new double[]{4}, new double[]{5}, new double[]{6}, new boolean[1]);
    }

    @Test
    public void noAliveObjectsProducesASafeSentinel() {
        FlatBarnesHutTree flat = new FlatBarnesHutTree(2);
        assertTrue(flat.build(new double[]{1, 2}, new double[]{0, 0}, new double[]{1, 1},
                              new double[]{1, 1}, new boolean[]{true, true}, 2));
        assertEquals(flat.getNodeCount(), 1);
        assertEquals(flat.nodeFirstChild[0], -1);
        assertEquals(flat.nodeBodyCount[0], 0);
        assertEquals(flat.nodeSkip[0], -1);
    }

    @Test
    public void overflowReturnsFalse() {
        /* Capacity for 3 nodes cannot hold 4 separated objects (1 root + 4 leaves). */
        FlatBarnesHutTree tiny = new FlatBarnesHutTree(4, 3);
        assertFalse(tiny.build(new double[]{-10, 10, -10, 10}, new double[]{-10, -10, 10, 10},
                               new double[]{1, 1, 1, 1}, new double[]{1, 1, 1, 1}, new boolean[4], 4));

        /* Many nearly coincident pairs burn deep single-child chains and exceed 4n+64. */
        int pairs = 30;
        int n = pairs * 2;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        for (int p = 0; p < pairs; p++) {
            x[2 * p] = p * 1000;
            x[2 * p + 1] = p * 1000 + 1e-9;
            m[2 * p] = 1;
            m[2 * p + 1] = 1;
            r[2 * p] = 0.1;
            r[2 * p + 1] = 0.1;
        }
        FlatBarnesHutTree flat = new FlatBarnesHutTree(n);
        assertFalse(flat.build(x, y, m, r, new boolean[n], n), "Chain scene should exceed 4n+64 nodes");
    }

    @Test
    public void largeSceneIsBuiltInParallelAndMirrorsTheObjectTree() {
        /* Big enough to cross the parallel build threshold. A dense cluster plus a uniform
         * spread exercises both deep and shallow partitions. The same instance builds twice,
         * so the reused partition buffers are covered too. */
        Random random = new Random(13);
        int n = 20000;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
            if (i % 5 == 0) {
                x[i] = (random.nextDouble() - 0.5) * 2000;
                y[i] = (random.nextDouble() - 0.5) * 2000;
            } else {
                x[i] = 100 + random.nextDouble() * 20;
                y[i] = -300 + random.nextDouble() * 20;
            }
            m[i] = 1e10 * (1 + random.nextDouble());
            r[i] = 0.1 + random.nextDouble();
        }
        FlatBarnesHutTree flat = new FlatBarnesHutTree(n);
        assertTrue(flat.build(x, y, m, r, new boolean[n], n), "Tree should fit in the default capacity");
        compareToObjectTree(flat, x, y, m, r, new boolean[n]);

        /* Shift the scene and rebuild on the same instance. */
        for (int i = 0; i < n; i++) {
            x[i] += 5;
            y[i] -= 3;
        }
        assertTrue(flat.build(x, y, m, r, new boolean[n], n), "Tree should fit in the default capacity");
        compareToObjectTree(flat, x, y, m, r, new boolean[n]);
    }

    @Test
    public void largeSceneWithDeletedObjectsMirrorsTheObjectTree() {
        Random random = new Random(14);
        int n = 30000;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        boolean[] deleted = new boolean[n];
        for (int i = 0; i < n; i++) {
            x[i] = (random.nextDouble() - 0.5) * 5000;
            y[i] = (random.nextDouble() - 0.5) * 5000;
            m[i] = 1e10;
            r[i] = 1;
            deleted[i] = i % 3 == 0;
        }
        FlatBarnesHutTree flat = new FlatBarnesHutTree(n);
        assertTrue(flat.build(x, y, m, r, deleted, n), "Tree should fit in the default capacity");
        compareToObjectTree(flat, x, y, m, r, deleted);
    }

    @Test
    public void largeOverflowingSceneReturnsFalseFromTheParallelBuild() {
        /* Nearly coincident pairs burn deep single-child chains; with thousands of pairs the
         * parallel build must also detect that 4n+64 nodes are not enough and give up. */
        int pairs = 10000;
        int n = pairs * 2;
        double[] x = new double[n];
        double[] y = new double[n];
        double[] m = new double[n];
        double[] r = new double[n];
        for (int p = 0; p < pairs; p++) {
            x[2 * p] = p * 1000;
            x[2 * p + 1] = p * 1000 + 1e-9;
            m[2 * p] = 1;
            m[2 * p + 1] = 1;
            r[2 * p] = 0.1;
            r[2 * p + 1] = 0.1;
        }
        FlatBarnesHutTree flat = new FlatBarnesHutTree(n);
        assertFalse(flat.build(x, y, m, r, new boolean[n], n), "Chain scene should exceed 4n+64 nodes");
    }

    @Test
    public void treeInstanceIsReusableAcrossBuilds() {
        FlatBarnesHutTree flat = new FlatBarnesHutTree(4);

        double[] x1 = {-10, 10, -10, 10};
        double[] y1 = {-10, -10, 10, 10};
        double[] m1 = {1, 2, 3, 4};
        double[] r1 = {1, 1, 1, 1};
        assertTrue(flat.build(x1, y1, m1, r1, new boolean[4], 4));
        compareToObjectTree(flat, x1, y1, m1, r1, new boolean[4]);

        double[] x2 = {5, 5, 5, 200};
        double[] y2 = {5, 5, 5, 200};
        double[] m2 = {2, 3, 4, 10};
        double[] r2 = {1, 1, 1, 2};
        assertTrue(flat.build(x2, y2, m2, r2, new boolean[4], 4));
        compareToObjectTree(flat, x2, y2, m2, r2, new boolean[4]);
    }

    private static void assertMirrors(double[] x, double[] y, double[] m, double[] r, boolean[] deleted) {
        FlatBarnesHutTree flat = new FlatBarnesHutTree(x.length);
        assertTrue(flat.build(x, y, m, r, deleted, x.length), "Tree should fit in the default capacity");
        compareToObjectTree(flat, x, y, m, r, deleted);
    }

    private static void compareToObjectTree(FlatBarnesHutTree flat, double[] x, double[] y, double[] m, double[] r,
                                            boolean[] deleted) {
        BarnesHutTree objectTree = BarnesHutTree.build(numbers(x), numbers(y), numbers(m), numbers(r), deleted, x.length);
        assertEquals(flat.getNodeCount(), countNodes(objectTree.getRoot()), "Node count");
        compare(objectTree.getRoot(), flat, 0, -1);
    }

    private static void compare(BarnesHutTree.Node node, FlatBarnesHutTree flat, int index, int continuation) {
        String at = "node " + index;
        assertEquals(flat.nodeMinX[index], node.minX.doubleValue(), 0.0, "minX of " + at);
        assertEquals(flat.nodeMinY[index], node.minY.doubleValue(), 0.0, "minY of " + at);
        assertEquals(flat.nodeSide[index], node.side.doubleValue(), 0.0, "side of " + at);
        assertEquals(flat.nodeSideSquared[index], node.sideSquared.doubleValue(), 0.0, "sideSquared of " + at);
        assertEquals(flat.nodeMass[index], node.mass.doubleValue(), 0.0, "mass of " + at);
        assertEquals(flat.nodeCenterOfMassX[index], node.centerOfMassX.doubleValue(), 0.0, "centerOfMassX of " + at);
        assertEquals(flat.nodeCenterOfMassY[index], node.centerOfMassY.doubleValue(), 0.0, "centerOfMassY of " + at);
        assertEquals(flat.nodeMaxRadius[index], node.maxRadius.doubleValue(), 0.0, "maxRadius of " + at);
        assertEquals(flat.nodeSkip[index], continuation, "skip of " + at);

        if (node.children != null) {
            assertEquals(flat.nodeFirstChild[index], index + 1, "firstChild of " + at);

            int lastQuadrant = -1;
            for (int q = 0; q < 4; q++) {
                if (node.children[q] != null) {
                    lastQuadrant = q;
                }
            }
            int childIndex = index + 1;
            for (int q = 0; q < 4; q++) {
                if (node.children[q] != null) {
                    int childSize = countNodes(node.children[q]);
                    int childContinuation = q == lastQuadrant ? continuation : childIndex + childSize;
                    compare(node.children[q], flat, childIndex, childContinuation);
                    childIndex += childSize;
                }
            }
        } else {
            assertEquals(flat.nodeFirstChild[index], -1, "leaf marker of " + at);
            if (node.bucket != null) {
                assertEquals(flat.nodeBodyCount[index], node.bucket.size(), "bucket size of " + at);
                for (int k = 0; k < node.bucket.size(); k++) {
                    assertEquals(flat.leafBodies[flat.nodeFirstBody[index] + k], (int) node.bucket.get(k),
                                 "bucket body " + k + " of " + at);
                }
            } else {
                assertEquals(flat.nodeBodyCount[index], 1, "body count of " + at);
                assertEquals(flat.leafBodies[flat.nodeFirstBody[index]], node.objectIndex, "body of " + at);
            }
        }
    }

    private static int countNodes(BarnesHutTree.Node node) {
        if (node == null) {
            return 0;
        }
        int count = 1;
        if (node.children != null) {
            for (BarnesHutTree.Node child : node.children) {
                count += countNodes(child);
            }
        }
        return count;
    }

    private static Number[] numbers(double[] values) {
        Number[] result = new Number[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = New.num(values[i]);
        }
        return result;
    }
}
