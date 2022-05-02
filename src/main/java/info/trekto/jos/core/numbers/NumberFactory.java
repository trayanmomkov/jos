package info.trekto.jos.core.numbers;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public interface NumberFactory {
    Number createNumber(BigDecimal val);

    Number createNumber(String val);

    Number createNumber(double val);

    Number createNumber(int val);

    Number createNumber(long val);
    
    Number createPi();

    enum NumberType {
        DOUBLE, FLOAT, APFLOAT, BIG_DECIMAL
    }
}
