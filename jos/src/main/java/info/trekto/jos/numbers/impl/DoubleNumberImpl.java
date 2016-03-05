/**
 * 
 */
package info.trekto.jos.numbers.impl;

import info.trekto.jos.numbers.Number;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * @date 19 Aug 2015
 */
public class DoubleNumberImpl implements Number {

    private final double value;

    public DoubleNumberImpl(BigDecimal value) {
        this.value = value.doubleValue();
    }

    public DoubleNumberImpl(String val) {
        value = Double.parseDouble(val);
    }

    public DoubleNumberImpl(double val) {
        value = val;
    }

    public DoubleNumberImpl(int val) {
        value = val;
    }

    public DoubleNumberImpl(long val) {
        value = val;
    }

    public Number add(Number augend) {
        return new DoubleNumberImpl(value + ((DoubleNumberImpl) augend).value);
    }

    public Number subtract(Number subtrahend) {
        return new DoubleNumberImpl(value - (((DoubleNumberImpl) subtrahend).value));
    }

    public Number multiply(Number multiplicand) {
        return new DoubleNumberImpl(value * (((DoubleNumberImpl) multiplicand).value));
    }

    public Number divide(Number divisor) {
        return new DoubleNumberImpl(value / (((DoubleNumberImpl) divisor).value));
    }

    public Number divideToIntegralValue(Number divisor) {
        return new DoubleNumberImpl(Math.floor(value / (((DoubleNumberImpl) divisor).value)));
    }

    public Number remainder(Number divisor) {
        return new DoubleNumberImpl(value % (((DoubleNumberImpl) divisor).value));
    }

    public Number[] divideAndRemainder(Number divisor) {
        return new Number[] { divideToIntegralValue(divisor), remainder(divisor) };
    }

    public Number pow(int n) {
        return new DoubleNumberImpl(Math.pow(value, n));
    }

    public Number abs() {
        return new DoubleNumberImpl(Math.abs(value));
    }

    public Number negate() {
        return new DoubleNumberImpl(-value);
    }

    // Comparison Operations

    public int compareTo(Number val) {
        return (value < ((DoubleNumberImpl) val).value) ? -1 : (value > ((DoubleNumberImpl) val).value) ? 1 : 0;
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
        return new DoubleNumberImpl(val);
    }

    public Number valueOf(double val) {
        return new DoubleNumberImpl(val);
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
