package info.trekto.jos.formulas;

import info.trekto.jos.model.ImmutableSimulationObject;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * 7 Apr 2016
 *
 */
public interface ForceCalculator {
    Number calculateForce(final ImmutableSimulationObject object1, final ImmutableSimulationObject object2, final Number distance);

    /**
     * Calculate force between objects and return it as a vector (x, y, z).
     */
    TripleNumber calculateForceAsVector(final ImmutableSimulationObject object1, final ImmutableSimulationObject object2, final Number distance);

    enum InteractingLaw {
        NEWTON_LAW_OF_GRAVITATION,
        COULOMB_LAW_ELECTRICALLY;
    }
}