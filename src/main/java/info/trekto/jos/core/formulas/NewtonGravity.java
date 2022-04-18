package info.trekto.jos.core.formulas;

import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.formulas.ScientificConstants.GRAVITY;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class NewtonGravity implements ForceCalculator {
    @Override
    public Number calculateForce(final ImmutableSimulationObject object1, final ImmutableSimulationObject object2, final Number distance) {
        //        (GRAVITY * object1.mass() * object2.mass()) / (distance * distance);
        return GRAVITY.multiply(object1.getMass()).multiply(object2.getMass())
                .divide(distance.multiply(distance));
    }

    @Override
    public TripleNumber calculateForceAsVector(ImmutableSimulationObject object1, ImmutableSimulationObject object2, Number distance) {
        Number force = calculateForce(object1, object2, distance);
        //       Fx = F*x/r;
        Number forceX = force.multiply(object2.getX().subtract(object1.getX())).divide(distance);
        Number forceY = force.multiply(object2.getY().subtract(object1.getY())).divide(distance);
        Number forceZ = force.multiply(object2.getZ().subtract(object1.getZ())).divide(distance);
        return new TripleNumber(forceX, forceY, forceZ);
    }
}
