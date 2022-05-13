package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.Number;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Double implementation.
 * Immutable.
 *
 * @author Trayan Momkov
 * 19 Aug 2015
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
        return new Number[]{divideToIntegralValue(divisor), remainder(divisor)};
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

    public int compareTo(Number val) {
        return Double.compare(value, ((DoubleNumberImpl) val).value);
    }

    /**
     * Returns the minimum of this {@code Number} and {@code val}.
     */
    public Number min(Number val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * Returns the maximum of this number and {@code val}.
     */
    public Number max(Number val) {
        return (compareTo(val) >= 0 ? this : val);
    }

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

    @Override
    public String toString() {
        return ((Double) value).toString();
    }

    public Number sqrt() {
        return new DoubleNumberImpl(Math.sqrt(value));
    }

    @Override
    public Number atan2(Number n1, Number n2) {
        return new DoubleNumberImpl(Math.atan2(n1.doubleValue(), n2.doubleValue()));
    }

    @Override
    public Number cos(Number n) {
        return new DoubleNumberImpl(Math.cos(n.doubleValue()));
    }

    @Override
    public Number sin(Number n) {
        return new DoubleNumberImpl(Math.sin(n.doubleValue()));
    }

    @Override
    public Number cbrt(Number n) {
        return new DoubleNumberImpl(Math.cbrt(n.doubleValue()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoubleNumberImpl)) {
            return false;
        }
        DoubleNumberImpl that = (DoubleNumberImpl) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public Object getInternalValue() {
        return value;
    }
}
