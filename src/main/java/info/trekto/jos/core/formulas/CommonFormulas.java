package info.trekto.jos.core.formulas;

import info.trekto.jos.core.model.ImmutableSimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.Number;

import static info.trekto.jos.core.numbers.New.IGNORED;
import static info.trekto.jos.core.numbers.New.RATIO_FOUR_THREE;

/**
 * @author Trayan Momkov
 * 7 Apr 2016
 *
 */
public class CommonFormulas {
    public static Number calculateDistance(ImmutableSimulationObject object1, ImmutableSimulationObject object2) {
        Number x = object2.getX().subtract(object1.getX());
        Number y = object2.getY().subtract(object1.getY());
        Number z = object2.getZ().subtract(object1.getZ());
        return (x.multiply(x).add(y.multiply(y)).add(z.multiply(z))).sqrt();
    }
    
    public static Number calculateVolumeFromRadius(Number radius) {
        // V = 4/3 * pi * r^3
        return RATIO_FOUR_THREE.multiply(ScientificConstants.PI).multiply(radius.pow(3));
    }
    
    public static Number calculateRadiusFromVolume(Number volume) {
        // V = 4/3 * pi * r^3
        return IGNORED.cbrt(volume.divide(RATIO_FOUR_THREE.multiply(ScientificConstants.PI)));
    }

    public static TripleNumber calculateAcceleration(ImmutableSimulationObject object, TripleNumber acceleration, TripleNumber force) {
        // ax = Fx / m
        Number newAccelerationX = acceleration.getX().add(force.getX().divide(object.getMass()));
        Number newAccelerationY = acceleration.getY().add(force.getY().divide(object.getMass()));
        Number newAccelerationZ = acceleration.getZ().add(force.getZ().divide(object.getMass()));
        return new TripleNumber(newAccelerationX, newAccelerationY, newAccelerationZ);
    }
}
