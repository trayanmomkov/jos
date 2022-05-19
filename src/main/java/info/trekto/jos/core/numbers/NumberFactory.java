package info.trekto.jos.core.numbers;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * 7 Dec 2015
 */
public interface NumberFactory {
    int DEFAULT_PRECISION = 16;

    Number createNumber(BigDecimal val);

    Number createNumber(String val);

    Number createNumber(double val);

    Number createNumber(int val);

    Number createNumber(long val);
    
    Number createPi();

    enum NumberType {
        FLOAT, DOUBLE, ARBITRARY_PRECISION
    }
}
