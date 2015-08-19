/**
 * 
 */
package info.trekto.jos.coreI.impl;

import info.trekto.jos.core.Number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * @author Trayan Momkov
 * @date 18 Aug 2015
 */
public class NumberBigDecimalImpl implements Number {

    private final BigDecimal value;

    /**
     * @param value
     */
    public NumberBigDecimalImpl(BigDecimal value) {
        this.value = value;
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in, int offset, int len)
     */
    public NumberBigDecimalImpl(char[] in, int offset, int len) {
        value = new BigDecimal(in, offset, len);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in, int offset, int len, MathContext mc)
     */
    public NumberBigDecimalImpl(char[] in, int offset, int len, MathContext mc) {
        value = new BigDecimal(in, offset, len, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in)
     */
    public NumberBigDecimalImpl(char[] in) {
        value = new BigDecimal(in);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(char[] in, MathContext mc)
     */
    public NumberBigDecimalImpl(char[] in, MathContext mc) {
        value = new BigDecimal(in, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(String val)
     */
    public NumberBigDecimalImpl(String val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(String val, MathContext mc)
     */
    public NumberBigDecimalImpl(String val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(double val)
     */
    public NumberBigDecimalImpl(double val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(double val, MathContext mc)
     */
    public NumberBigDecimalImpl(double val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger val)
     */
    public NumberBigDecimalImpl(BigInteger val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger val, MathContext mc)
     */
    public NumberBigDecimalImpl(BigInteger val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger unscaledVal, int scale)
     */
    public NumberBigDecimalImpl(BigInteger unscaledVal, int scale) {
        value = new BigDecimal(unscaledVal, scale);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(BigInteger unscaledVal, int scale, MathContext mc)
     */
    public NumberBigDecimalImpl(BigInteger unscaledVal, int scale, MathContext mc) {
        value = new BigDecimal(unscaledVal, scale, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(int val)
     */
    public NumberBigDecimalImpl(int val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(int val, MathContext mc)
     */
    public NumberBigDecimalImpl(int val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(long val)
     */
    public NumberBigDecimalImpl(long val) {
        value = new BigDecimal(val);
    }

    /**
     * @see java.math.BigDecimal#BigDecimal(long val, MathContext mc)
     */
    public NumberBigDecimalImpl(long val, MathContext mc) {
        value = new BigDecimal(val, mc);
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#add(java.math.BigDecimal)
     */
    public Number add(Number augend) {
        return newNumber(value.add(((NumberBigDecimalImpl) augend).value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#subtract(java.math.BigDecimal)
     */
    public Number subtract(Number subtrahend) {
        return newNumber(value.subtract(((NumberBigDecimalImpl) subtrahend).value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#multiply(java.math.BigDecimal)
     */
    public Number multiply(Number multiplicand) {
        return newNumber(value.multiply(((NumberBigDecimalImpl) multiplicand).value));
        // return new NumberBigDecimalImpl((value.multiply(((NumberBigDecimalImpl) multiplicand).value)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#divide(java.math.BigDecimal)
     */
    public Number divide(Number divisor) {
        return newNumber(value.divide(((NumberBigDecimalImpl) divisor).value));
        // return new NumberBigDecimalImpl((value.divide(((NumberBigDecimalImpl) divisor).value)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#divideToIntegralValue(java.math.BigDecimal)
     */
    public Number divideToIntegralValue(Number divisor) {
        return newNumber(value.divideToIntegralValue(((NumberBigDecimalImpl) divisor).value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#remainder(java.math.BigDecimal)
     */
    public Number remainder(Number divisor) {
        return newNumber(value.remainder(((NumberBigDecimalImpl) divisor).value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#divideAndRemainder(java.math.BigDecimal)
     */
    public Number[] divideAndRemainder(Number divisor) {
        BigDecimal[] result = value.divideAndRemainder(((NumberBigDecimalImpl) divisor).value);
        return new Number[] { newNumber(result[0]), newNumber(result[1]) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#pow(int)
     */
    public Number pow(int n) {
        return newNumber(value.pow(n));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#abs()
     */
    public Number abs() {
        return newNumber(value.abs());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#negate()
     */
    public Number negate() {
        return newNumber(value.negate());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#compareTo(java.math.BigDecimal)
     */
    public int compareTo(Number val) {
        return value.compareTo(((NumberBigDecimalImpl) val).value);
    }

    /**
     * Returns the minimum of this {@code Number} and {@code val}.
     */
    public Number min(Number val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * Returns the maximum of this {@code BigDecimal} and {@code val}.
     */
    public Number max(Number val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#valueOf(long)
     */
    public Number valueOf(long val) {
        return newNumber(BigDecimal.valueOf(val));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#valueOf(double)
     */
    public Number valueOf(double val) {
        return newNumber(BigDecimal.valueOf(val));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#floatValue()
     */
    public float floatValue() {
        return value.floatValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.math.BigDecimal#doubleValue()
     */
    public double doubleValue() {
        return value.doubleValue();
    }

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

    private NumberBigDecimalImpl newNumber(BigDecimal val) {
        return new NumberBigDecimalImpl(val.setScale(value.precision(), BigDecimal.ROUND_HALF_EVEN));
    }
}
