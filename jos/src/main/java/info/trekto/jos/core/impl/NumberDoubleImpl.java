/**
 * 
 */
package info.trekto.jos.core.impl;

import info.trekto.jos.core.Number;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * @date 19 Aug 2015
 */
public class NumberDoubleImpl implements Number {

    private final double value;

    public NumberDoubleImpl(BigDecimal value) {
        this.value = value.doubleValue();
    }

    public NumberDoubleImpl(String val) {
        value = Double.parseDouble(val);
    }

    public NumberDoubleImpl(double val) {
        value = val;
    }

    public NumberDoubleImpl(int val) {
        value = val;
    }

    public NumberDoubleImpl(long val) {
        value = val;
    }

    public Number add(Number augend) {
        return new NumberDoubleImpl(value + ((NumberDoubleImpl) augend).value);
    }

    public Number subtract(Number subtrahend) {
        return new NumberDoubleImpl(value - (((NumberDoubleImpl) subtrahend).value));
    }

    public Number multiply(Number multiplicand) {
        return new NumberDoubleImpl(value * (((NumberDoubleImpl) multiplicand).value));
    }

    public Number divide(Number divisor) {
        return new NumberDoubleImpl(value / (((NumberDoubleImpl) divisor).value));
    }

    public Number divideToIntegralValue(Number divisor) {
        return new NumberDoubleImpl(Math.floor(value / (((NumberDoubleImpl) divisor).value)));
    }

    public Number remainder(Number divisor) {
        return new NumberDoubleImpl(value % (((NumberDoubleImpl) divisor).value));
    }

    public Number[] divideAndRemainder(Number divisor) {
        return new Number[] { divideToIntegralValue(divisor), remainder(divisor) };
    }

    public Number pow(int n) {
        return new NumberDoubleImpl(Math.pow(value, n));
    }

    public Number abs() {
        return new NumberDoubleImpl(Math.abs(value));
    }

    public Number negate() {
        return new NumberDoubleImpl(-value);
    }

    // Comparison Operations

    public int compareTo(Number val) {
        return (value < ((NumberDoubleImpl) val).value) ? -1 : (value > ((NumberDoubleImpl) val).value) ? 1 : 0;
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

    // Format Converters

    public Number valueOf(long val) {
        return new NumberDoubleImpl(val);
    }

    public Number valueOf(double val) {
        return new NumberDoubleImpl(val);
    }

    public float floatValue() {
        return (float) value;
    }

    public double doubleValue() {
        return value;
    }

    public BigDecimal bigDecimalValue() {
        return new BigDecimal(value);
    }

    @Override
    public String toString() {
        return ((Double) value).toString();
    }
}