/**
 *
 */
package info.trekto.jos.formulas;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class NewtonGravity implements ForceCalculator {
    @Override
    public Number caclulateForce(final SimulationObject object1, final SimulationObject object2,
                                 final Number distance) {
        //        (GRAVITY * object1.mass() * object2.mass()) / (distance * distance);
        return ScientificConstants.GRAVITY.multiply(object1.getMass()).multiply(object2.getMass())
                .divide(distance.multiply(distance));
    }

    @Override
    public TripleNumber caclulateForceAsVector(SimulationObject object1, SimulationObject object2, Number distance) {
        Number force = caclulateForce(object1, object2, distance);
        //       Fx = F*x/r;
        Number forceX = force.multiply(object2.getX().subtract(object1.getX())).divide(distance);
        Number forceY = force.multiply(object2.getY().subtract(object1.getY())).divide(distance);
        Number forceZ = force.multiply(object2.getZ().subtract(object1.getZ())).divide(distance);
        return new TripleNumber(forceX, forceY, forceZ);
    }
}
