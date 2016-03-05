package info.trekto.jos.numbers.impl;

import info.trekto.jos.numbers.Number;
import info.trekto.jos.numbers.NumberFactory;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author Trayan Momkov
 * @date 7 Dec 2015
 */
public class BigDecimalNumberFactory implements NumberFactory {
    private static MathContext mathContext = new MathContext(50);

    public Number createNumber(BigDecimal val) {
        return new BigDecimalNumberImpl(val);
    }

    public Number createNumber(String val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }

    public Number createNumber(double val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }

    public Number createNumber(int val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }

    public Number createNumber(long val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }
}
