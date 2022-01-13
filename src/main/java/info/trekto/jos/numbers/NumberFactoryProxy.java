package info.trekto.jos.numbers;

import info.trekto.jos.formulas.ScientificConstants;
import info.trekto.jos.numbers.impl.BigDecimalNumberFactory;
import info.trekto.jos.numbers.impl.DoubleNumberFactory;
import info.trekto.jos.numbers.impl.FloatNumberFactory;

import java.math.BigDecimal;
import java.math.MathContext;

import static info.trekto.jos.numbers.impl.BigDecimalNumberImpl.roundingMode;

/**
 * In this class you can change the number factory and respectively type of every number in the program.
 * @author Trayan Momkov
 * 19 Aug 2015
 */
public class NumberFactoryProxy {

    /**
     * Here you can change number factory implementation and respectively type of every number in the program.
     */
    // private static NumberFactory factory = new BigDecimalNumberFactory();
    private static NumberFactory factory;

    private static void calculateConstants() {
        New.ZERO = NumberFactoryProxy.createNumber("0");
        New.ONE = NumberFactoryProxy.createNumber("1");
        New.TWO = NumberFactoryProxy.createNumber("2");
        New.THREE = NumberFactoryProxy.createNumber("3");
        New.FOUR = NumberFactoryProxy.createNumber("4");
        New.RATIO_FOUR_THREE = New.FOUR.divide(New.THREE);
        New.BILLION = NumberFactoryProxy.createNumber("1000000000");
    }

    public static NumberFactory getFactory() {
        return factory;
    }

    public static void setFactory(NumberFactory factory) {
        NumberFactoryProxy.factory = factory;
        calculateConstants();
        ScientificConstants.setConstants();
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
                setFactory(new BigDecimalNumberFactory(new MathContext(precision, roundingMode), scale));
                break;
            default:
                setFactory(new DoubleNumberFactory());
                break;
        }
    }
}
