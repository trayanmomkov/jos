/**
 *
 */
package info.trekto.jos.numbers.impl;

import info.trekto.jos.numbers.Number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * BigDecimal implementation.
 * Immutable.
 * @author Trayan Momkov
 * @date 18 Aug 2015
 */
public class BigDecimalNumberImpl implements Number {

    private final BigDecimal value;
    private final RoundingMode roundingMode = RoundingMode.HALF_EVEN;

    /**
     * @param value
     */
    public BigDecimalNumberImpl(BigDecimal value) {
        this.value = value;
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in, int offset, int len)
     */
    public BigDecimalNumberImpl(char[] in, int offset, int len) {
        value = new BigDecimal(in, offset, len);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in, int offset, int len, MathContext mc)
     */
    public BigDecimalNumberImpl(char[] in, int offset, int len, MathContext mc) {
        value = new BigDecimal(in, offset, len, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in)
     */
    public BigDecimalNumberImpl(char[] in) {
        value = new BigDecimal(in);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in, MathContext mc)
     */
    public BigDecimalNumberImpl(char[] in, MathContext mc) {
        value = new BigDecimal(in, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(String val)
     */
    public BigDecimalNumberImpl(String val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(String val, MathContext mc)
     */
    public BigDecimalNumberImpl(String val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(double val)
     */
    public BigDecimalNumberImpl(double val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(double val, MathContext mc)
     */
    public BigDecimalNumberImpl(double val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger val)
     */
    public BigDecimalNumberImpl(BigInteger val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger val, MathContext mc)
     */
    public BigDecimalNumberImpl(BigInteger val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger unscaledVal, int scale)
     */
    public BigDecimalNumberImpl(BigInteger unscaledVal, int scale) {
        value = new BigDecimal(unscaledVal, scale);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger unscaledVal, int scale, MathContext mc)
     */
    public BigDecimalNumberImpl(BigInteger unscaledVal, int scale, MathContext mc) {
        value = new BigDecimal(unscaledVal, scale, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(int val)
     */
    public BigDecimalNumberImpl(int val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(int val, MathContext mc)
     */
    public BigDecimalNumberImpl(int val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(long val)
     */
    public BigDecimalNumberImpl(long val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(long val, MathContext mc)
     */
    public BigDecimalNumberImpl(long val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }


    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#add(java.math.BigDecimal)
     */
    @Override
    public Number add(Number augend) {
        return newNumber(value.add(((BigDecimalNumberImpl) augend).value));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#subtract(java.math.BigDecimal)
     */
    @Override
    public Number subtract(Number subtrahend) {
        return newNumber(value.subtract(((BigDecimalNumberImpl) subtrahend).value));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#multiply(java.math.BigDecimal)
     */
    @Override
    public Number multiply(Number multiplicand) {
        return newNumber(value.multiply(((BigDecimalNumberImpl) multiplicand).value));
        // return new NumberBigDecimalImpl((value.multiply(((NumberBigDecimalImpl) multiplicand).value)));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#divide(java.math.BigDecimal)
     */
    @Override
    public Number divide(Number divisor) {
        return newNumber(value.divide(((BigDecimalNumberImpl) divisor).value, roundingMode));
        // return new NumberBigDecimalImpl((value.divide(((NumberBigDecimalImpl) divisor).value)));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#divideToIntegralValue(java.math.BigDecimal)
     */
    @Override
    public Number divideToIntegralValue(Number divisor) {
        return newNumber(value.divideToIntegralValue(((BigDecimalNumberImpl) divisor).value));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#remainder(java.math.BigDecimal)
     */
    @Override
    public Number remainder(Number divisor) {
        return newNumber(value.remainder(((BigDecimalNumberImpl) divisor).value));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#divideAndRemainder(java.math.BigDecimal)
     */
    @Override
    public Number[] divideAndRemainder(Number divisor) {
        BigDecimal[] result = value.divideAndRemainder(((BigDecimalNumberImpl) divisor).value);
        return new Number[]{newNumber(result[0]), newNumber(result[1])};
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#pow(int)
     */
    @Override
    public Number pow(int n) {
        return newNumber(value.pow(n));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#abs()
     */
    @Override
    public Number abs() {
        return newNumber(value.abs());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#negate()
     */
    @Override
    public Number negate() {
        return newNumber(value.negate());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#compareTo(java.math.BigDecimal)
     */
    @Override
    public int compareTo(Number val) {
        return value.compareTo(((BigDecimalNumberImpl) val).value);
    }

    /**
     * Returns the minimum of this {@code Number} and {@code val}.
     */
    @Override
    public Number min(Number val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * Returns the maximum of this {@code BigDecimal} and {@code val}.
     */
    @Override
    public Number max(Number val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#valueOf(long)
     */
    @Override
    public Number valueOf(long val) {
        return newNumber(BigDecimal.valueOf(val));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#valueOf(double)
     */
    @Override
    public Number valueOf(double val) {
        return newNumber(BigDecimal.valueOf(val));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#floatValue()
     */
    @Override
    public float floatValue() {
        return value.floatValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.math.BigDecimal#doubleValue()
     */
    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return value.toString();
    }

    private BigDecimalNumberImpl newNumber(BigDecimal val) {
        // return new BigDecimalNumberImpl(val.setScale(value.precision(), roundingMode));
        if (value.precision() > val.precision()) {
            return new BigDecimalNumberImpl(val.setScale(value.precision(), roundingMode));
        } else {
            return new BigDecimalNumberImpl(val.setScale(val.precision(), roundingMode));
        }
    }

    @Override
    public Number sqrt() {
        return new BigDecimalNumberImpl(BigDecimalSqrtCalculator.sqrt(value, roundingMode));
    }
}
