/**
 *
 */
package info.trekto.jos.formulas;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * @date 7 Apr 2016
 *
 */
public interface ForceCalculator {
    Number caclulateForce(final SimulationObject object1, final SimulationObject object2,
                          final Number distance);

    /**
     * Calculate force between objects and return it as a vector (x, y, z).
     * @param object1
     * @param object2
     * @param distance
     * @return
     */
    TripleNumber caclulateForceAsVector(final SimulationObject object1, final SimulationObject object2,
                                        final Number distance);

    public enum ForceCalculatorType {
        NEWTON_LAW_OF_GRAVITATION,
        COULOMB_LAW_ELECTRICALLY;
    }
}
