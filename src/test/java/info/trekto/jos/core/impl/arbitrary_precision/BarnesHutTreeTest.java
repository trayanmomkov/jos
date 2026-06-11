package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.areEqual;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class BarnesHutTreeTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    @Test
    public void singleObjectTreeIsALeafWithTheObjectAsMassDistribution() {
        BarnesHutTree tree = build(new double[]{3}, new double[]{4}, new double[]{5}, new boolean[1]);

        BarnesHutTree.Node root = tree.getRoot();
        assertNotNull(root);
        assertNull(root.children);
        assertEquals(root.objectIndex, 0);
        assertTrue(areEqual(root.mass, New.num(5), PRECISION - 2));
        assertTrue(areEqual(root.centerOfMassX, New.num(3), PRECISION - 2));
        assertTrue(areEqual(root.centerOfMassY, New.num(4), PRECISION - 2));
    }

    @Test
    public void fourObjectsLandInFourQuadrants() {
        /* Object index k is placed so that it lands in quadrant k (x then y, bottom-left first). */
        BarnesHutTree tree = build(new double[]{-10, 10, -10, 10},
                                   new double[]{-10, -10, 10, 10},
                                   new double[]{1, 1, 1, 1}, new boolean[4]);

        BarnesHutTree.Node root = tree.getRoot();
        assertNotNull(root.children);
        for (int q = 0; q < 4; q++) {
            assertNotNull(root.children[q], "Quadrant " + q);
            assertNull(root.children[q].children, "Quadrant " + q + " should be a leaf");
            assertEquals(root.children[q].objectIndex, q, "Quadrant " + q);
        }
        assertTrue(areEqual(root.mass, New.num(4), PRECISION - 2));
        assertTrue(areEqual(root.centerOfMassX, New.num(0), PRECISION - 2), "Root center of mass X: " + root.centerOfMassX);
        assertTrue(areEqual(root.centerOfMassY, New.num(0), PRECISION - 2), "Root center of mass Y: " + root.centerOfMassY);
    }

    @Test
    public void centerOfMassIsWeightedByMass() {
        /* m=1 at x=0 and m=3 at x=4: center of mass at x = (0*1 + 4*3) / 4 = 3 */
        BarnesHutTree tree = build(new double[]{0, 4}, new double[]{0, 0}, new double[]{1, 3}, new boolean[2]);

        BarnesHutTree.Node root = tree.getRoot();
        assertTrue(areEqual(root.mass, New.num(4), PRECISION - 2));
        assertTrue(areEqual(root.centerOfMassX, New.num(3), PRECISION - 2), "Center of mass X: " + root.centerOfMassX);
        assertTrue(areEqual(root.centerOfMassY, New.num(0), PRECISION - 2));
    }

    @Test
    public void deletedObjectsAreExcluded() {
        boolean[] deleted = {false, true, false};
        BarnesHutTree tree = build(new double[]{0, 5, 10}, new double[]{0, 0, 0}, new double[]{1, 100, 3}, deleted);

        BarnesHutTree.Node root = tree.getRoot();
        assertTrue(areEqual(root.mass, New.num(4), PRECISION - 2), "Mass without the deleted object: " + root.mass);
        /* (0*1 + 10*3) / 4 = 7.5 */
        assertTrue(areEqual(root.centerOfMassX, New.num("7.5"), PRECISION - 2), "Center of mass X: " + root.centerOfMassX);
        assertTrue(collectObjectIndexes(root).stream().noneMatch(index -> index == 1), "Deleted object must not be in the tree");
    }

    @Test
    public void coincidentObjectsTerminateInABucket() {
        /* Three objects at exactly the same point cannot be separated by subdivision.
         * The build must terminate, collecting them in a bucket at the maximum depth. */
        BarnesHutTree tree = build(new double[]{1, 1, 1, 100}, new double[]{1, 1, 1, 100},
                                   new double[]{2, 2, 2, 4}, new boolean[4]);

        BarnesHutTree.Node root = tree.getRoot();
        List<List<Integer>> buckets = new ArrayList<>();
        collectBuckets(root, buckets);
        assertEquals(buckets.size(), 1, "Exactly one bucket expected");
        assertEquals(buckets.get(0).size(), 3, "The three coincident objects share the bucket");
        assertTrue(areEqual(root.mass, New.num(10), PRECISION - 2));
        /* (1*6 + 100*4) / 10 = 40.6 */
        assertTrue(areEqual(root.centerOfMassX, New.num("40.6"), PRECISION - 2), "Center of mass X: " + root.centerOfMassX);
    }

    @Test
    public void treeWithoutAliveObjectsHasNoRoot() {
        assertNull(build(new double[0], new double[0], new double[0], new boolean[0]).getRoot());
        assertNull(build(new double[]{1, 2}, new double[]{0, 0}, new double[]{1, 1}, new boolean[]{true, true}).getRoot());
    }

    @Test
    public void overlapCandidatesContainNeighboursAndExcludeFarObjects() {
        /* Objects at x = 0 and 1.5 with r = 1 overlap; the object at x = 100 is far away. */
        BarnesHutTree tree = buildWithRadius(new double[]{0, 1.5, 100}, new double[]{0, 0, 0},
                                             new double[]{1, 1, 1}, new double[]{1, 1, 1}, new boolean[3]);

        List<Integer> candidates = tree.findOverlapCandidates(0);
        assertTrue(candidates.contains(1), "Overlapping neighbour must be a candidate: " + candidates);
        assertTrue(!candidates.contains(2), "Far object must be pruned: " + candidates);
    }

    @Test
    public void giantRadiusIsReachableAcrossTheScene() {
        /* The small object at x = 0 (r = 1) overlaps the giant at x = 50 only because of the
         * giant's radius (100). The node pruning must use the biggest radius in the subtree,
         * otherwise this pair is missed. */
        BarnesHutTree tree = buildWithRadius(new double[]{0, 50, 300}, new double[]{0, 0, 0},
                                             new double[]{1, 1, 1}, new double[]{1, 100, 1}, new boolean[3]);

        assertTrue(tree.findOverlapCandidates(0).contains(1), "The giant must be a candidate of the small object");
        assertTrue(tree.findOverlapCandidates(1).contains(0), "The small object must be a candidate of the giant");
        assertTrue(!tree.findOverlapCandidates(0).contains(2), "The far object must be pruned");
    }

    private static BarnesHutTree build(double[] x, double[] y, double[] mass, boolean[] deleted) {
        return BarnesHutTree.build(numbers(x), numbers(y), numbers(mass), deleted, x.length);
    }

    private static BarnesHutTree buildWithRadius(double[] x, double[] y, double[] mass, double[] radius, boolean[] deleted) {
        return BarnesHutTree.build(numbers(x), numbers(y), numbers(mass), numbers(radius), deleted, x.length);
    }

    private static Number[] numbers(double[] values) {
        Number[] result = new Number[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = New.num(values[i]);
        }
        return result;
    }

    private static List<Integer> collectObjectIndexes(BarnesHutTree.Node node) {
        List<Integer> result = new ArrayList<>();
        if (node == null) {
            return result;
        }
        if (node.children != null) {
            for (BarnesHutTree.Node child : node.children) {
                result.addAll(collectObjectIndexes(child));
            }
        } else if (node.bucket != null) {
            result.addAll(node.bucket);
        } else if (node.objectIndex != -1) {
            result.add(node.objectIndex);
        }
        return result;
    }

    private static void collectBuckets(BarnesHutTree.Node node, List<List<Integer>> buckets) {
        if (node == null) {
            return;
        }
        if (node.children != null) {
            for (BarnesHutTree.Node child : node.children) {
                collectBuckets(child, buckets);
            }
        } else if (node.bucket != null) {
            buckets.add(node.bucket);
        }
    }
}
