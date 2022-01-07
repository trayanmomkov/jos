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

    private MathContext mathContext;

    public BigDecimalNumberFactory(MathContext mathContext) {
        super();
        this.mathContext = mathContext;
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
        return new BigDecimalNumberImpl(val, mathContext);
    }

    @Override
    public Number createNumber(double val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }

    @Override
    public Number createNumber(int val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }

    @Override
    public Number createNumber(long val) {
        return new BigDecimalNumberImpl(val, mathContext);
    }
}
