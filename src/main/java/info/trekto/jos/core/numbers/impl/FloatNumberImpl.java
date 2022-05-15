package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Double implementation.
 * Immutable.
 *
 * @author Trayan Momkov
 * 10 Apr 2016
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
        return new Number[]{divideToIntegralValue(divisor), remainder(divisor)};
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

    @Override
    public int compareTo(Number val) {
        return Float.compare(value, ((FloatNumberImpl) val).value);
    }

    /**
     * Returns the minimum of this {@code Number} and {@code val}.
     */
    @Override
    public Number min(Number val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * Returns the maximum of this number and {@code val}.
     */
    @Override
    public Number max(Number val) {
        return (compareTo(val) >= 0 ? this : val);
    }

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
    public String toString() {
        return ((Float) value).toString();
    }

    @Override
    public Number sqrt() {
        return new FloatNumberImpl((float) Math.sqrt(value));
    }

    public Number atan2(Number n1, Number n2) {
        return New.num(Math.atan2(n1.floatValue(), n2.floatValue()));
    }

    @Override
    public Number cos(Number n) {
        return New.num(Math.cos(n.floatValue()));
    }

    @Override
    public Number sin(Number n) {
        return New.num(Math.sin(n.floatValue()));
    }

    @Override
    public Number cbrt(Number n) {
        return New.num(Math.cbrt(n.floatValue()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FloatNumberImpl)) {
            return false;
        }
        FloatNumberImpl that = (FloatNumberImpl) o;
        return Float.compare(that.value, value) == 0;
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
