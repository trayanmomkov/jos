import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

import static info.trekto.jos.util.Utils.nanoToHumanReadable;

public class AparapiTest {

//    @Test
    public void info() {
        Device device = Device.best();
        System.out.println(device.getShortDescription());
    }

//    @Test
    public void arraysMultiplication() {
        int n = 100000;
        final int k = 200000;
        Random random  = new Random(980767659786987L);

        final double[] inA = new double[k];
        for (int i = 0; i < k; i++) {
            inA[i] = random.nextDouble();
        }
        final double[] inB = new double[k];
        for (int i = 0; i < k; i++) {
            inB[i] = random.nextDouble();
        }
        
        /* Normal */
        assert (inA.length == inB.length);
        final double[] result = new double[inA.length];

        long start = System.nanoTime();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < inA.length; i++) {
                result[i] = inA[i] * inB[i];
            }
        }
        System.out.println("Normal: " + nanoToHumanReadable(System.nanoTime() - start));

        /* Aparapi */
        final double[] aparapiResult = new double[inA.length];
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                for (int j = 0; j < n; j++) {
                    int i = getGlobalId();
                    aparapiResult[i] = inA[i] * inB[i];
                }
            }
        };

        Range range = Range.create(aparapiResult.length);

        start = System.nanoTime();
        kernel.execute(range);
        System.out.println("Aparapi: " + nanoToHumanReadable(System.nanoTime() - start));

        Assert.assertEquals(aparapiResult, result);
    }
}
