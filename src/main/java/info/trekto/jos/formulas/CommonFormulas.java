package info.trekto.jos.formulas;

import info.trekto.jos.model.ImmutableSimulationObject;
import info.trekto.jos.model.impl.TripleNumber;

import static info.trekto.jos.formulas.ScientificConstants.PI;
import static info.trekto.jos.numbers.New.RATIO_FOUR_THREE;

/**
 * @author Trayan Momkov
 * 7 Apr 2016
 *
 */
public class CommonFormulas {
    public static double calculateDistance(ImmutableSimulationObject object1, ImmutableSimulationObject object2) {
        double x = object2.getX() - object1.getX();
        double y = object2.getY() - object1.getY();
        double z = object2.getZ() - object1.getZ();
        return Math.sqrt(x*x+y*y+z*z);
    }
    
    public static double calculateVolumeFromRadius(double radius) {
        // V = 4/3 * pi * r^3
        if (radius == 0) {
            return Double.MIN_VALUE;
        }
        return RATIO_FOUR_THREE * PI * Math.pow(radius, 3);
    }
    
    public static double calculateRadiusFromVolume(double volume) {
        // V = 4/3 * pi * r^3
        if (volume == 0) {
            return Double.MIN_VALUE;
        }
        return Math.cbrt(volume / (RATIO_FOUR_THREE * PI));
    }

    public static TripleNumber calculateAcceleration(ImmutableSimulationObject object, TripleNumber acceleration, TripleNumber force) {
        // ax = Fx / m
        double newAccelerationX = acceleration.getX() + force.getX() / object.getMass();
        double newAccelerationY = acceleration.getY() + force.getY() / object.getMass();
        double newAccelerationZ = acceleration.getZ() + force.getZ() / object.getMass();
        return new TripleNumber(newAccelerationX, newAccelerationY, newAccelerationZ);
    }
}
