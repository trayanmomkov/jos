package info.trekto.jos.core.numbers;

/**
 * This interface is initially extracted from java.math.BigDecimal (just a portion of its public methods) and after that
 * extended.
 *
 * @author Trayan Momkov
 * 18 Aug 2015
 */
public interface Number {
    // Arithmetic Operations

    /**
     * Returns a Number whose value is {@code (this + augend)}.
     *
     * @param augend value to be added to this {@code Number}.
     * @return {@code this + augend}
     */
    Number add(Number augend);

    /**
     * Returns a {@code Number} whose value is {@code (this - subtrahend)}.
     */
    Number subtract(Number subtrahend);

    /**
     * Returns a {@code Number} whose value is <tt>(this &times; multiplicand)</tt>.
     *
     * @param multiplicand value to be multiplied by this {@code Number}.
     * @return {@code this * multiplicand}
     */
    Number multiply(Number multiplicand);


    /**
     * Returns a {@code NumberDecimal} whose value is {@code (this / divisor)}.
     *
     * @param divisor value by which this {@code Number} is to be divided.
     * @return {@code this / divisor}
     */
    Number divide(Number divisor);

    /**
     * Returns a {@code Number} whose value is the integer part of the quotient {@code (this / divisor)} rounded down.
     *
     * @param divisor value by which this {@code Number} is to be divided.
     * @return The integer part of {@code this / divisor}.
     */
    Number divideToIntegralValue(Number divisor);

    /**
     * Returns a {@code Number} whose value is {@code (this % divisor)}.
     * Note that this is not the modulo operation (the result can be negative).
     *
     * @param divisor value by which this {@code Number} is to be divided.
     * @return {@code this % divisor}.
     */
    Number remainder(Number divisor);

    /**
     * Returns a two-element {@code Number} array containing the
     * result of {@code divideToIntegralValue} followed by the result of {@code remainder} on the two operands.
     *
     * @param divisor value by which this {@code Number} is to be divided, and the remainder computed.
     * @return a two element {@code Number} array: the quotient (the result of {@code divideToIntegralValue})
     * is the initial element and the remainder is the final element.
     */
    Number[] divideAndRemainder(Number divisor);

    /**
     * Returns a {@code Number} whose value is <tt>(this<sup>n</sup>)</tt>.
     *
     * @param n power to raise this {@code Number} to.
     * @return <tt>this<sup>n</sup></tt>
     */
    Number pow(int n);

    /**
     * Returns a {@code Number} whose value is the absolute value of this {@code Number}.
     *
     * @return {@code abs(this)}
     */
    Number abs();

    /**
     * Returns a {@code Number} whose value is {@code (-this)}.
     *
     * @return {@code -this}.
     */
    Number negate();

    /**
     * Compute the square root of the number.
     *
     * @return
     */
    Number sqrt();

    // Comparison Operations

    /**
     * Compares this {@code Number} with the specified {@code Number}.
     *
     * @param val {@code Number} to which this {@code Number} is to be compared.
     * @return -1, 0, or 1 as this {@code Number} is numerically less than, equal to, or greater than {@code val}.
     */
    int compareTo(Number val);

    /**
     * Returns the minimum of this {@code Number} and {@code val}.
     *
     * @param val value with which the minimum is to be computed.
     * @return the {@code Number} whose value is the lesser of this {@code Number} and {@code val}. If they are equal,
     * {@code this} is returned.
     */
    Number min(Number val);

    /**
     * Returns the maximum of this {@code Number} and {@code val}.
     *
     * @param val value with which the maximum is to be computed.
     * @return the {@code Number} whose value is the greater of this {@code Number} and {@code val}. If they are equal,
     * {@code this} is returned.
     */
    Number max(Number val);

    // Format Converters

    /**
     * Translates a {@code long} value into a {@code Number}
     *
     * @param val value of the {@code Number}.
     * @return a {@code Number} whose value is {@code val}.
     */
    Number valueOf(long val);

    /**
     * Translates a {@code double} into a {@code Number}.
     *
     * @param val {@code double} to convert to a {@code Number}.
     * @return a {@code Number} whose value is equal to or approximately
     * equal to the value of {@code val}.
     */
    Number valueOf(double val);

    /**
     * Converts this {@code Number} to a {@code float}.
     *
     * @return this {@code Number} converted to a {@code float}.
     */
    float floatValue();

    /**
     * Converts this {@code Number} to a {@code double}.
     *
     * @return this {@code Number} converted to a {@code double}.
     */
    double doubleValue();

    String toString();

    Number atan2(Number n1, Number n2);

    Number cos(Number n);

    Number sin(Number n);

    Number cbrt(Number n);
    
    Object getInternalValue();
}
