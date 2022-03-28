package info.trekto.jos.core.impl;

import com.aparapi.Kernel;

public class AparapiTestKernel extends Kernel {
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
