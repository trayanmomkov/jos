package info.trekto.jos.core;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.OpenCLDevice;
import com.aparapi.internal.opencl.OpenCLPlatform;
import info.trekto.jos.core.exceptions.SimulationException;
import info.trekto.jos.core.impl.SimulationGenerator;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.numbers.New;
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
import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.Utils.*;

public class GpuChecker {
    private static final Logger logger = LoggerFactory.getLogger(GpuChecker.class);
    public static boolean gpuDoubleAvailable;
    public static boolean gpuFloatAvailable;

    public static void checkGpu() {
        try {
            AparapiDoubleTestKernel testKernel = new AparapiDoubleTestKernel();
            Range testKernelRange = Range.create(5);
            testKernel.setExecutionMode(GPU);

            testKernel.execute(testKernelRange);
            if (GPU.equals(testKernel.getExecutionMode())) {
                gpuDoubleAvailable = true;
            } else {
                gpuDoubleAvailable = false;
                info(logger, "Double GPU is not compatible. Will try float.");
            }
        } catch (Throwable tr) {
            gpuDoubleAvailable = false;
            warn(logger, "Double GPU is not compatible. Will try float.", tr);
        }

        try {
            AparapiFloatTestKernel testKernel = new AparapiFloatTestKernel();
            Range testKernelRange = Range.create(5);
            testKernel.setExecutionMode(GPU);

            testKernel.execute(testKernelRange);
            if (GPU.equals(testKernel.getExecutionMode())) {
                gpuFloatAvailable = true;
            } else {
                gpuFloatAvailable = false;
                info(logger, "Float GPU is not compatible. Will use CPU.");
            }
        } catch (Throwable tr) {
            gpuFloatAvailable = false;
            warn(logger, "Float GPU is not compatible. Will use CPU. Try to restart your computer for GPU.", tr);
        }

        send_compatibility();
    }

    private static void send_compatibility() {
        new Thread(() -> {
            try {
                String vendors = "";
                String names = "";
                String gpuVersions = "";
                for (OpenCLPlatform platform : OpenCLPlatform.getUncachedOpenCLPlatforms()) {
                    for (OpenCLDevice device : platform.getOpenCLDevices()) {
                        vendors += (isNullOrBlank(vendors) ? "" : "|") + platform.getVendor();
                        gpuVersions += (isNullOrBlank(gpuVersions) ? "" : "|") + platform.getVersion();
                        names += (isNullOrBlank(names) ? "" : "|") + device.getName();
                    }
                }

                URL url = new URL("https://trekto.info/jos/");
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection) con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);

                Map<String, String> arguments = new HashMap<>();
                arguments.put("osVersion", System.getProperty("os.name"));
                arguments.put("vendor", vendors);
                arguments.put("name", names);
                arguments.put("gpuVersion", gpuVersions);
                arguments.put("floatCompatible", Boolean.toString(gpuFloatAvailable));
                arguments.put("doubleCompatible", Boolean.toString(gpuDoubleAvailable));
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
            } catch (Throwable ignored) {
            }
        }).start();
    }

    static class AparapiDoubleTestKernel extends Kernel {
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

    static class AparapiFloatTestKernel extends Kernel {
        public final float[] array = new float[]{1, 2, 3, 4, 5};

        /**
         * !!! DO NOT CHANGE THIS METHOD and methods called from it if you don't have experience with Aparapi library!!!
         * This code is translated to OpenCL and executed on the GPU.
         * You cannot use even simple 'break' here - it is not supported by Aparapi.
         */
        @Override
        public void run() {
            int i = getGlobalId();
            float a = 0;
            for (int j = 0; j < array.length; j++) {
                a = (a + array[i] + a * array[i] - j) / array[j];
            }
        }
    }

    public static int findCpuThreshold(SimulationProperties properties) throws SimulationException, InterruptedException {
        long startTime = System.nanoTime();
        SimulationProperties testProperties = new SimulationProperties(properties);
        createNumberFactory(testProperties.getNumberType(), testProperties.getPrecision());

        /* In May 2022 it looks like most video cards have 2048 or fewer shaders/cores */
        int top = Math.min(2048, testProperties.getNumberOfObjects());
        int bottom = 64;
        int factor = 204800;
        int numberOfObjects = bottom;
        while (top - 32 > bottom) {
            testProperties.setNumberOfObjects(numberOfObjects);
            testProperties.setSecondsPerIteration(New.num("0.0000001"));
            SimulationGenerator.generateObjects(testProperties, false);

            long numberOfIterations = factor / ((long)numberOfObjects * numberOfObjects / 4);
            if (numberOfIterations < 25) {
                numberOfIterations = 25;
            }
            info(logger, "Range: " + bottom + " - " + top + " Objects: " + numberOfObjects + " Iterations: " + numberOfIterations);
            double cpuTime = measureIteration(new SimulationAP(testProperties), numberOfIterations);
            if (C.isHasToStopCpuGpuMeasuring()) {
                return (top + bottom) / 2;
            }
            double gpuTime;
            if (testProperties.getNumberType() == DOUBLE) {
                gpuTime = measureIteration(new SimulationDouble(testProperties, null), numberOfIterations);
            } else {
                gpuTime = measureIteration(new SimulationDouble(testProperties, null), numberOfIterations);
            }
            if (C.isHasToStopCpuGpuMeasuring()) {
                return (top + bottom) / 2;
            }

            if (cpuTime < gpuTime) {
                bottom = numberOfObjects;
                numberOfObjects = bottom + (top - bottom) / 2;
                info(logger, "CPU < GPU\n");
            } else {
                top = numberOfObjects;
                numberOfObjects = top - (top - bottom) / 2;
                info(logger, "CPU > GPU\n");
            }
        }
        info(logger, "Measuring time total: " + nanoToHumanReadable(System.nanoTime() - startTime));
        return (top + bottom) / 2;
    }

    private static double measureIteration(SimulationAP simulation, long numberOfIterations) throws SimulationException, InterruptedException {
        simulation.init(false);

        long startTime = System.nanoTime();
        for (long i = 0; i < numberOfIterations; i++) {
            if (C.isHasToStopCpuGpuMeasuring()) {
                return 0;
            }
            simulation.doIteration(false, i + 1);
        }

        return (System.nanoTime() - startTime) / (double)numberOfIterations;
    }
}
