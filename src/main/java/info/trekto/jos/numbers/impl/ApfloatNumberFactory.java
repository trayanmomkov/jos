package info.trekto.jos.numbers.impl;

import info.trekto.jos.numbers.Number;
import info.trekto.jos.numbers.NumberFactory;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public class ApfloatNumberFactory implements NumberFactory {

    private final long precision;

    public ApfloatNumberFactory(long precision) {
        super();
        this.precision = precision;
    }

    @Override
    public Number createNumber(BigDecimal val) {
        return new ApfloatNumberImpl(val, precision);
    }

    @Override
    public Number createNumber(String val) {
        return new ApfloatNumberImpl(val, precision);
    }

    @Override
    public Number createNumber(double val) {
        return new ApfloatNumberImpl(val, precision);
    }

    @Override
    public Number createNumber(int val) {
        return new ApfloatNumberImpl(val, precision);
    }

    @Override
    public Number createNumber(long val) {
        return new ApfloatNumberImpl(val, precision);
    }
}
