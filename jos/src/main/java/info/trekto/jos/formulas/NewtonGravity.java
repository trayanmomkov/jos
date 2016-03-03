/**
 * 
 */
package info.trekto.jos.formulas;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * @date 3 Mar 2016
 */
public class NewtonGravity {
    public Number caclulateForce(final SimulationObject object1, final SimulationObject object2, final Number distance) {
        return ScientificConstants.GRAVITY.multiply(object1.getMass()).multiply(object2.getMass())
                .divide(distance.pow(2));
    }
}
