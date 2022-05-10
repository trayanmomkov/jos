package info.trekto.jos.core;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.opencl.OpenCLPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

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
            warn(logger, "GPU is not compatible. Will use CPU. Try to restart your machine for GPU.", ex);
        }
        send_compatibility();
    }

    private static void send_compatibility() {
        new Thread(() -> {
            try {
                String vendors = "";
                String names = "";
                String versions = "";
                for (OpenCLPlatform platform : OpenCLPlatform.getUncachedOpenCLPlatforms()) {
                    for (OpenCLDevice device : platform.getOpenCLDevices()) {
                        vendors += (isNullOrBlank(vendors) ? "" : "|") + platform.getVendor();
                        versions += (isNullOrBlank(versions) ? "" : "|") + platform.getVersion();
                        names += (isNullOrBlank(names) ? "" : "|") + device.getName();
                    }
                }

                URL url = new URL("https://trekto.info/jos/");
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);

                Map<String, String> arguments = new HashMap<>();
                arguments.put("vendor", vendors);
                arguments.put("name", names);
                arguments.put("version", versions);
                arguments.put("compatible", Boolean.toString(gpuAvailable));
                StringJoiner sj = new StringJoiner("&");
                for (Map.Entry<String, String> entry : arguments.entrySet()) {
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                                   + URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                http.setFixedLengthStreamingMode(length);
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                http.connect();
                try (OutputStream os = http.getOutputStream()) {
                    os.write(out);
                }
            } catch (Exception ignored) {
            }
        }).start();
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
