package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.impl.single_precision.SimulationFloat;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.NumberFactory;
import info.trekto.jos.core.numbers.NumberFactoryProxy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.Controller.DEFAULT_MIN_DISTANCE;
import static info.trekto.jos.core.Controller.DEFAULT_SCALE;
import static info.trekto.jos.core.Controller.createSimulation;
import static info.trekto.jos.core.ExecutionMode.AUTO;
import static info.trekto.jos.core.ExecutionMode.CPU;
import static info.trekto.jos.core.ExecutionMode.GPU;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.FLOAT;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ControllerTest {
    public static final boolean DOUBLE_YES = true;
    public static final boolean DOUBLE_NO = false;
    public static final boolean FLOAT_YES = true;
    public static final boolean FLOAT_NO = false;
    public static final boolean ABOVE_CPU_THRESHOLD = true;
    public static final boolean BELOW_CPU_THRESHOLD = false;

    @DataProvider(name = "simulation_type_selection")
    public static Object[][] logicImplementations() {
        return new Object[][]{
                {CPU, DOUBLE_YES, FLOAT_YES, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_YES, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_YES, FLOAT_YES, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_YES, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_YES, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_YES, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_YES, FLOAT_NO, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_NO, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_YES, FLOAT_NO, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_NO, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_NO, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_NO, ARBITRARY_PRECISION, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},

                {CPU, DOUBLE_YES, FLOAT_YES, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_YES, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationDouble.class, null},
                {AUTO, DOUBLE_YES, FLOAT_YES, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationDouble.class, SimulationAP.class},
                {CPU, DOUBLE_NO, FLOAT_YES, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_YES, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_YES, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_YES, FLOAT_NO, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_NO, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationDouble.class, null},
                {AUTO, DOUBLE_YES, FLOAT_NO, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationDouble.class, SimulationAP.class},
                {CPU, DOUBLE_NO, FLOAT_NO, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_NO, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_NO, DOUBLE, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},

                {CPU, DOUBLE_YES, FLOAT_YES, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_YES, FLOAT, ABOVE_CPU_THRESHOLD, SimulationFloat.class, null},
                {AUTO, DOUBLE_YES, FLOAT_YES, FLOAT, ABOVE_CPU_THRESHOLD, SimulationFloat.class, SimulationAP.class},
                {CPU, DOUBLE_NO, FLOAT_YES, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_YES, FLOAT, ABOVE_CPU_THRESHOLD, SimulationFloat.class, null},
                {AUTO, DOUBLE_NO, FLOAT_YES, FLOAT, ABOVE_CPU_THRESHOLD, SimulationFloat.class, SimulationAP.class},
                {CPU, DOUBLE_YES, FLOAT_NO, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_NO, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_YES, FLOAT_NO, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_NO, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_NO, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_NO, FLOAT, ABOVE_CPU_THRESHOLD, SimulationAP.class, null},


                {CPU, DOUBLE_YES, FLOAT_YES, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_YES, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_YES, FLOAT_YES, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_YES, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_YES, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_YES, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_YES, FLOAT_NO, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_NO, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_YES, FLOAT_NO, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_NO, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_NO, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_NO, ARBITRARY_PRECISION, BELOW_CPU_THRESHOLD, SimulationAP.class, null},

                {CPU, DOUBLE_YES, FLOAT_YES, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_YES, DOUBLE, BELOW_CPU_THRESHOLD, SimulationDouble.class, null},
                {AUTO, DOUBLE_YES, FLOAT_YES, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_YES, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_YES, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_YES, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_YES, FLOAT_NO, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_NO, DOUBLE, BELOW_CPU_THRESHOLD, SimulationDouble.class, null},
                {AUTO, DOUBLE_YES, FLOAT_NO, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_NO, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_NO, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_NO, DOUBLE, BELOW_CPU_THRESHOLD, SimulationAP.class, null},

                {CPU, DOUBLE_YES, FLOAT_YES, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_YES, FLOAT, BELOW_CPU_THRESHOLD, SimulationFloat.class, null},
                {AUTO, DOUBLE_YES, FLOAT_YES, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_YES, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_YES, FLOAT, BELOW_CPU_THRESHOLD, SimulationFloat.class, null},
                {AUTO, DOUBLE_NO, FLOAT_YES, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_YES, FLOAT_NO, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_YES, FLOAT_NO, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_YES, FLOAT_NO, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {CPU, DOUBLE_NO, FLOAT_NO, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {GPU, DOUBLE_NO, FLOAT_NO, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
                {AUTO, DOUBLE_NO, FLOAT_NO, FLOAT, BELOW_CPU_THRESHOLD, SimulationAP.class, null},
        };
    }

    @Test(dataProvider = "simulation_type_selection")
    public void testCreateSimulation(ExecutionMode executionMode, boolean gpuDoubleAvailable, boolean gpuFloatAvailable,
                                     NumberFactory.NumberType numberType, boolean aboveCpuThreshold, Class expectedSimulationClass,
                                     Class expectedCpuSimulationClass) {
        GpuChecker.gpuDoubleAvailable = gpuDoubleAvailable;
        GpuChecker.gpuFloatAvailable = gpuFloatAvailable;
        C.cpuThreshold = 10;
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(numberType);
        properties.setNumberOfObjects(C.cpuThreshold + (aboveCpuThreshold ? 1 : 0));
        NumberFactoryProxy.createNumberFactory(numberType, NumberFactory.DEFAULT_PRECISION);
        properties.setSecondsPerIteration(ONE);
        properties.setCoefficientOfRestitution(ONE);
        properties.setMinDistance(New.num(DEFAULT_MIN_DISTANCE));
        properties.setScale(DEFAULT_SCALE);
        
        Simulation simulation = createSimulation(properties, executionMode);

        assertEquals(simulation.getClass(), expectedSimulationClass);
        if (expectedSimulationClass != SimulationAP.class) {
            CpuSimulation cpuSimulation;
            if (simulation instanceof SimulationDouble) {
                cpuSimulation = ((SimulationDouble) simulation).getCpuSimulation();
            } else {
                cpuSimulation = ((SimulationFloat) simulation).getCpuSimulation();
            }

            if (expectedCpuSimulationClass == null) {
                assertNull(cpuSimulation);
            } else {
                assertNotNull(cpuSimulation);
                assertEquals(cpuSimulation.getClass(), expectedCpuSimulationClass);
            }
        }
    }

    /**
     * A soft-contact DOUBLE RUN must route to SimulationDouble (multi-core CPU) even with no GPU / CPU
     * mode. But PLAYBACK only replays saved frames and must stay a CpuSimulation (SimulationAP) -
     * otherwise play() fails casting SimulationDouble to CpuSimulation.
     */
    @Test
    public void softContactRunsOnDoubleButPlaysBackOnCpuSimulation() {
        GpuChecker.gpuDoubleAvailable = false;
        GpuChecker.gpuFloatAvailable = false;
        C.cpuThreshold = 10;
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(DOUBLE);
        properties.setNumberOfObjects(4);
        NumberFactoryProxy.createNumberFactory(DOUBLE, NumberFactory.DEFAULT_PRECISION);
        properties.setSecondsPerIteration(ONE);
        properties.setCoefficientOfRestitution(ONE);
        properties.setMinDistance(New.num(DEFAULT_MIN_DISTANCE));
        properties.setScale(DEFAULT_SCALE);
        properties.setMergeOnCollision(false);
        properties.setSoftContact(true);

        /* RUN: must be the double path (where soft contact is implemented), even with no GPU. */
        assertEquals(createSimulation(properties, CPU).getClass(), SimulationDouble.class);
        assertEquals(createSimulation(properties, AUTO).getClass(), SimulationDouble.class);

        /* PLAYBACK: must remain a CpuSimulation so play() can cast and replay it. */
        Simulation playback = createSimulation(properties, CPU, true);
        assertEquals(playback.getClass(), SimulationAP.class);
        assertTrue(playback instanceof CpuSimulation, "Playback simulation must be a CpuSimulation");
    }

    /* A soft-contact FLOAT run must route to SimulationFloat in every execution mode. */
    @Test
    public void softContactFloatSceneRoutesToSimulationFloat() {
        GpuChecker.gpuDoubleAvailable = false;
        GpuChecker.gpuFloatAvailable = true;
        C.cpuThreshold = 10;
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(FLOAT);
        properties.setNumberOfObjects(4);
        NumberFactoryProxy.createNumberFactory(FLOAT, NumberFactory.DEFAULT_PRECISION);
        properties.setSecondsPerIteration(ONE);
        properties.setCoefficientOfRestitution(ONE);
        properties.setMinDistance(New.num(DEFAULT_MIN_DISTANCE));
        properties.setScale(DEFAULT_SCALE);
        properties.setMergeOnCollision(false);
        properties.setSoftContact(true);

        assertEquals(createSimulation(properties, GPU).getClass(), SimulationFloat.class);
        assertEquals(createSimulation(properties, CPU).getClass(), SimulationFloat.class);
        assertEquals(createSimulation(properties, AUTO).getClass(), SimulationFloat.class);
    }

    @DataProvider(name = "soft_contact_execution")
    public static Object[][] softContactExecution() {
        return new Object[][]{
                // number type, execution mode, GPU available, above the CPU threshold, on the GPU
                {DOUBLE, CPU, true, true, false},
                {DOUBLE, GPU, true, false, true},
                {DOUBLE, AUTO, true, true, true},
                {DOUBLE, AUTO, true, false, false},
                {DOUBLE, GPU, false, true, false},
                {FLOAT, CPU, true, true, false},
                {FLOAT, GPU, true, false, true},
                {FLOAT, AUTO, true, true, true},
                {FLOAT, AUTO, true, false, false},
                {FLOAT, GPU, false, true, false},
        };
    }

    /* The soft contact runs on the GPU or on the CPU cores, as the execution mode and the CPU threshold say. */
    @Test(dataProvider = "soft_contact_execution")
    public void softContactFollowsExecutionMode(NumberFactory.NumberType numberType, ExecutionMode executionMode,
                                                boolean gpuAvailable, boolean aboveCpuThreshold, boolean expectedOnGpu) {
        GpuChecker.gpuDoubleAvailable = gpuAvailable;
        GpuChecker.gpuFloatAvailable = gpuAvailable;
        C.cpuThreshold = 10;
        SimulationProperties properties = softContactProperties(numberType, C.cpuThreshold + (aboveCpuThreshold ? 1 : 0));

        Simulation simulation = createSimulation(properties, executionMode);

        if (numberType == FLOAT) {
            assertEquals(((SimulationFloat) simulation).isSoftContactOnGpu(), expectedOnGpu);
        } else {
            assertEquals(((SimulationDouble) simulation).isSoftContactOnGpu(), expectedOnGpu);
        }
    }

    private static SimulationProperties softContactProperties(NumberFactory.NumberType numberType, int numberOfObjects) {
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(numberType);
        properties.setNumberOfObjects(numberOfObjects);
        NumberFactoryProxy.createNumberFactory(numberType, NumberFactory.DEFAULT_PRECISION);
        properties.setSecondsPerIteration(ONE);
        properties.setCoefficientOfRestitution(ONE);
        properties.setMinDistance(New.num(DEFAULT_MIN_DISTANCE));
        properties.setScale(DEFAULT_SCALE);
        properties.setMergeOnCollision(false);
        properties.setSoftContact(true);
        return properties;
    }
}