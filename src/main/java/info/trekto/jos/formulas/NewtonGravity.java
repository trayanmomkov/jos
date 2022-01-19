package info.trekto.jos.formulas;

import static info.trekto.jos.formulas.ScientificConstants.GRAVITY;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class NewtonGravity {
    public static double calculateForce(final double object1Mass, final double object2Mass, final double distance) {
        return GRAVITY * object1Mass * object2Mass / (distance * distance);
    }
}
