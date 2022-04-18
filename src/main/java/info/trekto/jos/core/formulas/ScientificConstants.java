package info.trekto.jos.core.formulas;

import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

/**
 * @author Trayan Momkov
 * 3 Mar 2016
 */
public class ScientificConstants {
    public static Number GRAVITY;
    public static Number PI;
    public static final long NANOSECONDS_IN_ONE_SECOND = 1000 * 1000 * 1000;
    public static final long MILLISECONDS_IN_ONE_SECOND = 1000;
    public static final long NANOSECONDS_IN_ONE_MILLISECOND = 1000 * 1000;
    public static final long MILLI_IN_DAY = 24 * 60 * 60 * MILLISECONDS_IN_ONE_SECOND;
    public static final long MILLI_IN_HOUR = 60 * 60 * MILLISECONDS_IN_ONE_SECOND;
    public static final long MILLI_IN_MINUTE = 60 * MILLISECONDS_IN_ONE_SECOND;

    public static void setConstants() {
        GRAVITY = New.num("0.00000000006674"); // 6.674×10^−11 N⋅m2/kg2
        PI = New.num("3.1415926535897932384626433832795028841971693993751058209749445923078164062862");
    }
}