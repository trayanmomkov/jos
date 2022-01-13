package info.trekto.jos.formulas;

import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class ScientificConstants {
    public static Number GRAVITY;
    public static Number PI;
    public static int NANOSECONDS_IN_ONE_SECOND = 1000 * 1000 * 1000;

    public static void setConstants() {
        GRAVITY = New.num("0.00000000006674"); // 6.674×10^−11 N⋅m2/kg2
        PI = New.num("3.1415926535897932384626433832795028841971693993751058209749445923078164062862");
    }
}
