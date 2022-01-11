/**
 *
 */
package info.trekto.jos.io;

import info.trekto.jos.Container;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.formulas.ScientificConstants;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.NumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Trayan Momkov
 * @date 18 Mar 2016
 */
public class FormatVersion1ReaderWriter implements ReaderWriter {
    public static final String keyValueSeparator = ":[\\s\\t]+";
    private static final Logger logger = LoggerFactory.getLogger(FormatVersion1ReaderWriter.class);
    private BufferedWriter writer;

    private static String match(String pattern, String input) {
        return match(pattern, input, 1);
    }

    private static String match(String pattern, String input, int groupNumber) {
        Matcher m = Pattern.compile(pattern).matcher(input);
        if (m.matches() && m.groupCount() >= groupNumber) {
            return m.group(groupNumber);
        } else {
            logger.error("Cannot match pattern: '" + pattern + "' in string: '" + input + "' or there is no group: " + groupNumber);
            return "";
        }
    }

    @Override
    public void writeProperties(SimulationProperties properties, String outputFilePath) {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public void initWriter(SimulationProperties properties, String inputFilePath) {
        try {
            if (Container.runtimeProperties.getWriterBufferSize() == 0) {
                writer = Files.newBufferedWriter(new File(properties.getOutputFile()).toPath(), StandardCharsets.UTF_8);
            } else {
                writer = new BufferedWriter(Files.newBufferedWriter(new File(properties.getOutputFile()).toPath(), StandardCharsets.UTF_8),
                                            Container.runtimeProperties.getWriterBufferSize());
            }
        } catch (IOException e) {
            logger.info("Cannot open output file " + inputFilePath, e);
        }
    }

    /**
     *
     */
//    public FormatVersion1ReaderWriter(String inputFile) {
//        try {
//            this.inputFile = new File(inputFile);
//            reader = Files.newBufferedReader(this.inputFile.toPath(), charset);
//            // writer = Files.newBufferedWriter(new File(outputFile).toPath(), charset);
//        } catch (IOException e) {
//            logger.info("Cannot open file " + inputFile, e);
//        }
//    }

    // public FormatVersion1ReaderWriter(InputStream inputStream) {
    // try {
    // this.inputFile = new File(inputFile);
    // reader = Files.newBufferedReader(this.inputFile.toPath(), charset);
    // reader = new BufferedInputStream(inputStream);
    // getClass().getResource(name)
    // // writer = Files.newBufferedWriter(new File(outputFile).toPath(), charset);
    // } catch (IOException e) {
    // logger.error("Cannot open file " + inputFile, e);
    // }
    // }
    // public void flushToDisk() {
    // try {
    // writer.flush();
    // } catch (IOException e) {
    // logger.error("Error during flush to disk", e);
    // }
    // }
    public void appendObjectsToFile(List<SimulationObject> simulationObjects) {
        try {
            // fileToSave<<"============================ " << N << " | " << simulationProperties.cycleCounter <<
            // " (objects | cycle) ============================\n\n";
            writer.write("============================ " + Container.properties.getNumberOfObjects() + " | "
                                 + Container.simulation.getCurrentIterationNumber()
                                 + " (objects | cycle) ============================\n\n");

            for (Object element : simulationObjects) {
                SimulationObject simulationObject = (SimulationObject) element;
                appendObjectToFile(simulationObject);
            }
        } catch (IOException e) {
            logger.info("Cannot write to file ", e);
        }
    }

    public void appendObjectToFile(SimulationObject simulationObject) throws IOException {
        /**
         * f<<"x = \t\t"<< setprecision(VISUALISATION_PRECISION) << x << endl; f<<"y = \t\t"<<
         * setprecision(VISUALISATION_PRECISION) << y << endl; f<<"speed x = \t"<<
         * setprecision(VISUALISATION_PRECISION) << speed.x << endl; f<<"speed y = \t"<<
         * setprecision(VISUALISATION_PRECISION) << speed.y << endl; f<<"mag = \t\t"<<
         * setprecision(VISUALISATION_PRECISION) << getSpeedMagnitude() << endl; f<<"mass = \t\t"<<
         * setprecision(VISUALISATION_PRECISION) << mass << endl; f<<"radius = \t"<<
         * setprecision(VISUALISATION_PRECISION) << radius << endl; f<<"color R =
         * \t"<<(int)color.r<<endl; f<<"color G = \t"<<(int)color.g<<endl; f<<"color B =
         * \t"<<(int)color.b<<endl; f<<"is static = \t"<<statical<<endl; f<<"label=
         * \t\t'"<<label<<"'"<<endl; f<<endl;
         */

        writer.write("x = \t\t" + simulationObject.getX() + "\n");
        writer.write("y = \t\t" + simulationObject.getY() + "\n");
        writer.write("speed x = \t" + simulationObject.getSpeed().getX() + "\n");
        writer.write("speed y = \t" + simulationObject.getSpeed().getY() + "\n");
        writer.write("mag = \t\t" + simulationObject.calculateSpeedMagnitude() + "\n");
        writer.write("mass = \t\t" + simulationObject.getMass() + "\n");
        writer.write("radius = \t" + simulationObject.getRadius() + "\n");
        if (simulationObject.getColor() != null) {
            writer.write("color R = \t" + simulationObject.getColor().getR() + "\n");
            writer.write("color G = \t" + simulationObject.getColor().getG() + "\n");
            writer.write("color B = \t" + simulationObject.getColor().getB() + "\n");
        } else {
            writer.write("color R = \t0\n");
            writer.write("color G = \t0\n");
            writer.write("color B = \t255\n");
        }
        writer.write("is static = \t" + ((simulationObject.isMotionless()) ? '1' : '0') + "\n");
        writer.write("label= \t\t'" + simulationObject.getLabel() + "'" + "\n");
        writer.write("\n");
    }

//    private void setDefaultValues(SimulationProperties properties) {
//        properties.setForceCalculatorType(ForceCalculatorType.NEWTON_LAW_OF_GRAVITATION);
//    }

    public void endFile() {
        try {
            writer.write("END\n");
            writer.close();
        } catch (IOException e) {
            logger.info("Error while closing file.", e);
        }
    }

    public SimulationProperties readProperties(String inputFilePath) {
        return readProperties(inputFilePath, null);
    }

    public SimulationProperties readProperties(String inputFilePath, Charset charset) {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        BufferedReader reader;
        try {
            File inputFile = new File(inputFilePath);
            reader = Files.newBufferedReader(inputFile.toPath(), charset);
            // writer = Files.newBufferedWriter(new File(outputFile).toPath(), charset);
        } catch (IOException e) {
            logger.info("Cannot open file " + inputFilePath, e);
            return null;
        }

        SimulationProperties properties = new SimulationProperties();

        try {

            /* Numbers type */
            properties.setNumberType(NumberFactory.NumberType.valueOf(match("numbers type" + keyValueSeparator + "([\\w_]+).*",
                                                                            reader.readLine())));

            // precision: 32 Arithmetic precision in digits after point. 0 = infinite precision; -1 = native floating
            // point type double
            properties.setPrecision(Integer.valueOf(match("precision" + keyValueSeparator + "([\\d]+).*",
                                                          reader.readLine())));

            /* At this point we know numbers type and precision so we must create NumberFactory
             * because the rest of the method create numbers. */
            properties.createNumberFactory();

            // window size: 1280 1024 in pixels
            reader.readLine();
            reader.readLine();

            // bounce from screen: 0 (1 = true, 0 = false) Whether objects bounce from the window borders
            reader.readLine();

            // bounce from each other: 0 (1 = true, 0 = false) Whether objects bounce from each other
            reader.readLine();

            // gravity: 1 (1 = true, 0 = false) Whether gravitation law is in force
            reader.readLine();

            // absorbtion: 1 (1 = true, 0 = false) Whether one object can swallow (absorb) another
            reader.readLine();

            // restitution: 1 [0, 1] perfectly inelastic = 0 to 1 = perfectly elastic Coeficient of objects elasticity.
            reader.readLine();
            reader.readLine();

            // number of cycles: -1 -1 = infinite loop
            properties.setNumberOfIterations(Integer.valueOf(match("number of cycles" + keyValueSeparator + "([-\\d]+).*",
                                                                   reader.readLine())));

            // seconds per cycle: 10
            properties.setNanoSecondsPerIteration(Integer.valueOf(match("seconds per cycle" + keyValueSeparator + "([-\\d]+).*",
                                                                        reader.readLine())) * ScientificConstants.NANOSECONDS_IN_ONE_SECOND);

            // simulation time: -1 (Depend from numberOfCycles and secondsPerCycle and vice versa, -1 mean no value)
            reader.readLine();

            // real time: 1 (1 = true, 0 = false) Show simulation in real time
            properties.setRealTimeVisualization("1".equals(match("real time" + keyValueSeparator + "([01]).*",
                                                                 reader.readLine())));

            // playing: 0 0 (1 = true, 0 = false) Whether to read data form file and play it; Second digit toggles
            // recording mode
            reader.readLine();

            // playing speed: 25 in real time mode indicates 1 per how many cycle to be saved in file; in playing mode
            // indicates how many cycles to be skipped in each visualisation step
            properties.setPlayingSpeed(Integer.valueOf(match("playing speed" + keyValueSeparator + "([\\d]+).*",
                                                             reader.readLine())));

            // sleep mseconds: 0 miliseconds, how long to sleep for every amount of cycles to prevent system overheat
            // for long simulations (0 = no sleep)
            reader.readLine();

            // cycles before sleep: 0 number of cycles before sleep to prevent system overheat for long simulations
            reader.readLine();
            reader.readLine();

            // 3D: 0 (1 = 3D, 0 = 2D) Whether visualization to be 3D or 2D.
            reader.readLine();

            // scene translation: 0 0 2.7e-6 (x, y, z)
            reader.readLine();

            // scene rotation: 0 0 0 0 1 0 0 292 x, y, z, angle axis of rotation, vector and angle of rotation in
            // degrees
            reader.readLine();

            // perspective: 45 0 0.1 100 fovy - field of view angle, in degrees, in the y direction; aspect - aspect
            // ratio, 0 = default; zNear - Distance from the viewer to the near clipping plane (>0); zFar - Distance
            // from the viewer to the far clipping plane (>0)
            reader.readLine();

            // interaction plane: 2 (1 = xz; 2 = xy, 3 = yz)
            reader.readLine();
            reader.readLine();

            // trajectory length: 0 Number of point remembered for trajectory. 0 = no trajectory is drawn; -1 = no
            // limit, keep all points
            reader.readLine();

            // trajectory width: 1 Width of lines linking trajectory points
            reader.readLine();

            // showing labels: 0 Whether to show object labels.
            reader.readLine();
            reader.readLine();

            // saving data: 0 (1 = true, 0 = false)
            properties.setSaveToFile("1".equals(match("saving data" + keyValueSeparator + "([01]).*",
                                                      reader.readLine())));

            // output file name: simulations/PSC_5.out
            String outputFilename = match("output file name" + keyValueSeparator + "([^\\s]+).*", reader.readLine());
            if (outputFilename.equals("default")) {
                outputFilename = inputFilePath + ".out";
            }
            properties.setOutputFile(outputFilename);

            // number of objects: 399
            properties.setNumberOfObjects(Integer.valueOf(match("number of objects" + keyValueSeparator + "(\\d+).*",
                                                                reader.readLine())));
            reader.readLine();

            // Objects:
            reader.readLine();
            reader.readLine();

            properties.setInitialObjects(new ArrayList<>());
            for (int i = 0; i < properties.getNumberOfObjects(); i++) {
                properties.getInitialObjects().add(readObjectFromFile(reader));
            }
        } catch (IOException e) {
            logger.info("Cannot read properties from file", e);
        }

        return properties;
    }
    
    public static SimulationObject readObjectFromFile(BufferedReader reader) {
        SimulationObject simulationObject = new SimulationObjectImpl();

        // x = 648
        // y = 1.34e3
        // speed x = 1
        // speed y = 0
        // mag = 0
        // mass = 65
        // radius = 5
        // color R = 128
        // color G = 128
        // color B = 128
        // is static = 0
        // label= '1'
        String spaceAndNumber = "[\\s\\t]+([\\+-eE\\d\\.]+)";
        try {
            simulationObject.setX(New.num(match("x =" + spaceAndNumber, reader.readLine())));
            simulationObject.setY(New.num(match("y =" + spaceAndNumber, reader.readLine())));
            simulationObject.setZ(New.ZERO);
            simulationObject.setSpeed(new TripleNumber(
                    New.num(match("speed x =" + spaceAndNumber, reader.readLine())),
                    New.num(match("speed y =" + spaceAndNumber, reader.readLine())),
                    New.ZERO));
            reader.readLine();
            /**
             * We don't need megnitude
             */

            // If we want to calculate mass for solid sphere from its radius
            // Volume of sphere = 4/3 * pi * r^3, where r is radius
            // Consider density is 1
            // For 2D we decrease radius for better looking, but not correct looking
            // ten = 10;
            // temp_radius = radius / ten;
            // if (mass == -1)
            // mass = RATIO_FOUR_THREE * PI * pow(radius / ten, 3);
            String mass = match("mass =" + spaceAndNumber, reader.readLine());
            if ("-1".equals(mass)) {
                simulationObject
                        .setMass(New.RATIO_FOUR_THREE.multiply(ScientificConstants.PI).multiply(
                                simulationObject.getRadius().pow(3)));
            } else {
                simulationObject
                        .setMass(New.num(mass));
            }
            simulationObject.setRadius(New.num(match("radius =" + spaceAndNumber, reader.readLine())));
            simulationObject
                    .setColor(new TripleInt(
                            Integer.valueOf(match("color R =" + spaceAndNumber, reader.readLine())),
                            Integer.valueOf(match("color G =" + spaceAndNumber, reader.readLine())),
                            Integer.valueOf(match("color B =" + spaceAndNumber, reader.readLine()))));
            simulationObject
                    .setMotionless(Boolean.valueOf(match("is static =" + spaceAndNumber, reader.readLine())));
            simulationObject.setLabel(match("label=[\\s\\t]+(.+)", reader.readLine()));

            reader.readLine();
        } catch (IOException e) {
            logger.info("Cannot read object from file", e);
        }

        return simulationObject;
    }
}
