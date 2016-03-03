/**
 * 
 */
package info.trekto.jos.numbers;

import info.trekto.jos.numbers.impl.DoubleNumberFactory;

import java.math.BigDecimal;

/**
 * In this class you can change the number factory and respectively type of every number in the program.
 * @author Trayan Momkov
 * @date 19 Aug 2015
 */
public class NumberFactoryProxy {

    /**
     * Here you can change number factory implementation and respectively type of every number in the program.
     */
    // private static NumberFactory factory = new BigDecimalNumberFactory();
    private static NumberFactory factory = new DoubleNumberFactory();

    public static Number createNumber(BigDecimal val) {
        return factory.createNumber(val);
    }

    public static Number createNumber(String val) {
        return factory.createNumber(val);
    }

    public static Number createNumber(double val) {
        return factory.createNumber(val);
    }

    public static Number createNumber(int val) {
        return factory.createNumber(val);
    }

    public static Number createNumber(long val) {
        return factory.createNumber(val);
    }
}
