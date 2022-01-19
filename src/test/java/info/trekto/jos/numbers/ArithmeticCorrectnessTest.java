package info.trekto.jos.numbers;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DecimalFormat;

import static org.testng.Assert.assertEquals;

public class ArithmeticCorrectnessTest {
    DecimalFormat df = new DecimalFormat("#.#");

    @BeforeClass
    public void init() {
        df.setMaximumFractionDigits(32);
    }

    @Test
    public void testDoubleAdd() {
        double a = 2;
        double b = a / 3;
        double c = a + b;
        assertEquals(c, 2/3.0+2);
        
        assertEquals(0.0000000000000001 * 10000000000000000L, 1.0);
        
        assertEquals(0.0000000000000001 - 10000000000000000L,
         -9999999999999999.9999999999999999);
    }

    @Test
    public void testBigDecimal() {
        int precision = 32;
        int scale = 16;

        double a = 2;
        double b = a / 3;
        double c = a + b;

        assertEquals(1 / 3.0, 0.3333333333333333333333333333333333333333);
                
        assertEquals(df.format(0.0000000000000001 * 10000000000000000L), "1");
    }
}
