package info.trekto.jos.core.impl.double_precision;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertTrue;

/**
 * A ring of light particles on circular orbits around a heavy planet, like the rings of Saturn.
 * The ring must keep its shape, and the inner particles must overtake the outer ones (Kepler's third law).
 * Only the move kernel is used: the particles do not collide.
 */
public class KeplerRingTest {
    private static final double G = 0.000000000066743;

    /** One orbit at the inner edge takes 1200 iterations. */
    private static final double PLANET_MASS = 2.27136e15;
    private static final double R_IN = 240.0;
    private static final double R_OUT = 480.0;
    private static final double DT = 0.05;
    private static final double MIN_DISTANCE = 3.0;
    private static final int RING_N = 192;

    /** 3 orbits at the inner edge; the outer edge manages only ~1.06 in the same time. */
    private static final int STEPS = 3600;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, 16);
    }

    @Test
    public void ringHoldsItsShapeAndShearsDifferentially() {
        int n = RING_N + 1;                      // ring + planet
        DataDouble data = new DataDouble(n);

        // Index 0 is the planet, at rest at the origin.
        set(data, 0, 0.0, 0.0, 0.0, 0.0, PLANET_MASS, 200.0);

        /* Ring particles laid out deterministically (no RNG, so a failure is reproducible):
         * radii evenly spaced across the annulus, angles spread by an irrational turn so no two
         * particles line up. Total ring mass is 1e-6 of the planet - light enough that
         * self-gravity cannot move a particle measurably. */
        double particleMass = 1e-6 * PLANET_MASS / RING_N;
        double goldenTurn = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int k = 0; k < RING_N; k++) {
            double r = R_IN + (R_OUT - R_IN) * k / (double) (RING_N - 1);
            double a = k * goldenTurn;
            double v = Math.sqrt(G * PLANET_MASS / r);
            set(data, k + 1, r * Math.cos(a), r * Math.sin(a),
                -Math.sin(a) * v, Math.cos(a) * v, particleMass, 1.5);
        }
        data.copyToReadOnly(true);               // initialize every read-only array

        double[] r0 = new double[n];
        for (int i = 0; i < n; i++) {
            r0[i] = Math.hypot(data.positionX[i], data.positionY[i]);
        }
        int inner = 1, outer = RING_N;           // the r=R_IN and r=R_OUT tracers
        double[] swept = new double[n];
        double[] lastAngle = new double[n];
        for (int i = 0; i < n; i++) {
            lastAngle[i] = Math.atan2(data.positionY[i], data.positionX[i]);
        }

        /* Track the radial excursion envelope, split into the first and last third of the run: a
         * stable ring has a bounded envelope, an unstable one has a growing one. */
        double[] earlyDrift = new double[n];
        double[] lateDrift = new double[n];
        MoveObjectsLogicDouble move = new MoveObjectsLogicDouble(data, DT, MIN_DISTANCE, 0, 0);
        for (int step = 0; step < STEPS; step++) {
            data.copyToReadOnly(false);
            move.runOnCpu();
            for (int i = 1; i < n; i++) {        // accumulate unwrapped angle for every ring particle
                double a = Math.atan2(data.positionY[i], data.positionX[i]);
                double d = a - lastAngle[i];
                if (d > Math.PI) {
                    d -= 2 * Math.PI;
                } else if (d < -Math.PI) {
                    d += 2 * Math.PI;
                }
                swept[i] += d;
                lastAngle[i] = a;
                double drift = Math.abs(Math.hypot(data.positionX[i], data.positionY[i]) - r0[i]) / r0[i];
                if (step < STEPS / 3) {
                    earlyDrift[i] = Math.max(earlyDrift[i], drift);
                } else if (step >= 2 * STEPS / 3) {
                    lateDrift[i] = Math.max(lateDrift[i], drift);
                }
            }
        }

        /* 1. Every particle kept its orbital radius. The engine integrates with semi-implicit
         * (symplectic) Euler, which is first order: a circular orbit acquires a small *bounded*
         * eccentricity of order omega*dt instead of tracking the circle exactly. So the bound to
         * assert is that physical one, per particle, not an arbitrary constant - and, more to the
         * point, that the excursion does not grow from the start of the run to the end, which is
         * what "the ring neither spreads nor collapses" actually means. */
        for (int i = 1; i < n; i++) {
            double omega = Math.sqrt(G * PLANET_MASS / (r0[i] * r0[i] * r0[i]));
            double bound = 2.0 * omega * DT;
            assertTrue(lateDrift[i] < bound,
                       "Ring particle " + i + " (r=" + r0[i] + ") swings " + lateDrift[i]
                       + " in radius, beyond the omega*dt integrator bound " + bound);
            assertTrue(lateDrift[i] < 1.5 * earlyDrift[i] + 1e-9,
                       "Ring particle " + i + " (r=" + r0[i] + ") is drifting away: excursion grew from "
                       + earlyDrift[i] + " to " + lateDrift[i]);
        }
        System.out.printf("%n[kepler ring] %d steps; radial excursion at inner edge %.3e -> %.3e "
                          + "(omega*dt = %.3e), at outer edge %.3e -> %.3e%n",
                          STEPS, earlyDrift[inner], lateDrift[inner],
                          Math.sqrt(G * PLANET_MASS / (R_IN * R_IN * R_IN)) * DT,
                          earlyDrift[outer], lateDrift[outer]);

        // 2. Kepler's third law: period scales as r^1.5, so the edges sweep very different angles.
        double innerOrbits = swept[inner] / (2 * Math.PI);
        double outerOrbits = swept[outer] / (2 * Math.PI);
        double expectedInner = STEPS * DT / (2 * Math.PI * Math.sqrt(R_IN * R_IN * R_IN / (G * PLANET_MASS)));
        double expectedOuter = STEPS * DT / (2 * Math.PI * Math.sqrt(R_OUT * R_OUT * R_OUT / (G * PLANET_MASS)));
        System.out.printf("[kepler ring] orbits completed: inner %.4f (expected %.4f), outer %.4f (expected %.4f)%n",
                          innerOrbits, expectedInner, outerOrbits, expectedOuter);
        assertTrue(Math.abs(innerOrbits - expectedInner) < 0.01,
                   "Inner edge completed " + innerOrbits + " orbits, expected " + expectedInner);
        assertTrue(Math.abs(outerOrbits - expectedOuter) < 0.01,
                   "Outer edge completed " + outerOrbits + " orbits, expected " + expectedOuter);
        assertTrue(innerOrbits > 2 * outerOrbits,
                   "The ring must shear: inner " + innerOrbits + " vs outer " + outerOrbits + " orbits");

        // 3. Both edges went the same way round (prograde); a sign flip would mean a broken setup.
        assertTrue(innerOrbits > 0 && outerOrbits > 0,
                   "Both edges should orbit prograde: " + innerOrbits + ", " + outerOrbits);
    }

    private void set(DataDouble data, int i, double x, double y, double vx, double vy, double mass, double radius) {
        data.positionX[i] = x;
        data.positionY[i] = y;
        data.velocityX[i] = vx;
        data.velocityY[i] = vy;
        data.mass[i] = mass;
        data.radius[i] = radius;
        data.id[i] = String.valueOf(i);
        data.color[i] = 0;
        data.deleted[i] = false;
    }
}
