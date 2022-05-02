package info.trekto.jos.core.numbers;

import info.trekto.jos.core.numbers.impl.*;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * In this class you can change the number factory and respectively type of every number in the program.
 *
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public class NumberFactoryProxy {

    public static Number ZERO;
    public static Number ONE;
    public static Number TWO;
    public static Number THREE;
    public static Number FOUR;
    public static Number RATIO_FOUR_THREE;
    public static Number BILLION;
    public static Number IGNORED;
    public static Number PI;
    /**
     * Here you can change number factory implementation and respectively type of every number in the program.
     */
    // private static NumberFactory factory = new BigDecimalNumberFactory();
    private static NumberFactory factory;

    private static void calculateConstants() {
        ZERO = createNumber("0");
        ONE = createNumber("1");
        TWO = createNumber("2");
        THREE = createNumber("3");
        FOUR = createNumber("4");
        RATIO_FOUR_THREE = FOUR.divide(THREE);
        BILLION = createNumber("1000000000");
        IGNORED = createNumber("0");
        PI = factory.createPi();
    }

    public static NumberFactory getFactory() {
        return factory;
    }

    public static void setFactory(NumberFactory factory) {
        NumberFactoryProxy.factory = factory;
        calculateConstants();
    }

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

    public static void createNumberFactory(NumberFactory.NumberType numberType, int precision, int scale) {
        switch (numberType) {
            case FLOAT:
                setFactory(new FloatNumberFactory());
                break;
            case DOUBLE:
                setFactory(new DoubleNumberFactory());
                break;
            case BIG_DECIMAL:
                setFactory(new BigDecimalNumberFactory(new MathContext(precision, BigDecimalNumberImpl.roundingMode), scale));
                break;
            case APFLOAT:
                setFactory(new ApfloatNumberFactory(precision));
                break;
            default:
                setFactory(new DoubleNumberFactory());
                break;
        }
    }
}
