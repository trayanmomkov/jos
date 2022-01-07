package info.trekto.jos;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.impl.SimulationForkJoinImpl;
import info.trekto.jos.core.impl.SimulationLogicImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.exceptions.SimulationException;
import info.trekto.jos.formulas.ForceCalculator.ForceCalculatorType;
import info.trekto.jos.io.FormatVersion1ReaderWriter;
import info.trekto.jos.numbers.NumberFactory;
import info.trekto.jos.util.Utils;

import static info.trekto.jos.numbers.NumberFactory.NumberType.BIG_DECIMAL;
import static info.trekto.jos.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.util.Utils.CORES;

/**
 * @author Trayan Momkov
 * @date 31 Mar 2016
 */
public class Benchmark {

    public static void main(String[] args) throws SimulationException {
        Benchmark benchmark = new Benchmark();
        // int numberOfObjects = 6400;
        // int numberOfObjects = 12800;
//        int numberOfObjects = 400;
        // String inputFileName = "/PSC_5_6400_objects_RUN";
        // String inputFileName = "/PSC_5_12800_objects_RUN";
        String inputFileName = "/PSC_5_10_objects_Java2D_RUN";
        
        if (args.length == 0) {
            System.err.println("Missing input file. Please pass it as program argument.");
            return;
        }
        inputFileName = args[0];

        /* Double */
        benchmark.runBenchmark(new SimulationForkJoinImpl(), 1, DOUBLE, 0, inputFileName);
        benchmark.runBenchmark(new SimulationForkJoinImpl(), 1, DOUBLE, 0, inputFileName);
        if (CORES > 2) {
            benchmark.runBenchmark(new SimulationForkJoinImpl(), CORES / 2, DOUBLE, 0, inputFileName);
        }
        benchmark.runBenchmark(new SimulationForkJoinImpl(), CORES, DOUBLE, 0, inputFileName);
        benchmark.runBenchmark(new SimulationForkJoinImpl(), CORES * 2, DOUBLE, 0, inputFileName);

        /* BigDecimal faster in JRE 1.8 */
        benchmark.runBenchmark(new SimulationForkJoinImpl(), 1, BIG_DECIMAL, 0, inputFileName);
        if (CORES > 2) {
            benchmark.runBenchmark(new SimulationForkJoinImpl(), CORES / 2, BIG_DECIMAL, 0, inputFileName);
        }
        benchmark.runBenchmark(new SimulationForkJoinImpl(), CORES, BIG_DECIMAL, 0, inputFileName);
        benchmark.runBenchmark(new SimulationForkJoinImpl(), CORES * 2, BIG_DECIMAL, 0, inputFileName);
        benchmark.runBenchmark(new SimulationForkJoinImpl(), 1, DOUBLE, 0, inputFileName);
    }

    private void runBenchmark(Simulation simulation, int numberOfThreads, NumberFactory.NumberType numberType, int writerBufferSize,
                              String inputFileName) throws SimulationException {
        Container.setSimulation(simulation);

        SimulationProperties simulationProperties = FormatVersion1ReaderWriter.readProperties(inputFileName);
        simulationProperties.setNumberType(numberType);

        simulationProperties.setNumberOfThreads(numberOfThreads);
        simulationProperties.setWriterBufferSize(writerBufferSize);
        simulationProperties.setBenchmarkMode(true);
        simulationProperties.setSaveToFile(false);
        simulationProperties.setForceCalculatorType(ForceCalculatorType.NEWTON_LAW_OF_GRAVITATION);

        Container.setProperties(simulationProperties);
        Container.setSimulationLogic(new SimulationLogicImpl());
        
        Utils.printConfiguration(simulationProperties);
        
        long durationInNanoseconds = Container.getSimulation().startSimulation();

        System.out.print("Total time: " + "\t" + (durationInNanoseconds / 1000000) + " ms");
        if (simulationProperties.getNumberOfThreads() == CORES) {
            System.out.print(" <<<<<<");
        }
        Utils.log();
    }

