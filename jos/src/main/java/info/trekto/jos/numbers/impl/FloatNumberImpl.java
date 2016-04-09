/**
 * 
 */
package info.trekto.jos.numbers.impl;

import java.math.BigDecimal;

import info.trekto.jos.numbers.Number;

/**
 * Double implementation.
 * Immutable.
 * @author Trayan Momkov
 * @date 10 Apr 2016
 */
public class FloatNumberImpl implements Number {

    private final float value;

    public FloatNumberImpl(BigDecimal value) {
        this.value = value.floatValue();
    }

    public FloatNumberImpl(String val) {
        value = Float.parseFloat(val);
    }

    public FloatNumberImpl(float val) {
        value = val;
    }

    public FloatNumberImpl(int val) {
        value = val;
    }

    public FloatNumberImpl(long val) {
        value = val;
    }

    @Override
    public Number add(Number augend) {
        return new FloatNumberImpl(value + ((FloatNumberImpl) augend).value);
    }

    @Override
    public Number subtract(Number subtrahend) {
        return new FloatNumberImpl(value - (((FloatNumberImpl) subtrahend).value));
    }

    @Override
    public Number multiply(Number multiplicand) {
        return new FloatNumberImpl(value * (((FloatNumberImpl) multiplicand).value));
    }

    @Override
    public Number divide(Number divisor) {
        return new FloatNumberImpl(value / (((FloatNumberImpl) divisor).value));
    }

    @Override
    public Number divideToIntegralValue(Number divisor) {
        return new FloatNumberImpl((float) Math.floor(value / (((FloatNumberImpl) divisor).value)));
    }

    @Override
    public Number remainder(Number divisor) {
        return new FloatNumberImpl(value % (((FloatNumberImpl) divisor).value));
    }

    @Override
    public Number[] divideAndRemainder(Number divisor) {
        return new Number[] { divideToIntegralValue(divisor), remainder(divisor) };
    }

    @Override
    public Number pow(int n) {
        return new FloatNumberImpl((float) Math.pow(value, n));
    }

    @Override
    public Number abs() {
        return new FloatNumberImpl(Math.abs(value));
    }

    @Override
    public Number negate() {
        return new FloatNumberImpl(-value);
    }

    // Comparison Operations

    @Override
    public int compareTo(Number val) {
        return (value < ((FloatNumberImpl) val).value) ? -1 : (value > ((FloatNumberImpl) val).value) ? 1 : 0;
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

    // Format Converters

    @Override
    public Number valueOf(long val) {
        return new FloatNumberImpl(val);
    }

    @Override
    public Number valueOf(double val) {
        return new FloatNumberImpl((float) val);
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return new BigDecimal(value);
    }

    @Override
    public String toString() {
        return ((Float) value).toString();
    }

    @Override
    public Number sqrt() {
        return new FloatNumberImpl((float) Math.sqrt(value));
    }
}
