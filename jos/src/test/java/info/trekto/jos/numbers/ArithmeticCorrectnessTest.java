package info.trekto.jos.numbers;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import info.trekto.jos.util.Utils;

public class ArithmeticCorrectnessTest {

    @Test
    public void testBigDecimalAdd() {
        BigDecimal a = new BigDecimal(0);
        BigDecimal b = new BigDecimal(2).divide(new BigDecimal(3));
        Utils.log("a precision(" + a.precision() + "): " + a);
        Utils.log("b precision(" + b.precision() + "): " + b);
        BigDecimal c = a.add(b);
        Utils.log("c precision(" + c.precision() + "): " + c);
    }

}
