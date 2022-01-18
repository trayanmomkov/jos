package info.trekto.jos.numbers;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public interface NumberFactory {
    enum NumberType {
        DOUBLE, APFLOAT, BIG_DECIMAL, FLOAT;
    }
}
