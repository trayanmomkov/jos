package info.trekto.jos.core;

import com.aparapi.Kernel;
import com.aparapi.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.util.Utils.*;

public class GpuChecker {
    private static final Logger logger = LoggerFactory.getLogger(GpuChecker.class);
    public static boolean gpuAvailable;

    public static void checkGpu() {
        try {
            AparapiTestKernel testKernel = new AparapiTestKernel();
            Range testKernelRange = Range.create(5);
            testKernel.setExecutionMode(GPU);

            testKernel.execute(testKernelRange);
            if (GPU.equals(testKernel.getExecutionMode())) {
                gpuAvailable = true;
            } else {
                gpuAvailable = false;
                info(logger, "GPU is not compatible. Will use CPU.");
            }
        } catch (Exception ex) {
            gpuAvailable = false;
            warn(logger, "GPU is not compatible. Will use CPU.", ex);
        }
    }

    static class AparapiTestKernel extends Kernel {
        public final double[] array = new double[]{1, 2, 3, 4, 5};

        /**
         * !!! DO NOT CHANGE THIS METHOD and methods called from it if you don't have experience with Aparapi library!!!
         * This code is translated to OpenCL and executed on the GPU.
         * You cannot use even simple 'break' here - it is not supported by Aparapi.
         */
        @Override
        public void run() {
            int i = getGlobalId();
            double a = 0;
            for (int j = 0; j < array.length; j++) {
                a = (a + array[i] + a * array[i] - j) / array[j];
            }
        }
    }
}
