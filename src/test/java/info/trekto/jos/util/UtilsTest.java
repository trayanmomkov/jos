package info.trekto.jos.util;

import org.testng.annotations.Test;

import static info.trekto.jos.util.Utils.showRemainingTime;
import static org.testng.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void deepCopyShouldWorkCorrectly() {
    }

    @Test
    public void showRemainingTimeShouldWorkCorrectly() {
        assertEquals(showRemainingTime(0, 100, 100), "");
        assertEquals(showRemainingTime(1, 100, 100), "Iteration 1 Remaining time: 0 s.");
        assertEquals(showRemainingTime(2, 100, 100), "Iteration 2 Remaining time: 0 s.");
        assertEquals(showRemainingTime(1, 1000000000, 100), "Iteration 1 Remaining time: 1 m. 39 s.");
        assertEquals(showRemainingTime(2, 1000000000, 100), "Iteration 2 Remaining time: 49 s.");
        assertEquals(showRemainingTime(1, 100000000000L, 100),
                     "Iteration 1 Remaining time: 2 h. 45 m. 0 s.");
        assertEquals(showRemainingTime(2, 4000000000000L, 100),
                     "Iteration 2 Remaining time: 2 d. 6 h. 26 m. 40 s.");
    }

}