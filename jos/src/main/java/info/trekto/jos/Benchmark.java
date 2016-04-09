/**
 *
 */
package info.trekto.jos;

import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator.ForceCalculatorType;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.numbers.NumberFactory;
import info.trekto.jos.numbers.NumberFactory.NumberType;
import info.trekto.jos.util.Utils;

/**
 * @author Trayan Momkov
 * @date 31 Mar 2016
 *
 */
public class Benchmark {

    private static final int NUMBER_OF_ITERATIONS = 10;

    public static void main(String[] args) throws SimulationException {
        Benchmark benchmark = new Benchmark();
        //        int numberOfObjects = 6400;
        //        int numberOfObjects = 12800;
        int numberOfObjects = 400;
        //        String inputFileName = "/PSC_5_6400_objects_RUN";
        //        String inputFileName = "/PSC_5_12800_objects_RUN";
        String inputFileName = "/PSC_5_400_objects_RUN";
        if (args.length > 0 && args[0] != null) {
            inputFileName = args[0];
        }

        /** Double */
        benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, 1, NumberType.DOUBLE, 0, inputFileName);
        if (Utils.CORES > 2) {
            benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, Utils.CORES / 2, NumberType.DOUBLE, 0,
                    inputFileName);
        }
        benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, Utils.CORES, NumberType.DOUBLE, 0, inputFileName);
        benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, Utils.CORES * 2, NumberType.DOUBLE, 0,
                inputFileName);

        /** BigDecimal faster in JRE 1.8 */
        benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, 1, NumberType.BIG_DECIMAL, 0, inputFileName);
        if (Utils.CORES > 2) {
            benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, Utils.CORES / 2, NumberType.BIG_DECIMAL, 0,
                    inputFileName);
        }
        benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, Utils.CORES, NumberType.BIG_DECIMAL, 0,
                inputFileName);
        benchmark.runBenchmark(numberOfObjects, NUMBER_OF_ITERATIONS, Utils.CORES * 2, NumberType.BIG_DECIMAL, 0,
                inputFileName);
    }

    private void runBenchmark(int numberOfObjects, int numberOfIterations, int numberOfThreads,
            NumberFactory.NumberType numberType, int writerBufferSize, String inputFileName)
            throws SimulationException {
        //        setNumberFactory(numberType);

        Container.setSimulation(new SimulationImpl());

        SimulationProperties simulationProperties = new SimulationProperties();
        simulationProperties.setNumberType(numberType);
        simulationProperties.setFormatVersion1Writer(new FormatVersion1ReaderWriter(getClass().getResource(
                inputFileName).getPath()));
        simulationProperties.getFormatVersion1Writer().readProperties(simulationProperties);

        simulationProperties.setNumberOfObjects(numberOfObjects);
        simulationProperties.setNumberOfIterations(numberOfIterations);
        simulationProperties.setNumberOfThreads(numberOfThreads);
        simulationProperties.setWriterBufferSize(writerBufferSize);
        simulationProperties.setBenchmarkMode(true);
        simulationProperties.setSaveToFile(false);
        simulationProperties.setForceCalculatorType(ForceCalculatorType.NEWTON_LAW_OF_GRAVITATION);

        Container.getSimulation().setProperties(simulationProperties);
        Container.setSimulationLogic(new SimulationLogicImpl());

        printConfiguration(simulationProperties, numberType);
        long durationInNanoseconds = Container.getSimulation().startSimulation();

        System.out.print("Total time: " + "\t" + (durationInNanoseconds / 1000000) + " ms");
        if (simulationProperties.getNumberOfThreads() == Utils.CORES) {
            System.out.print(" <<<<<<");
        }
        System.out.println();

    }

    private void printConfiguration(SimulationProperties properties, NumberFactory.NumberType numberType) {
        System.out.println("\n\nJRE version: " + System.getProperty("java.specification.version"));
        System.out.println("JVM  implementation name: " + System.getProperty("java.vm.name"));
        //        System.out.println("JVM implementation version: " + System.getProperty("java.vm.version"));
        //        System.out.println("The Name of JIT compiler to use: " + System.getProperty("java.compiler"));

        System.out.println("Free memory (Mbytes): " + Runtime.getRuntime().freeMemory() / (1024 * 1024));

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        System.out.println("Maximum memory (Mbytes): "
                + (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / (1024 * 1024)));

        /* Total memory currently available to the JVM */
        System.out.println("Total memory available to JVM (Mbytes): " + Runtime.getRuntime().totalMemory()
                / (1024 * 1024));

        System.out.println("The name of OS name: " + System.getProperty("os.name"));
        System.out.println("The version of OS: " + System.getProperty("os.version"));
        System.out.println("The OS architecture: " + System.getProperty("os.arch"));
        System.out.println("Host machine native word size: " + System.getProperty("sun.arch.data.model"));

        System.out.println("Number of cores: " + Utils.CORES);
        System.out.println("Number of runnig threads: " + properties.getNumberOfThreads());
        System.out.println("Number of objects: " + properties.getNumberOfObjects());
        System.out.println("Number of iterations: " + properties.getNumberOfIterations());
        System.out.println("Number implementation: " + numberType);
        System.out.println("Writer buffer size: " + properties.getWriterBufferSize());
    }

    //    private void setFactory(NumberFactory numberFactory) {
    //        Class clazz = NumberFactoryProxy.class;
    //
    //        Field factoryField;
    //        try {
    //            factoryField = clazz.getDeclaredField("factory");
    //            factoryField.setAccessible(true);
    //            factoryField.set(null, numberFactory);
    //        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //
    //
    //    }
    //    private void setConstants() {
    //        Class clazz = Number.class;
    //
    //        Field factoryField;
    //        try {
    //            //            factoryField = clazz.getDeclaredField("ZERO");
    //
    //            setFinalStatic(clazz.getDeclaredField("ZERO"), NumberFactoryProxy.createNumber(0));
    //            setFinalStatic(clazz.getDeclaredField("ONE"), NumberFactoryProxy.createNumber(1));
    //            setFinalStatic(clazz.getDeclaredField("TWO"), NumberFactoryProxy.createNumber(2));
    //            setFinalStatic(clazz.getDeclaredField("THREE"), NumberFactoryProxy.createNumber(3));
    //            Number THREE = NumberFactoryProxy.createNumber(3);
    //            setFinalStatic(clazz.getDeclaredField("FOUR"), NumberFactoryProxy.createNumber(4));
    //            Number FOUR = NumberFactoryProxy.createNumber(4);
    //            setFinalStatic(clazz.getDeclaredField("RATIO_FOUR_THREE"), FOUR.divide(THREE));
    //
    //
    //            //            factoryField.setAccessible(true);
    //            //            factoryField.setInt(factoryField, factoryField.getModifiers() & ~Modifier.FINAL);
    //            //            factoryField.set(null, NumberFactoryProxy.createNumber(0));
    //            //
    //            //            factoryField = clazz.getDeclaredField("ONE");
    //            //            factoryField.setAccessible(true);
    //            //            factoryField.set(null, NumberFactoryProxy.createNumber(1));
    //            //
    //            //            factoryField = clazz.getDeclaredField("TWO");
    //            //            factoryField.setAccessible(true);
    //            //            factoryField.set(null, NumberFactoryProxy.createNumber(2));
    //            //
    //            //            factoryField = clazz.getDeclaredField("THREE");
    //            //            factoryField.setAccessible(true);
    //            //            factoryField.set(null, NumberFactoryProxy.createNumber(3));
    //            //            Number THREE = NumberFactoryProxy.createNumber(3);
    //            //
    //            //            factoryField = clazz.getDeclaredField("FOUR");
    //            //            factoryField.setAccessible(true);
    //            //            factoryField.set(null, NumberFactoryProxy.createNumber(4));
    //            //            Number FOUR = NumberFactoryProxy.createNumber(4);
    //            //
    //            //            factoryField = clazz.getDeclaredField("RATIO_FOUR_THREE");
    //            //            factoryField.setAccessible(true);
    //            //            factoryField.set(null, FOUR.divide(THREE));
    //
    //        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException /*| IllegalAccessException*/e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //    }
    //    private void setNumberFactory(NumberFactory.NumberType numberType) {
    //        switch (numberType) {
    //            case DOUBLE:
    //                setFactory(new DoubleNumberFactory());
    //                setConstants();
    //                break;
    //            case BIG_DECIMAL:
    //                setFactory(new BigDecimalNumberFactory());
    //                setConstants();
    //                break;
    //            default:
    //                setFactory(new DoubleNumberFactory());
    //                setConstants();
    //                break;
    //        }
    //    }
    //    static void setFinalStatic(Field field, Object newValue) {
    //        field.setAccessible(true);
    //
    //        Field modifiersField;
    //        try {
    //            modifiersField = Field.class.getDeclaredField("modifiers");
    //
    //            modifiersField.setAccessible(true);
    //            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    //
    //            field.set(null, newValue);
    //        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //    }
    //     public static void main(String args[]) throws Exception {
    //        setFinalStatic(Boolean.class.getField("FALSE"), true);
    //
    //        System.out.format("Everything is %s", false); // "Everything is true"
    //     }
}
