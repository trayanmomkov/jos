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

    BigDecimal value;

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
     * @see info.trekto.jos.core.Number#add(info.trekto.jos.core.Number)
     */
    public Number add(Number augend) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#subtract(info.trekto.jos.core.Number)
     */
    public Number subtract(Number subtrahend) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#multiply(info.trekto.jos.core.Number)
     */
    public Number multiply(Number multiplicand) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#divide(info.trekto.jos.core.Number)
     */
    public Number divide(Number divisor) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#divideToIntegralValue(info.trekto.jos.core.Number)
     */
    public Number divideToIntegralValue(Number divisor) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#remainder(info.trekto.jos.core.Number)
     */
    public Number remainder(Number divisor) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#divideAndRemainder(info.trekto.jos.core.Number)
     */
    public Number[] divideAndRemainder(Number divisor) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#pow(int)
     */
    public Number pow(int n) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#abs()
     */
    public Number abs() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#negate()
     */
    public Number negate() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#compareTo(info.trekto.jos.core.Number)
     */
    public int compareTo(Number val) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#min(info.trekto.jos.core.Number)
     */
    public Number min(Number val) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#max(info.trekto.jos.core.Number)
     */
    public Number max(Number val) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#valueOf(long)
     */
    public Number valueOf(long val) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#valueOf(double)
     */
    public Number valueOf(double val) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#floatValue()
     */
    public float floatValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see info.trekto.jos.core.Number#doubleValue()
     */
    public double doubleValue() {
        // TODO Auto-generated method stub
        return 0;
    }

}
