package info.trekto.jos.core.impl;

import info.trekto.jos.core.Number;
import info.trekto.jos.core.NumberFactory;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * @date 7 Dec 2015
 */
public class DoubleNumberFactory implements NumberFactory {

    public Number createNumber(BigDecimal val) {
        return new NumberDoubleImpl(val);
    }

    public Number createNumber(String val) {
        return new NumberDoubleImpl(val);
    }

    public Number createNumber(double val) {
        return new NumberDoubleImpl(val);
    }

    public Number createNumber(int val) {
        return new NumberDoubleImpl(val);
    }

    public Number createNumber(long val) {
        return new NumberDoubleImpl(val);
    }
}
