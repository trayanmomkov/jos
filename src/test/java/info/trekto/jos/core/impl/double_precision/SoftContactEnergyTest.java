package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.SoftContactParameters;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.UtilsTest.createObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * The defining property of the soft-sphere (DEM) contact model: total mechanical energy
 * (kinetic + gravitational potential + spring potential) is NON-INCREASING. The spring is
 * conservative and the damper is the only sink, so a collision can dissipate energy but never
 * inject it - which is exactly what the classic impulse + de-penetration model fails to guarantee
 * (its position shove raises potential energy with no matching loss, so colliding self-gravitating
 * bodies gain energy and explode). These tests drive the move kernel on the CPU (Aparapi
 * {@code execute()} cannot run head-less) the way {@code doSoftContactIteration} does: re-snapshot,
 * then one sub-step, repeated.
 */
public class SoftContactEnergyTest {
    private static final double G = 0.000000000066743;
    private static final double MIN_DISTANCE = 1.0;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, 16);
    }

    /** A grid of particles released from rest collapses under its own gravity, collides, and settles. */
    @Test
    public void collapsingGridNeverGainsEnergy() {
        int side = 4;
        double radius = 1.0;
        double spacing = 2.5;         // small gaps, so they fall together before contact
        double mass = 1.0e8;
        List<SimulationObject> objects = new ArrayList<>();
        double offset = (side - 1) * spacing / 2.0;
        for (int gx = 0; gx < side; gx++) {
            for (int gy = 0; gy < side; gy++) {
                objects.add(createObject(gx * spacing - offset, gy * spacing - offset, 0.0, 0.0, mass, radius));
            }
        }
        double[] energies = integrate(objects, 0.3, 0.2, 2000, 100, "collapsing grid");
        assertEnergyNonIncreasing(energies, 0.02);
        assertContracted(objects);   // gravity actually pulled them together (real dynamics happened)
    }

    /** Two equal objects collide head-on with cor = 0.5: energy must drop (dissipation) and never rise. */
    @Test
    public void headOnCollisionDissipatesAndNeverGainsEnergy() {
        double radius = 1.0;
        double mass = 1.0e6;
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(-6.0, 0.0, 1.0, 0.0, mass, radius));
        objects.add(createObject(6.0, 0.0, -1.0, 0.0, mass, radius));

        double[] energies = integrate(objects, 0.5, 0.1, 1200, 60, "head-on cor=0.5");
        double e0 = energies[0];
        double eFinal = energies[energies.length - 1];
        assertEnergyNonIncreasing(energies, 0.02);
        /* cor = 0.5 head-on between equal masses leaves a quarter of the normal kinetic energy,
         * so the total must fall well below the start. */
        assertTrue(eFinal < 0.6 * e0, "Expected dissipation: eFinal=" + eFinal + " e0=" + e0);
        assertTrue(eFinal > 0, "Energy stays positive (kinetic-dominated): " + eFinal);
    }

    /** Wiring: a scene that asks for soft contact comes back with it active and a derived stiffness. */
    @Test
    public void simulationActivatesSoftContact() throws SimulationException {
        SimulationProperties properties = baseProperties(0.3, 0.2);
        properties.setSoftContact(true);
        properties.setMergeOnCollision(false);
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(-3.0, 0.0, 1.0, 0.0, 1.0e6, 1.0));
        objects.add(createObject(3.0, 0.0, -1.0, 0.0, 1.0e6, 1.0));
        properties.setNumberOfObjects(objects.size());
        properties.setInitialObjects(objects);

        SimulationDouble simulation = new SimulationDouble(properties, null);
        simulation.init(false);

        assertTrue(simulation.isSoftContactActive(), "Soft contact should be active");
        assertTrue(simulation.getSoftContactStiffness() > 0, "Stiffness should be derived");
        assertTrue(simulation.getSoftContactSubSteps() >= 1, "Sub-steps should be at least 1");
    }

    /** Wiring: soft contact is ignored while merging (merge owns the collision response). */
    @Test
    public void mergeDisablesSoftContact() throws SimulationException {
        SimulationProperties properties = baseProperties(0.3, 0.2);
        properties.setSoftContact(true);
        properties.setMergeOnCollision(true);
        List<SimulationObject> objects = new ArrayList<>();
        objects.add(createObject(-3.0, 0.0, 1.0, 0.0, 1.0e6, 1.0));
        objects.add(createObject(3.0, 0.0, -1.0, 0.0, 1.0e6, 1.0));
        properties.setNumberOfObjects(objects.size());
        properties.setInitialObjects(objects);

        SimulationDouble simulation = new SimulationDouble(properties, null);
        simulation.init(false);

        assertFalse(simulation.isSoftContactActive(), "Merge mode must not activate soft contact");
    }

    /* ------------------------------------------------------------------ helpers */

    /** Builds the scene, derives the contact parameters, and integrates it sub-step by sub-step on the
     * CPU, sampling total energy every {@code sample} steps. Returns the energy samples. */
    private double[] integrate(List<SimulationObject> objects, double cor, double secondsPerIteration,
                               int steps, int sample, String label) {
        int n = objects.size();
        DataDouble data = new DataDouble(n);
        for (int i = 0; i < n; i++) {
            SimulationObject o = objects.get(i);
            data.positionX[i] = o.getX().doubleValue();
            data.positionY[i] = o.getY().doubleValue();
            data.velocityX[i] = o.getVelocity().getX().doubleValue();
            data.velocityY[i] = o.getVelocity().getY().doubleValue();
            data.mass[i] = o.getMass().doubleValue();
            data.radius[i] = o.getRadius().doubleValue();
            data.id[i] = o.getId();
            data.color[i] = 0;
            data.deleted[i] = false;
        }
        data.copyToReadOnly(true);   // initialize every read-only array (mass, deleted, positions, ...)

        SoftContactParameters params = SoftContactParameters.derive(
                n, data.positionX, data.positionY, data.velocityX, data.velocityY, data.mass, data.radius,
                data.deleted, secondsPerIteration, cor, MIN_DISTANCE);
        assertTrue(params.stiffness > 0, "Stiffness should be derived for " + label);

        MoveObjectsLogicDouble move = new MoveObjectsLogicDouble(data, params.subStepSeconds, MIN_DISTANCE, 0, 0);
        move.enableSoftContact(params.stiffness, params.damping);

        List<Double> samples = new ArrayList<>();
        double e0 = energy(data, params.stiffness);
        samples.add(e0);
        System.out.printf("%n[%s] %s  E0=%.6e%n", label, params, e0);
        for (int step = 1; step <= steps; step++) {
            /* The two-phase sub-step the engine runs: phase A computes all accelerations, phase B
             * integrates. Two passes = the same global barrier the parallel/GPU paths use. */
            for (int i = 0; i < n; i++) {
                move.computeContactAcceleration(i);
            }
            for (int i = 0; i < n; i++) {
                move.integrateContact(i);
            }
            if (step % sample == 0) {
                double e = energy(data, params.stiffness);
                samples.add(e);
                System.out.printf("  step %5d  E=%.6e  E/E0=%+.6f%n", step, e, e / Math.abs(e0));
            }
        }
        double[] out = new double[samples.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = samples.get(i);
        }
        // write back final positions so callers can inspect dynamics
        for (int i = 0; i < n; i++) {
            objects.get(i).setX(New.num(data.positionX[i]));
            objects.get(i).setY(New.num(data.positionY[i]));
        }
        return out;
    }

    /** Total mechanical energy: kinetic + softened gravitational PE + spring PE (matching the force law). */
    private double energy(DataDouble data, double stiffness) {
        int n = data.positionX.length;
        double kinetic = 0;
        for (int i = 0; i < n; i++) {
            if (data.deleted[i]) {
                continue;
            }
            kinetic += 0.5 * data.mass[i] * (data.velocityX[i] * data.velocityX[i] + data.velocityY[i] * data.velocityY[i]);
        }
        double potential = 0;
        double spring = 0;
        for (int i = 0; i < n; i++) {
            if (data.deleted[i]) {
                continue;
            }
            for (int j = i + 1; j < n; j++) {
                if (data.deleted[j]) {
                    continue;
                }
                double dx = data.positionX[j] - data.positionX[i];
                double dy = data.positionY[j] - data.positionY[i];
                double distance = Math.sqrt(dx * dx + dy * dy);
                double softened = distance < MIN_DISTANCE ? MIN_DISTANCE : distance;
                potential -= G * data.mass[i] * data.mass[j] / softened;
                double overlap = data.radius[i] + data.radius[j] - distance;
                if (overlap > 0) {
                    double effectiveMass = data.mass[i] * data.mass[j] / (data.mass[i] + data.mass[j]);
                    spring += 0.5 * stiffness * effectiveMass * overlap * overlap;
                }
            }
        }
        return kinetic + potential + spring;
    }

    private void assertEnergyNonIncreasing(double[] energies, double relativeTolerance) {
        double e0 = energies[0];
        double tolerance = relativeTolerance * Math.abs(e0);
        for (int i = 1; i < energies.length; i++) {
            assertFalse(Double.isNaN(energies[i]), "Energy went NaN at sample " + i);
            assertTrue(energies[i] <= e0 + tolerance,
                       "Energy increased: sample " + i + " = " + energies[i] + " > E0 + tol = " + (e0 + tolerance));
        }
    }

    private void assertContracted(List<SimulationObject> objects) {
        double maxAbs = 0;
        for (SimulationObject o : objects) {
            maxAbs = Math.max(maxAbs, Math.max(Math.abs(o.getX().doubleValue()), Math.abs(o.getY().doubleValue())));
        }
        // started at half-extent 3.75 (4x4 grid, spacing 2.5); contraction means the cloud shrank
        assertTrue(maxAbs < 3.75, "Expected the grid to contract under gravity; max |coord| = " + maxAbs);
    }

    private SimulationProperties baseProperties(double cor, double secondsPerIteration) {
        SimulationProperties properties = new SimulationProperties();
        properties.setCoefficientOfRestitution(New.num(cor));
        properties.setMinDistance(New.num(MIN_DISTANCE));
        properties.setSecondsPerIteration(New.num(secondsPerIteration));
        properties.setScale(1.0);
        return properties;
    }
}
