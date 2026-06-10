package info.trekto.jos.core.impl.arbitrary_precision;

import info.trekto.jos.core.ProcessCollisionsLogic;
import info.trekto.jos.core.impl.double_precision.ProcessCollisionsLogicDouble;
import info.trekto.jos.core.impl.single_precision.ProcessCollisionsLogicFloat;
import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.impl.arbitrary_precision.ElasticCollision1DTest.error;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.HALF;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.MINUS_ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.TRIPLE_ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;

/**
 * The collision impulse must be applied only to objects which are moving towards each other.
 * An overlapping pair which is already separating (it bounced in a previous iteration, but the
 * overlap is not yet cleared) must not be processed again. Otherwise the impulse is applied with
 * an opposite sign, pushes the objects back into each other and they remain stuck together,
 * losing velocity on every iteration. The effect is most visible with coefficient of
 * restitution < 1 because the separation speed is only cor * approach speed, so the overlap
 * is usually not cleared in a single iteration.
 */
public class SeparatingObjectsCollisionTest {
    public static final int PRECISION = 16;
    private ImmutableSimulationObject o;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);

        SimulationObject om = new SimulationObjectImpl();
        om.setMass(ONE);
        om.setVelocity(new TripleNumber(ONE, ZERO, ZERO));
        om.setRadius(ONE);
        om.setX(HALF.negate());
        om.setY(ZERO);
        om.setZ(ZERO);
        o = om;
    }

    @DataProvider(name = "logic_implementations")
    public static Object[][] logicImplementations() {
        return new Object[][]{
                {new ProcessCollisionsLogicAP(2, false, ONE)},
                {new ProcessCollisionsLogicDouble(2, false, 1)},
                {new ProcessCollisionsLogicFloat(2, false, 1)}
        };
    }

    @Test(dataProvider = "logic_implementations")
    public void separatingObjectsAreNotAffected(ProcessCollisionsLogic simulationLogic) {
        /* Overlapping, but moving away from each other - the state just after a bounce. */
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setVelocity(new TripleNumber(MINUS_ONE, ZERO, ZERO));
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(HALF);
        o2.setVelocity(new TripleNumber(ONE, ZERO, ZERO));

        simulationLogic.processElasticCollisionObjects(o1, o2, HALF);

        assertEquals(o1.getVelocity(), new TripleNumber(MINUS_ONE, ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getVelocity(), new TripleNumber(ONE, ZERO, ZERO), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void bouncedObjectsDoNotCollideAgainWhileOverlapping(ProcessCollisionsLogic simulationLogic) {
        /* Head-on, equal masses, cor = 0.5. */
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(HALF);
        o2.setVelocity(new TripleNumber(MINUS_ONE, ZERO, ZERO));

        /* First processing: the objects collide and bounce. */
        simulationLogic.processElasticCollisionObjects(o1, o2, HALF);

        assertEquals(o1.getVelocity(), new TripleNumber(HALF.negate(), ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getVelocity(), new TripleNumber(HALF, ZERO, ZERO), error(simulationLogic));

        /* Second processing of the same, still overlapping pair: the objects are separating now,
         * so the velocities must not change. Without the direction check the impulse is applied
         * again, the velocities are flipped towards each other and the objects get stuck. */
        simulationLogic.processElasticCollisionObjects(o1, o2, HALF);

        assertEquals(o1.getVelocity(), new TripleNumber(HALF.negate(), ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getVelocity(), new TripleNumber(HALF, ZERO, ZERO), error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void perfectlyInelasticHeadOnCollision(ProcessCollisionsLogic simulationLogic) {
        /* cor = 0: both objects continue together with the velocity of the common mass centre. */
        SimulationObject o1 = new SimulationObjectImpl(o);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(HALF);
        o2.setVelocity(new TripleNumber(MINUS_ONE, ZERO, ZERO));

        simulationLogic.processElasticCollisionObjects(o1, o2, ZERO);

        assertEquals(o1.getVelocity(), TRIPLE_ZERO, error(simulationLogic));
        assertEquals(o2.getVelocity(), TRIPLE_ZERO, error(simulationLogic));

        /* Processing the resting pair again must not change anything. */
        simulationLogic.processElasticCollisionObjects(o1, o2, ZERO);

        assertEquals(o1.getVelocity(), TRIPLE_ZERO, error(simulationLogic));
        assertEquals(o2.getVelocity(), TRIPLE_ZERO, error(simulationLogic));
    }

    @Test(dataProvider = "logic_implementations")
    public void coincidingCentersDoNotCorruptVelocities(ProcessCollisionsLogic simulationLogic) {
        /* Objects whose centres coincide have no defined collision direction. They must be left
         * untouched: zero distance leads to division by zero (NaN velocities in double/float,
         * ArithmeticException in arbitrary precision). */
        SimulationObject o1 = new SimulationObjectImpl(o);
        o1.setX(ZERO);
        SimulationObject o2 = new SimulationObjectImpl(o);
        o2.setX(ZERO);
        o2.setVelocity(new TripleNumber(MINUS_ONE, ZERO, ZERO));

        simulationLogic.processElasticCollisionObjects(o1, o2, HALF);

        assertEquals(o1.getVelocity(), new TripleNumber(ONE, ZERO, ZERO), error(simulationLogic));
        assertEquals(o2.getVelocity(), new TripleNumber(MINUS_ONE, ZERO, ZERO), error(simulationLogic));
    }
}
