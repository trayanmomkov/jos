package info.trekto.jos.core.numbers;

import java.math.BigDecimal;

/**
 * Just a shortcut for NumberFactoryProxy.
 *
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public class New {
    public static Number ZERO;
    public static Number ONE;
    public static Number TWO;
    public static Number THREE;
    public static Number FOUR;
    public static Number RATIO_FOUR_THREE;
    public static Number BILLION;
    public static Number IGNORED;

    public static Number num(BigDecimal val) {
        return NumberFactoryProxy.createNumber(val);
    }

    public static Number num(String val) {
        return NumberFactoryProxy.createNumber(val);
    }

    public static Number num(double val) {
        return NumberFactoryProxy.createNumber(val);
    }

    public static Number num(int val) {
        return NumberFactoryProxy.createNumber(val);
    }

    public static Number num(long val) {
        return NumberFactoryProxy.createNumber(val);
    }
}
