package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * 10 Apr 2016
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

    @Override
    public Number createPi() {
        return new FloatNumberImpl((float)Math.PI);
    }
}
