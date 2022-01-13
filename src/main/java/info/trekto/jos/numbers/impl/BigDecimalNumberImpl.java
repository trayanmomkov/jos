package info.trekto.jos.numbers.impl;

import info.trekto.jos.numbers.Number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * BigDecimal implementation.
 * Immutable.
 * @author Trayan Momkov
 * 18 Aug 2015
 */
public class BigDecimalNumberImpl implements Number {

    private final BigDecimal value;
    public static final RoundingMode roundingMode = RoundingMode.HALF_EVEN;

    public BigDecimalNumberImpl(BigDecimal value) {
        this.value = value;
    }

    public BigDecimalNumberImpl(String val, MathContext mc, int scale) {
        value = new BigDecimal(val, mc).setScale(scale, mc.getRoundingMode());
    }

    public BigDecimalNumberImpl(double val, MathContext mc, int scale) {
        value = new BigDecimal(val, mc).setScale(scale, mc.getRoundingMode());
    }

    public BigDecimalNumberImpl(BigInteger val, MathContext mc, int scale) {
        value = new BigDecimal(val, mc).setScale(scale, mc.getRoundingMode());
    }

    public BigDecimalNumberImpl(BigInteger unscaledVal, int scale) {
        value = new BigDecimal(unscaledVal, scale);
    }

    public BigDecimalNumberImpl(BigInteger unscaledVal, int scale, MathContext mc) {
        value = new BigDecimal(unscaledVal, scale, mc);
    }

    public BigDecimalNumberImpl(int val, MathContext mc, int scale) {
        value = new BigDecimal(val, mc).setScale(scale, mc.getRoundingMode());
    }

    public BigDecimalNumberImpl(long val, MathContext mc, int scale) {
        value = new BigDecimal(val, mc).setScale(scale, mc.getRoundingMode());
    }

    @Override
    public Number add(Number augend) {
        return newNumber(value.add(((BigDecimalNumberImpl) augend).value));
    }

    @Override
    public Number subtract(Number subtrahend) {
        return newNumber(value.subtract(((BigDecimalNumberImpl) subtrahend).value));
    }

    @Override
    public Number multiply(Number multiplicand) {
        return newNumber(value.multiply(((BigDecimalNumberImpl) multiplicand).value));
        // return new NumberBigDecimalImpl((value.multiply(((NumberBigDecimalImpl) multiplicand).value)));
    }

    @Override
    public Number divide(Number divisor) {
        return newNumber(value.divide(((BigDecimalNumberImpl) divisor).value, roundingMode));
        // return new NumberBigDecimalImpl((value.divide(((NumberBigDecimalImpl) divisor).value)));
    }

    @Override
    public Number divideToIntegralValue(Number divisor) {
        return newNumber(value.divideToIntegralValue(((BigDecimalNumberImpl) divisor).value));
    }

    @Override
    public Number remainder(Number divisor) {
        return newNumber(value.remainder(((BigDecimalNumberImpl) divisor).value));
    }

    @Override
    public Number[] divideAndRemainder(Number divisor) {
        BigDecimal[] result = value.divideAndRemainder(((BigDecimalNumberImpl) divisor).value);
        return new Number[]{newNumber(result[0]), newNumber(result[1])};
    }

    @Override
    public Number pow(int n) {
        return newNumber(value.pow(n));
    }

    @Override
    public Number abs() {
        return newNumber(value.abs());
    }

    @Override
    public Number negate() {
        return newNumber(value.negate());
    }

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

    @Override
    public Number valueOf(long val) {
        return newNumber(BigDecimal.valueOf(val));
    }

    @Override
    public Number valueOf(double val) {
        return newNumber(BigDecimal.valueOf(val));
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BigDecimalNumberImpl)) {
            return false;
        }
        BigDecimalNumberImpl that = (BigDecimalNumberImpl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private BigDecimalNumberImpl newNumber(BigDecimal val) {
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
