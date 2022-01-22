package info.trekto.jos.formulas;

import info.trekto.jos.model.ImmutableSimulationObject;

import static info.trekto.jos.core.impl.SimulationImpl.RATIO_FOUR_THREE;
import static info.trekto.jos.formulas.ScientificConstants.PI;

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
}
