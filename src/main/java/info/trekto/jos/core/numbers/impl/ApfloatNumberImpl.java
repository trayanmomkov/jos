package info.trekto.jos.core.numbers.impl;

import info.trekto.jos.core.numbers.Number;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumber;

public class ApfloatNumberImpl implements Number {

    private final Apfloat value;

    public ApfloatNumberImpl(Apfloat value) {
        this.value = value;
    }

    public ApfloatNumberImpl(String val, long precision) {
        value = new Apfloat(val, precision);
    }

    public ApfloatNumberImpl(double val, long precision) {
        value = new Apfloat(val, precision);
    }

    public ApfloatNumberImpl(long val, long precision) {
        value = new Apfloat(val, precision);
    }

    public ApfloatNumberImpl(int val, long precision) {
        value = new Apfloat(val, precision);
    }

    public ApfloatNumberImpl(BigInteger val, long precision) {
        value = new Apfloat(val, precision);
    }

    public ApfloatNumberImpl(BigDecimal val, long precision) {
        value = new Apfloat(val, precision);
    }

    @Override
    public Number add(Number augend) {
        return newNumber(value.add(((ApfloatNumberImpl) augend).value));
    }

    @Override
    public Number subtract(Number subtrahend) {
        return newNumber(value.subtract(((ApfloatNumberImpl) subtrahend).value));
    }

    @Override
    public Number multiply(Number multiplicand) {
        return newNumber(value.multiply(((ApfloatNumberImpl) multiplicand).value));
        // return new NumberBigDecimalImpl((value.multiply(((NumberBigDecimalImpl) multiplicand).value)));
    }

    @Override
    public Number divide(Number divisor) {
        return newNumber(value.divide(((ApfloatNumberImpl) divisor).value));
        // return new NumberBigDecimalImpl((value.divide(((NumberBigDecimalImpl) divisor).value)));
    }

    @Override
    public Number divideToIntegralValue(Number divisor) {
        throw new RuntimeException("divideToIntegralValue not implemented");
    }

    @Override
    public Number remainder(Number divisor) {
        throw new RuntimeException("remainder not implemented");
    }

    @Override
    public Number[] divideAndRemainder(Number divisor) {
        throw new RuntimeException("divideAndRemainder not implemented");
    }

    @Override
    public Number pow(int n) {
        return newNumber(ApfloatMath.pow(value, n));
    }

    @Override
    public Number abs() {
        return newNumber(ApfloatMath.abs(value));
    }

    @Override
    public Number negate() {
        return newNumber(value.negate());
    }

    @Override
    public int compareTo(Number val) {
        return value.compareTo(((ApfloatNumberImpl) val).value);
    }

    /**
     * Returns the minimum of this {@code Number} and {@code val}.
     */
    @Override
    public Number min(Number val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * Returns the maximum of this {@code Apfloat} and {@code val}.
     */
    @Override
    public Number max(Number val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    @Override
    public Number valueOf(long val) {
        return createNumber(val);
    }

    @Override
    public Number valueOf(double val) {
        return createNumber(val);
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
        return BigDecimal.valueOf(value.doubleValue()).setScale((int) value.precision(), BigDecimalNumberImpl.roundingMode);
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
        if (!(o instanceof ApfloatNumberImpl)) {
            return false;
        }
        ApfloatNumberImpl that = (ApfloatNumberImpl) o;
        if (value == null) {
            return that.value == null;
        }
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    private ApfloatNumberImpl newNumber(Apfloat val) {
        return new ApfloatNumberImpl(val);
    }

    @Override
    public Number sqrt() {
        return new ApfloatNumberImpl(ApfloatMath.sqrt(value));
    }

    public Number atan2(Number n1, Number n2) {
        return new ApfloatNumberImpl(ApfloatMath.atan2(((ApfloatNumberImpl) n1).value, ((ApfloatNumberImpl) n2).value));
    }

    public Number cos(Number n) {
        return new ApfloatNumberImpl(ApfloatMath.cos(((ApfloatNumberImpl) n).value));
    }

    @Override
    public Number sin(Number n) {
        return new ApfloatNumberImpl(ApfloatMath.sin(((ApfloatNumberImpl) n).value));
    }

    public Number cbrt(Number n) {
        return new ApfloatNumberImpl(ApfloatMath.cbrt(((ApfloatNumberImpl) n).value));
    }
}
