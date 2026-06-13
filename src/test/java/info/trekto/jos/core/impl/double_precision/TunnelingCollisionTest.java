package info.trekto.jos.core.impl.double_precision;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertTrue;

/**
 * Fast-object collision handling.
 *
 * <p>The elastic approach test uses the positions from the START of the iteration (before the
 * move), so a fast pair that crosses centers within a single step still registers as approaching
 * and bounces (see {@link ProcessCollisionsLogicDouble}). This fixes the "partial tunnel", where
 * the pair ends the step overlapping but with crossed centers - the post-move relative position
 * would have flipped sign and the old guard skipped the collision.
 *
 * <p>A FULL tunnel - the step so large the pair never overlaps on any frame - cannot be caught by
 * any discrete overlap test. It stays a documented limitation: the simulations log a one-time
 * warning when an object moves more than its radius in a step, and the README advises reducing the
 * time per iteration. {@link #fullTunnelStillPassesThrough_documentsResidualLimitation()} pins it.
 */
public class TunnelingCollisionTest {
    private static final double DT = 1;
    private static final double MIN_DISTANCE = 1;
    private static final double COR = 1;

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, 16);
    }

    /**
     * Object 0 (x=0, +6/iteration) overtakes object 1 (x=5, at rest) in one dt=1 step: the centers
     * cross (post-move x0=6 &gt; x1=5) while overlapping (distance 1 &lt; 2). With the approach test in
     * the pre-move frame the impulse is applied and the equal-mass pair exchanges velocities.
     */
    @Test
    public void partialTunnelBouncesViaThePreMoveFrame() {
        DataDouble data = scene(6);

        data.copyToReadOnly(false);
        double[] beforeMoveX = data.positionX.clone();
        double[] beforeMoveY = data.positionY.clone();
        new MoveObjectsLogicDouble(data, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        double distanceAfterMove = Math.abs(data.positionX[0] - data.positionX[1]);
        assertTrue(distanceAfterMove < data.radius[0] + data.radius[1], "Precondition: overlapping after the move");
        assertTrue(data.positionX[0] > data.positionX[1], "Precondition: centers have crossed");

        data.copyToReadOnly(false);
        new ProcessCollisionsLogicDouble(data, false, COR, beforeMoveX, beforeMoveY).runOnSingleThread();

        assertTrue(data.velocityX[1] > 5.9, "Object 1 receives the momentum (velocities exchange)");
        assertTrue(data.velocityX[0] < 0.1, "Object 0 gives up its velocity");
    }

    /**
     * The same crossing, but the collision is given the post-move positions for the approach test
     * (the default 3-arg constructor aliases the pre-move snapshot to the post-move one). This
     * reproduces the old behaviour and shows why the pre-move frame is necessary: the pair tunnels.
     */
    @Test
    public void partialTunnelIsMissedWhenTheGuardUsesPostMovePositions() {
        DataDouble data = scene(6);

        data.copyToReadOnly(false);
        new MoveObjectsLogicDouble(data, DT, MIN_DISTANCE, 0, 0).runOnCpu();
        data.copyToReadOnly(false);
        new ProcessCollisionsLogicDouble(data, false, COR).runOnSingleThread();

        assertTrue(data.velocityX[0] > 5.9, "Post-move frame: the pair tunnels, object 0 keeps its velocity");
        assertTrue(data.velocityX[1] < 0.1, "Object 1 is never hit");
    }

    /**
     * A FULL tunnel: object 0 is so fast (+20/iteration) that after the move the pair does not
     * overlap on any frame (x0=20, x1=5). No discrete overlap test can catch this; it remains a
     * documented limitation, mitigated by the fast-motion warning and a smaller time per iteration.
     */
    @Test
    public void fullTunnelStillPassesThrough_documentsResidualLimitation() {
        DataDouble data = scene(20);

        data.copyToReadOnly(false);
        double[] beforeMoveX = data.positionX.clone();
        double[] beforeMoveY = data.positionY.clone();
        new MoveObjectsLogicDouble(data, DT, MIN_DISTANCE, 0, 0).runOnCpu();

        double distanceAfterMove = Math.abs(data.positionX[0] - data.positionX[1]);
        assertTrue(distanceAfterMove > data.radius[0] + data.radius[1], "No overlapping frame exists");

        data.copyToReadOnly(false);
        new ProcessCollisionsLogicDouble(data, false, COR, beforeMoveX, beforeMoveY).runOnSingleThread();

        assertTrue(data.velocityX[0] > 19.9, "Full tunnel: object 0 passes through unchanged");
        assertTrue(data.velocityX[1] < 0.1, "Object 1 is never hit");
    }

    private static DataDouble scene(double velocityOfFirst) {
        DataDouble data = new DataDouble(2);
        data.positionX[0] = 0;
        data.velocityX[0] = velocityOfFirst;
        data.mass[0] = 1;
        data.radius[0] = 1;

        data.positionX[1] = 5;
        data.velocityX[1] = 0;
        data.mass[1] = 1;
        data.radius[1] = 1;
        /* The mass snapshot is filled only once, like in initArrays(). */
        System.arraycopy(data.mass, 0, data.readOnlyMass, 0, data.n);
        return data;
    }
}
