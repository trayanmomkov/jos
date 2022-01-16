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
        final int k = 1000000;
        Random random  = new Random(980767659786987L);

        final float[] inA = new float[k];
        for (int i = 0; i < k; i++) {
            inA[i] = random.nextFloat();
        }
        final float[] inB = new float[k];
        for (int i = 0; i < k; i++) {
            inB[i] = random.nextFloat();
        }
        
        /* Normal */
        assert (inA.length == inB.length);
        final float[] result = new float[inA.length];

        long start = System.nanoTime();
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < inA.length; i++) {
                result[i] = inA[i] * inB[i];
            }
        }
        System.out.println("Normal: " + nanoToHumanReadable(System.nanoTime() - start));

        /* Aparapi */
        final float[] aparapiResult = new float[inA.length];
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
