package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.impl.double_precision.SimulationDouble;
import info.trekto.jos.core.impl.single_precision.SimulationFloat;
import info.trekto.jos.core.numbers.NumberFactory;
import info.trekto.jos.core.numbers.NumberFactoryProxy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
        Controller.cpuThreshold = 10;
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(numberType);
        properties.setNumberOfObjects(Controller.cpuThreshold + (aboveCpuThreshold ? 1 : 0));
        NumberFactoryProxy.createNumberFactory(numberType, NumberFactory.DEFAULT_PRECISION);
        properties.setSecondsPerIteration(ONE);
        properties.setCoefficientOfRestitution(ONE);
        
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
}