package info.trekto.jos.core;

import com.aparapi.Range;
import info.trekto.jos.core.impl.double_precision.AparapiTestKernel;
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
}
