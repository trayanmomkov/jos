/**
 * 
 */
package info.trekto.jos.numbers;

import java.math.BigDecimal;

/**
 * @author Trayan Momkov
 * @date 7 Dec 2015
 */
public interface NumberFactory {
    public Number createNumber(BigDecimal val);

    public Number createNumber(String val);

    public Number createNumber(double val);

    public Number createNumber(int val);

    public Number createNumber(long val);
}
