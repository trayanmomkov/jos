package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * With Barnes-Hut enabled the collision detection uses the tree only to narrow down the
 * candidate pairs - the detection itself stays exact. The candidates are processed in the
 * same order as the brute force loop, so the results of the two paths must be identical
 * bit by bit, in both the elastic and the merge mode.
 */
public class BarnesHutCollisionTest {
    public static final int PRECISION = 16;
    private static final int BODIES = 60;
    private static final String THETA = "0.7";
    private static final String COR = "0.75";

    private double[] x;
    private double[] y;
    private double[] vx;
    private double[] vy;
    private double[] mass;
    private double[] radius;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);

        /* Deterministic dense scene with several overlapping pairs */
        Random random = new Random(7);
        x = new double[BODIES];
        y = new double[BODIES];
        vx = new double[BODIES];
        vy = new double[BODIES];
        mass = new double[BODIES];
        radius = new double[BODIES];
        for (int i = 0; i < BODIES; i++) {
            x[i] = (random.nextDouble() - 0.5) * 200;
            y[i] = (random.nextDouble() - 0.5) * 200;
            vx[i] = (random.nextDouble() - 0.5) * 4;
            vy[i] = (random.nextDouble() - 0.5) * 4;
            mass[i] = 1e10 * (1 + random.nextDouble());
            radius[i] = 2 + random.nextDouble() * 4;
        }
    }

    @Test
    public void elasticCollisionsWithTreeMatchBruteForceExactly() {
        DataAP brute = sceneData();
        new ProcessCollisionsLogicAP(brute, false, New.num(COR)).runOnSingleThread();

        DataAP withTree = sceneData();
        new ProcessCollisionsLogicAP(withTree, false, New.num(COR), New.num(THETA)).runOnSingleThread();

        int collided = 0;
        for (int i = 0; i < BODIES; i++) {
            assertEquals(withTree.velocityX[i].compareTo(brute.velocityX[i]), 0, "Object " + i + " velocity X");
            assertEquals(withTree.velocityY[i].compareTo(brute.velocityY[i]), 0, "Object " + i + " velocity Y");
            assertEquals(withTree.positionX[i].compareTo(brute.positionX[i]), 0, "Object " + i + " position X");
            assertEquals(withTree.positionY[i].compareTo(brute.positionY[i]), 0, "Object " + i + " position Y");
            if (brute.velocityX[i].compareTo(New.num(vx[i])) != 0) {
                collided++;
            }
        }
        assertTrue(collided > 0, "The scene should contain collisions, otherwise the test proves nothing");
    }

    @Test
    public void parallelElasticCollisionsWithTreeEqualSerial() {
        DataAP serial = sceneData();
        new ProcessCollisionsLogicAP(serial, false, New.num(COR), New.num(THETA)).runOnSingleThread();

        DataAP parallel = sceneData();
        new ProcessCollisionsLogicAP(parallel, false, New.num(COR), New.num(THETA)).run();

        for (int i = 0; i < BODIES; i++) {
            assertEquals(parallel.velocityX[i].compareTo(serial.velocityX[i]), 0, "Object " + i + " velocity X");
            assertEquals(parallel.positionX[i].compareTo(serial.positionX[i]), 0, "Object " + i + " position X");
        }
    }

    @Test
    public void mergeCollisionsWithTreeMatchBruteForceExactly() {
        DataAP brute = sceneData();
        new ProcessCollisionsLogicAP(brute, true, New.num(COR)).runOnSingleThread();

        DataAP withTree = sceneData();
        new ProcessCollisionsLogicAP(withTree, true, New.num(COR), New.num(THETA)).runOnSingleThread();

        int deletions = 0;
        for (int i = 0; i < BODIES; i++) {
            assertEquals(withTree.deleted[i], brute.deleted[i], "Object " + i + " deleted flag");
            if (brute.deleted[i]) {
                deletions++;
            } else {
                assertEquals(withTree.mass[i].compareTo(brute.mass[i]), 0, "Object " + i + " mass");
                assertEquals(withTree.velocityX[i].compareTo(brute.velocityX[i]), 0, "Object " + i + " velocity X");
                assertEquals(withTree.positionX[i].compareTo(brute.positionX[i]), 0, "Object " + i + " position X");
                assertEquals(withTree.radius[i].compareTo(brute.radius[i]), 0, "Object " + i + " radius");
            }
        }
        assertTrue(deletions > 0, "The scene should contain merges, otherwise the test proves nothing");
    }

    @Test
    public void giantObjectIsDetectedAcrossTheScene() {
        /* The small object overlaps the giant only because of the giant's radius. If the tree
         * pruned nodes by the query object's radius alone, this collision would be missed.
         * The objects do not move towards each other, so only the de-penetration push applies. */
        double[] px = {0, 50, 300};
        double[] pr = {1, 100, 1};
        double[] pm = {1e10, 1e12, 1e10};

        DataAP brute = staticData(px, pr, pm);
        new ProcessCollisionsLogicAP(brute, false, New.num(COR)).runOnSingleThread();

        DataAP withTree = staticData(px, pr, pm);
        new ProcessCollisionsLogicAP(withTree, false, New.num(COR), New.num(THETA)).runOnSingleThread();

        assertTrue(withTree.positionX[0].compareTo(New.num(0)) < 0, "The small object must be pushed out of the giant");
        assertEquals(withTree.positionX[0].compareTo(brute.positionX[0]), 0, "Same push as brute force");
        assertEquals(withTree.positionX[1].compareTo(brute.positionX[1]), 0, "Same push of the giant");
        assertEquals(withTree.positionX[2].compareTo(New.num(300)), 0, "The far object must not move");
    }

    @Test
    public void multiObjectMergeRulesHoldWithTree() {
        /* Sandwich: the middle object is absorbed only by the most massive neighbour. */
        DataAP sandwich = staticData(new double[]{0, 1, 2}, new double[]{0.6, 0.5, 0.6}, new double[]{10, 1, 5});
        new ProcessCollisionsLogicAP(sandwich, true, New.num(COR), New.num(THETA)).runOnSingleThread();

        assertEquals(sandwich.deleted[0], false);
        assertEquals(sandwich.deleted[1], true);
        assertEquals(sandwich.deleted[2], false);
        assertEquals(sandwich.mass[0].compareTo(New.num(11)), 0, "Winner mass: " + sandwich.mass[0]);
        assertEquals(sandwich.mass[2].compareTo(New.num(5)), 0, "Untouched neighbour mass: " + sandwich.mass[2]);

        /* Chain: the middle object is dying, so it must not eat the small one. */
        DataAP chain = staticData(new double[]{2, 1, 0}, new double[]{0.6, 0.5, 0.6}, new double[]{1, 5, 10});
        new ProcessCollisionsLogicAP(chain, true, New.num(COR), New.num(THETA)).runOnSingleThread();

        assertEquals(chain.deleted[0], false);
        assertEquals(chain.deleted[1], true);
        assertEquals(chain.deleted[2], false);
        assertEquals(chain.mass[2].compareTo(New.num(15)), 0, "Big object mass: " + chain.mass[2]);
        assertEquals(chain.mass[0].compareTo(New.num(1)), 0, "Small object survives: " + chain.mass[0]);
    }

    private DataAP sceneData() {
        DataAP data = new DataAP(BODIES);
        for (int i = 0; i < BODIES; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(y[i]);
            data.velocityX[i] = New.num(vx[i]);
            data.velocityY[i] = New.num(vy[i]);
            data.mass[i] = New.num(mass[i]);
            data.radius[i] = New.num(radius[i]);
        }
        data.copyToReadOnly(true);
        return data;
    }

    private static DataAP staticData(double[] x, double[] radius, double[] mass) {
        DataAP data = new DataAP(x.length);
        for (int i = 0; i < x.length; i++) {
            data.positionX[i] = New.num(x[i]);
            data.positionY[i] = New.num(0);
            data.velocityX[i] = New.num(0);
            data.velocityY[i] = New.num(0);
            data.mass[i] = New.num(mass[i]);
            data.radius[i] = New.num(radius[i]);
        }
        data.copyToReadOnly(true);
        return data;
    }
}
