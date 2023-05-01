package info.trekto.jos.util;

import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.impl.single_precision.SimulationFloat;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import java.util.Arrays;
import java.util.UUID;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;

public class UtilsTest {
    public static SimulationAP createAPSimulation(SimulationObject... objects) throws SimulationException {
        SimulationProperties properties = new SimulationProperties();
        properties.setCoefficientOfRestitution(ZERO);
        properties.setSecondsPerIteration(ONE);
        properties.setNumberOfObjects(objects.length);
        properties.setInitialObjects(Arrays.asList(objects));
        SimulationAP simulation = new SimulationAP(properties);
        simulation.init(false);
        return simulation;
    }
    
    public static SimulationDouble createDoubleSimulation(SimulationObject... objects) throws SimulationException {
        SimulationProperties properties = new SimulationProperties();
        properties.setCoefficientOfRestitution(ZERO);
        properties.setSecondsPerIteration(ONE);
        properties.setNumberOfObjects(objects.length);
        properties.setInitialObjects(Arrays.asList(objects));
        SimulationDouble simulation = new SimulationDouble(properties, null);
        simulation.init(false);
        return simulation;
    }
    
    public static SimulationFloat createFloatSimulation(SimulationObject... objects) throws SimulationException {
        SimulationProperties properties = new SimulationProperties();
        properties.setCoefficientOfRestitution(ZERO);
        properties.setSecondsPerIteration(ONE);
        properties.setNumberOfObjects(objects.length);
        properties.setInitialObjects(Arrays.asList(objects));
        SimulationFloat simulation = new SimulationFloat(properties, null);
        simulation.init(false);
        return simulation;
    }

    public static SimulationObject createObject(Number x, Number y, Number mass, Number radius) {
        SimulationObject o = new SimulationObjectImpl();
        o.setMass(mass);
        o.setId(UUID.randomUUID().toString());
        o.setX(x);
        o.setY(y);
        o.setRadius(radius);
        return o;
    }

    public static SimulationObject createObject(Number x, Number y, Number velocityX, Number velocityY, Number mass, Number radius) {
        SimulationObject o = new SimulationObjectImpl();
        o.setMass(mass);
        o.setId(UUID.randomUUID().toString());
        o.setX(x);
        o.setY(y);
        o.setVelocity(new TripleNumber(velocityX, velocityY, ZERO));
        o.setRadius(radius);
        return o;
    }

    public static SimulationObject createObject(double x, double y, double mass, double radius) {
        SimulationObject o = new SimulationObjectImpl();
        o.setMass(New.num(mass));
        o.setId(UUID.randomUUID().toString());
        o.setX(New.num(x));
        o.setY(New.num(y));
        o.setRadius(New.num(radius));
        return o;
    }

    public static SimulationObject createObject(double x, double y, double velocityX, double velocityY, double mass, double radius) {
        SimulationObject o = new SimulationObjectImpl();
        o.setMass(New.num(mass));
        o.setId(UUID.randomUUID().toString());
        o.setX(New.num(x));
        o.setY(New.num(y));
        o.setVelocity(new TripleNumber(New.num(velocityX), New.num(velocityY), ZERO));
        o.setRadius(New.num(radius));
        return o;
    }

    public static SimulationObject createObject(float x, float y, float mass, float radius) {
        SimulationObject o = new SimulationObjectImpl();
        o.setMass(New.num(mass));
        o.setId(UUID.randomUUID().toString());
        o.setX(New.num(x));
        o.setY(New.num(y));
        o.setRadius(New.num(radius));
        return o;
    }

    public static SimulationObject createObject(float x, float y, float velocityX, float velocityY, float mass, float radius) {
        SimulationObject o = new SimulationObjectImpl();
        o.setMass(New.num(mass));
        o.setId(UUID.randomUUID().toString());
        o.setX(New.num(x));
        o.setY(New.num(y));
        o.setVelocity(new TripleNumber(New.num(velocityX), New.num(velocityY), ZERO));
        o.setRadius(New.num(radius));
        return o;
    }
}