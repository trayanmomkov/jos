package info.trekto.jos.numbers.impl;

import java.math.BigDecimal;

import info.trekto.jos.numbers.Number;
import info.trekto.jos.numbers.NumberFactory;

/**
 * @author Trayan Momkov
 * @date 10 Apr 2016
 */
public class FloatNumberFactory implements NumberFactory {

    @Override
    public Number createNumber(BigDecimal val) {
        return new FloatNumberImpl(val);
    }

    @Override
    public Number createNumber(String val) {
        return new FloatNumberImpl(val);
    }

    @Override
    public Number createNumber(double val) {
        return new FloatNumberImpl((float) val);
    }

    @Override
    public Number createNumber(int val) {
        return new FloatNumberImpl(val);
    }

    @Override
    public Number createNumber(long val) {
        return new FloatNumberImpl(val);
    }
}