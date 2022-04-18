package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public class BigDecimalNumberFactory implements NumberFactory {

    private MathContext mathContext;
    private final int scale;

    public BigDecimalNumberFactory(MathContext mathContext, int scale) {
        super();
        this.mathContext = mathContext;
        this.scale = scale;
    }

    public MathContext getMathContext() {
        return mathContext;
    }

    public void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }

    @Override
    public Number createNumber(BigDecimal val) {
        return new BigDecimalNumberImpl(val);
    }

    @Override
    public Number createNumber(String val) {
        return new BigDecimalNumberImpl(val, mathContext, scale);
    }

    @Override
    public Number createNumber(double val) {
        return new BigDecimalNumberImpl(val, mathContext, scale);
    }

    @Override
    public Number createNumber(int val) {
        return new BigDecimalNumberImpl(val, mathContext, scale);
    }

    @Override
    public Number createNumber(long val) {
        return new BigDecimalNumberImpl(val, mathContext, scale);
    }
}
