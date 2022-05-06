package info.trekto.jos.numbers;

import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactoryProxy;
import info.trekto.jos.core.numbers.impl.DoubleNumberFactory;
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
        NumberFactoryProxy.setFactory(new DoubleNumberFactory());
        Number a = New.num(2);
        Number b = a.divide(New.num(3));
        Number c = a.add(b);
        assertEquals(c.doubleValue(), 2 / 3.0 + 2);

        assertEquals(New.num(1).divide(New.num(3)).doubleValue(), 1 / 3.0);

        assertEquals(New.num("0.0000000000000001").multiply(New.num("10000000000000000")).doubleValue(), 1.0);

        assertEquals(New.num("0.0000000000000001").subtract(New.num("10000000000000000")).doubleValue(),
                     -9999999999999999.9999999999999999);

        assertEquals(New.num("2").sqrt().doubleValue(), Math.sqrt(2));
    }
}
