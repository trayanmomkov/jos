package info.trekto.jos.numbers;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
import info.trekto.jos.core.numbers.NumberFactoryProxy;
import info.trekto.jos.core.numbers.impl.BigDecimalNumberFactory;
import info.trekto.jos.core.numbers.impl.DoubleNumberFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.numbers.impl.BigDecimalNumberImpl.roundingMode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

    @Test
    public void testBigDecimal() {
        int precision = 32;
        int scale = 16;

        MathContext mathContext = new MathContext(precision, roundingMode);
        NumberFactoryProxy.setFactory(new BigDecimalNumberFactory(mathContext, scale));
        C.setSimulation(new SimulationAP(new SimulationProperties()));
        C.getSimulation().getProperties().setScale(scale);

        Number a = New.num(2);
        Number b = a.divide(New.num(3));
        Number c = a.add(b);

        assertTrue(Pattern.compile("2\\.6{" + (scale - 1) + "}7").matcher(df.format(c.bigDecimalValue())).matches());
        assertEquals(New.num(1).divide(New.num(3)).bigDecimalValue().setScale(scale),
                     New.num("0.3333333333333333333333333333333333333333").bigDecimalValue().setScale(scale));

        assertEquals(df.format(New.num("0.0000000000000001").multiply(New.num("10000000000000000")).bigDecimalValue()), "1");

        assertEquals(df.format(New.num("0.0000000000000001").subtract(New.num("10000000000000000")).bigDecimalValue()),
                     "-9999999999999999.9999999999999999");

        assertEquals(df.format(New.num("2").sqrt().bigDecimalValue()), "1.414213562373095");
    }
}
