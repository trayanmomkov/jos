/**
 * 
 */
package info.trekto.jos.core;

import info.trekto.jos.coreI.impl.NumberBigDecimalImpl;
import info.trekto.jos.coreI.impl.NumberDoubleImpl;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author Trayan Momkov
 * @date 19 Aug 2015
 */
public class NumberFactory {

    private static MathContext mathContext = new MathContext(50);

    public static Number createNumber(BigDecimal val) {
        return new NumberDoubleImpl(val);
        // return new NumberBigDecimalImpl(val);
    }

    public static Number createNumber(String val) {
        // return new NumberDoubleImpl(val);
        return new NumberBigDecimalImpl(val, mathContext);
    }

    public static Number createNumber(double val) {
        // return new NumberDoubleImpl(val);
        return new NumberBigDecimalImpl(val, mathContext);
    }

    public static Number createNumber(int val) {
        // return new NumberDoubleImpl(val);
        return new NumberBigDecimalImpl(val, mathContext);
    }

    public static Number createNumber(long val) {
        // return new NumberDoubleImpl(val);
        return new NumberBigDecimalImpl(val, mathContext);
    }
}
