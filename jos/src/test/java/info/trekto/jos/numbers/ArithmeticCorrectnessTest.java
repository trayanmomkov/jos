package info.trekto.jos.numbers;

import java.math.BigDecimal;

import org.testng.annotations.Test;

public class ArithmeticCorrectnessTest {

    @Test
    public void testBigDecimalAdd() {
        BigDecimal a = new BigDecimal(0d);
        BigDecimal b = new BigDecimal("0.000000000000000001");
        System.out.println("b: " + b);
        BigDecimal c = a.add(b);
        System.out.println("c: " + c);
    }

}
