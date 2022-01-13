package info.trekto.jos.formulas;

import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * 7 Apr 2016
 *
 */
public class CommonFormulas {
    public static Number calculateDistance(SimulationObject object1, SimulationObject object2) {
        Number x = object2.getX().subtract(object1.getX());
        Number y = object2.getY().subtract(object1.getY());
        Number z = object2.getZ().subtract(object1.getZ());
        return (x.multiply(x).add(y.multiply(y)).add(z.multiply(z))).sqrt();
    }
}
