package info.trekto.jos.core;

import com.aparapi.Kernel;
import com.aparapi.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aparapi.Kernel.EXECUTION_MODE.GPU;
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.util.Utils.error;

public class GpuChecker {
    private static final Logger logger = LoggerFactory.getLogger(GpuChecker.class);
    private static final String CPU_VERSION_LINK = "https://sourceforge.net/projects/jos-n-body/files/jos-cpu.jar/download";

    public static void checkGpu() {
        String message = "Looks like your video card is not compatible with Aparapi.\n"
                + "Please download the CPU version: " + CPU_VERSION_LINK + "\n"
                + "Please send me email with this message and your video card model:\n"
                + "trayan.momkov аt gmail with subject: JOS - Error.\n";

        String htmlMessage = "<p>Looks like your video card is not compatible with Aparapi.</p>"
                + "<p>Please download the CPU version: <a href=\"" + CPU_VERSION_LINK + "\">" + CPU_VERSION_LINK + "</a></p>"
                + "<p>Please send me email with this message and your video card model:</p>"
                + "<p>trayan.momkov аt gmail with subject: JOS - Error.</p>";
        try {
            AparapiTestKernel testKernel = new AparapiTestKernel();
            Range testKernelRange = Range.create(5);
            testKernel.setExecutionMode(GPU);

            testKernel.execute(testKernelRange);
            if (!GPU.equals(testKernel.getExecutionMode())) {
                error(logger, message);
                C.showHtmlError(htmlMessage);
            }
        } catch (Exception ex) {
            error(logger, message, ex);
            C.showHtmlError(htmlMessage, ex);
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
