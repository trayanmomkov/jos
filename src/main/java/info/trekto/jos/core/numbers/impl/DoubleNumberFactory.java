package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public class DoubleNumberFactory implements NumberFactory {

    public Number createNumber(BigDecimal val) {
        return new DoubleNumberImpl(val);
    }

    public Number createNumber(String val) {
        return new DoubleNumberImpl(val);
    }

    public Number createNumber(double val) {
        return new DoubleNumberImpl(val);
    }

    public Number createNumber(int val) {
        return new DoubleNumberImpl(val);
    }

    public Number createNumber(long val) {
        return new DoubleNumberImpl(val);
    }

    @Override
    public Number createPi() {
        return new DoubleNumberImpl(Math.PI);
    }
}
