package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactory;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public class ApfloatNumberFactory implements NumberFactory {

    final long precision;

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

    @Override
    public Number createPi() {
        return new ApfloatNumberImpl(ApfloatMath.pi(precision));
    }
}