    // private void setFactory(NumberFactory numberFactory) {
    // Class clazz = NumberFactoryProxy.class;
    //
    // Field factoryField;
    // try {
    // factoryField = clazz.getDeclaredField("factory");
    // factoryField.setAccessible(true);
    // factoryField.set(null, numberFactory);
    // } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    //
    // }
    // private void setConstants() {
    // Class clazz = Number.class;
    //
    // Field factoryField;
    // try {
    // // factoryField = clazz.getDeclaredField("ZERO");
    //
    // setFinalStatic(clazz.getDeclaredField("ZERO"), NumberFactoryProxy.createNumber(0));
    // setFinalStatic(clazz.getDeclaredField("ONE"), NumberFactoryProxy.createNumber(1));
    // setFinalStatic(clazz.getDeclaredField("TWO"), NumberFactoryProxy.createNumber(2));
    // setFinalStatic(clazz.getDeclaredField("THREE"), NumberFactoryProxy.createNumber(3));
    // Number THREE = NumberFactoryProxy.createNumber(3);
    // setFinalStatic(clazz.getDeclaredField("FOUR"), NumberFactoryProxy.createNumber(4));
    // Number FOUR = NumberFactoryProxy.createNumber(4);
    // setFinalStatic(clazz.getDeclaredField("RATIO_FOUR_THREE"), FOUR.divide(THREE));
    //
    //
    // // factoryField.setAccessible(true);
    // // factoryField.setInt(factoryField, factoryField.getModifiers() & ~Modifier.FINAL);
    // // factoryField.set(null, NumberFactoryProxy.createNumber(0));
    // //
    // // factoryField = clazz.getDeclaredField("ONE");
    // // factoryField.setAccessible(true);
    // // factoryField.set(null, NumberFactoryProxy.createNumber(1));
    // //
    // // factoryField = clazz.getDeclaredField("TWO");
    // // factoryField.setAccessible(true);
    // // factoryField.set(null, NumberFactoryProxy.createNumber(2));
    // //
    // // factoryField = clazz.getDeclaredField("THREE");
    // // factoryField.setAccessible(true);
    // // factoryField.set(null, NumberFactoryProxy.createNumber(3));
    // // Number THREE = NumberFactoryProxy.createNumber(3);
    // //
    // // factoryField = clazz.getDeclaredField("FOUR");
    // // factoryField.setAccessible(true);
    // // factoryField.set(null, NumberFactoryProxy.createNumber(4));
    // // Number FOUR = NumberFactoryProxy.createNumber(4);
    // //
    // // factoryField = clazz.getDeclaredField("RATIO_FOUR_THREE");
    // // factoryField.setAccessible(true);
    // // factoryField.set(null, FOUR.divide(THREE));
    //
    // } catch (NoSuchFieldException | SecurityException | IllegalArgumentException /*| IllegalAccessException*/e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // private void setNumberFactory(NumberFactory.NumberType numberType) {
    // switch (numberType) {
    // case DOUBLE:
    // setFactory(new DoubleNumberFactory());
    // setConstants();
    // break;
    // case BIG_DECIMAL:
    // setFactory(new BigDecimalNumberFactory());
    // setConstants();
    // break;
    // default:
    // setFactory(new DoubleNumberFactory());
    // setConstants();
    // break;
    // }
    // }
    // static void setFinalStatic(Field field, Object newValue) {
    // field.setAccessible(true);
    //
    // Field modifiersField;
    // try {
    // modifiersField = Field.class.getDeclaredField("modifiers");
    //
    // modifiersField.setAccessible(true);
    // modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    //
    // field.set(null, newValue);
    // } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // public static void main(String args[]) throws Exception {
    // setFinalStatic(Boolean.class.getField("FALSE"), true);
    //
    // System.out.format("Everything is %s", false); // "Everything is true"
    // }
}
