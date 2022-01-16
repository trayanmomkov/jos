package info.trekto.jos.formulas;

import info.trekto.jos.model.ImmutableSimulationObject;
import info.trekto.jos.model.impl.TripleNumber;

import static info.trekto.jos.formulas.ScientificConstants.GRAVITY;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class NewtonGravity implements ForceCalculator {
    @Override
    public double calculateForce(final ImmutableSimulationObject object1, final ImmutableSimulationObject object2, final double distance) {
        //        (GRAVITY * object1.mass() * object2.mass()) / (distance * distance);
        return GRAVITY * object1.getMass() * object2.getMass() / (distance * distance);
    }

    @Override
    public TripleNumber calculateForceAsVector(ImmutableSimulationObject object1, ImmutableSimulationObject object2, double distance) {
        double force = calculateForce(object1, object2, distance);
        //       Fx = F*x/r;
        double forceX = force * (object2.getX() - object1.getX()) / distance;
        double forceY = force * (object2.getY() - object1.getY()) / distance;
        double forceZ = force * (object2.getZ() - object1.getZ()) / distance;
        return new TripleNumber(forceX, forceY, forceZ);
    }
}
