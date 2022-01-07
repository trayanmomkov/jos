package info.trekto.jos.numbers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ArithmeticCorrectnessTest {
    private static final Logger logger = LoggerFactory.getLogger(ArithmeticCorrectnessTest.class);

    @Test
    public void testBigDecimalAdd() {
        BigDecimal a = new BigDecimal(0);
        BigDecimal b = new BigDecimal(2).divide(new BigDecimal(3), RoundingMode.HALF_UP);
        logger.info("a precision(" + a.precision() + "): " + a);
        logger.info("b precision(" + b.precision() + "): " + b);
        BigDecimal c = a.add(b);
        logger.info("c precision(" + c.precision() + "): " + c);
    }

}
