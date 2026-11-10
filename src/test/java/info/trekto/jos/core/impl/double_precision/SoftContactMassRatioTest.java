package info.trekto.jos.core.impl.double_precision;

import info.trekto.jos.core.impl.SoftContactParameters;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * The spring of a contact is proportional to the masses of the pair,
 * so the soft contact must work for light objects next to heavy ones.
 */
public class SoftContactMassRatioTest {
    private static final double G = 0.000000000066743;
    private static final double MIN_DISTANCE = 1.0;
    private static final double COR = 0.5;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, 16);
    }

    /** A planet, a moon on an orbit and three particles, 5e-11 of the planet each, which fall on the planet. */
    @Test
    public void lightParticlesComeToRestOnAHeavyBody() {
        double planetMass = 2e15;
        double planetRadius = 100;
        double particleRadius = 1.5;
        double start = planetRadius + particleRadius + 0.1;
        DataDouble data = new DataDouble(5);
        set(data, 0, 0, 0, 0, 0, planetMass, planetRadius);
        set(data, 1, 300, 0, 0, Math.sqrt(G * planetMass / 300), 6e10, 5);
        set(data, 2, 0, start, 0, 0, 1e5, particleRadius);
        set(data, 3, -start, 0, 0, 0, 1e5, particleRadius);
        set(data, 4, 0, -start, 0, 0, 1e5, particleRadius);

        SoftContactParameters parameters = derive(data, 0.05);
        double fastest = 0;
        for (int iteration = 0; iteration < 200; iteration++) {
            move(data, parameters, 1);
            for (int i = 2; i < 5; i++) {
                fastest = Math.max(fastest, speed(data, i));
            }
        }

        /* They fall 0.1 with about 13 m/s^2, so they hit the planet with 1.6 m/s. */
        assertTrue(fastest < 2, "A particle was thrown away with speed " + fastest);
        for (int i = 2; i < 5; i++) {
            double height = Math.hypot(data.positionX[i] - data.positionX[0], data.positionY[i] - data.positionY[0]);
            assertEquals(height, planetRadius + particleRadius, 0.01, "Particle " + i + " does not lie on the planet");
            assertTrue(speed(data, i) < 0.01, "Particle " + i + " still moves with speed " + speed(data, i));
        }
    }

    /** A light object hits a 1000 times heavier one. */
    @Test
    public void unequalMassesBounceWithTheWantedRestitution() {
        DataDouble data = new DataDouble(2);
        set(data, 0, -3, 0, 2, 0, 1, 1);
        set(data, 1, 3, 0, 0, 0, 1000, 1);

        move(data, derive(data, 0.01), 600);

        double speedOfSeparation = data.velocityX[1] - data.velocityX[0];
        assertTrue(speedOfSeparation > 0, "The objects did not bounce");
        assertEquals(speedOfSeparation / 2, COR, 0.05, "Coefficient of restitution");
        assertEquals(data.velocityX[0] + 1000 * data.velocityX[1], 2.0, 1e-9, "Momentum");
    }

    /** Ten moving objects: the same scene with equal masses and with every second object 1e12 times lighter. */
    @Test
    public void subStepsDoNotDependOnTheMasses() {
        SoftContactParameters equal = derive(tenMovingObjects(1e6), 0.05);
        SoftContactParameters mixed = derive(tenMovingObjects(1e-6), 0.05);

        assertTrue(equal.subSteps > 1, "The scene must need small steps");
        assertEquals(mixed.subSteps, equal.subSteps);
        assertEquals(mixed.stiffness, equal.stiffness, 1e-9 * equal.stiffness);
    }

    /** Matter can fall on a heavy object later. The spring must be ready to carry a pile of it there. */
    @Test
    public void theSpringCarriesAPileOnTheSurfaceOfAHeavyObject() {
        double heavyMass = 6e14;
        double heavyRadius = 8;
        DataDouble data = tenMovingObjects(1e6);
        set(data, 9, 5000, 0, 0, 0, heavyMass, heavyRadius);

        SoftContactParameters parameters = derive(data, 0.05);

        double surfaceGravity = G * heavyMass / Math.pow(heavyRadius + 1, 2);
        double sinking = 2 * surfaceGravity * Math.sqrt(data.n) / parameters.stiffness;
        assertTrue(parameters.gravityLimited, "The gravity on the surface of the heavy object must set the stiffness");
        assertEquals(sinking, 0.05, 1e-9, "A pile on the heavy object sinks by 5 % of the radius of the objects");
    }

    /* ------------------------------------------------------------------ helpers */

    private static DataDouble tenMovingObjects(double massOfEverySecond) {
        DataDouble data = new DataDouble(10);
        for (int i = 0; i < 10; i++) {
            set(data, i, 3 * i, 0, 0, i % 3, i % 2 == 0 ? 1e6 : massOfEverySecond, 1);
        }
        return data;
    }

    private static void set(DataDouble data, int i, double x, double y, double velocityX, double velocityY,
                            double mass, double radius) {
        data.positionX[i] = x;
        data.positionY[i] = y;
        data.velocityX[i] = velocityX;
        data.velocityY[i] = velocityY;
        data.mass[i] = mass;
        data.radius[i] = radius;
        data.id[i] = String.valueOf(i);
        data.deleted[i] = false;
    }

    private static SoftContactParameters derive(DataDouble data, double secondsPerIteration) {
        data.copyToReadOnly(true);
        return SoftContactParameters.derive(data.n, data.positionX, data.positionY, data.velocityX, data.velocityY,
                                            data.mass, data.radius, data.deleted, secondsPerIteration, COR, MIN_DISTANCE);
    }

    /* The small steps of the given number of iterations, as SimulationDouble makes them. */
    private static void move(DataDouble data, SoftContactParameters parameters, int iterations) {
        MoveObjectsLogicDouble logic = new MoveObjectsLogicDouble(data, parameters.subStepSeconds, MIN_DISTANCE, 0, 0);
        logic.enableSoftContact(parameters.stiffness, parameters.damping);
        for (int step = 0; step < iterations * parameters.subSteps; step++) {
            for (int i = 0; i < data.n; i++) {
                logic.computeContactAcceleration(i);
            }
            for (int i = 0; i < data.n; i++) {
                logic.integrateContact(i);
            }
        }
    }

    private static double speed(DataDouble data, int i) {
        return Math.hypot(data.velocityX[i], data.velocityY[i]);
    }
}
