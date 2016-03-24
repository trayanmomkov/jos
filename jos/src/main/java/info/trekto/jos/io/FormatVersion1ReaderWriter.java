/**
 * 
 */
package info.trekto.jos.io;

import info.trekto.jos.Container;
import info.trekto.jos.formulas.ScientificConstants;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 18 Mar 2016
 */
public class FormatVersion1ReaderWriter {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // private FileOutputStream fileOutputStream;
    private BufferedWriter writer;

    // private FileInputStream fileInputStream;
    private BufferedReader reader;

    /**
     * 
     */
    public FormatVersion1ReaderWriter(String inputFile, String outputFile) {
        Charset charset = Charset.forName("UTF-8");
        try {
            reader = Files.newBufferedReader(new File(inputFile).toPath(), charset);
            writer = Files.newBufferedWriter(new File(outputFile).toPath(), charset);
        } catch (IOException e) {
            logger.error("Cannot open file " + outputFile, e);
        }
    }

    public void appendObjectsToFile(List<SimulationObject> simulationObjects) {
        try {
            // fileToSave<<"============================ " << N << " | " << simulationProperties.cycleCounter <<
            // " (objects | cycle) ============================\n\n";
            writer.write("============================ " + Container.getSimulation().getProperties().getN() + " | "
                    + Container.getSimulation().getCurrentIterationNumber()
                    + " (objects | cycle) ============================\n\n");

            for (Iterator iterator = simulationObjects.iterator(); iterator.hasNext();) {
                SimulationObject simulationObject = (SimulationObject) iterator.next();
                appendObjectToFile(simulationObject);
            }
        } catch (IOException e) {
            logger.error("Cannot write to file ", e);
        }
    }

    public void appendObjectToFile(SimulationObject simulationObject) throws IOException {
        /**
         * f<<"x = \t\t"<< setprecision(VISUALISATION_PRECISION) << x << endl;
         * f<<"y = \t\t"<< setprecision(VISUALISATION_PRECISION) << y << endl;
         * f<<"speed x = \t"<< setprecision(VISUALISATION_PRECISION) << speed.x << endl;
         * f<<"speed y = \t"<< setprecision(VISUALISATION_PRECISION) << speed.y << endl;
         * f<<"mag = \t\t"<< setprecision(VISUALISATION_PRECISION) << getSpeedMagnitude() << endl;
         * f<<"mass = \t\t"<< setprecision(VISUALISATION_PRECISION) << mass << endl;
         * f<<"radius = \t"<< setprecision(VISUALISATION_PRECISION) << radius << endl;
         * f<<"color R = \t"<<(int)color.r<<endl;
         * f<<"color G = \t"<<(int)color.g<<endl;
         * f<<"color B = \t"<<(int)color.b<<endl;
         * f<<"is static = \t"<<statical<<endl;
         * f<<"label= \t\t'"<<label<<"'"<<endl;
         * f<<endl;
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

    public void endFile() {
        try {
            writer.write("END\n");
            writer.close();
        } catch (IOException e) {
            logger.error("Error while closing file.", e);
        } finally {
        }
    }

    public SimulationObject readObjectFromFile() {
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

        String line;
        Matcher matcher;
        String spaceAndNumber = "\\s+([+-e\\d\\.]+)";

        try {
            simulationObject.setX(New.num(Pattern.compile("x =" + spaceAndNumber).matcher(reader.readLine()).group(1)));
            simulationObject.setY(New.num(Pattern.compile("y =" + spaceAndNumber).matcher(reader.readLine()).group(1)));
            simulationObject.setSpeed(new TripleNumber(
                    New.num(Pattern.compile("speed x =" + spaceAndNumber).matcher(reader.readLine()).group(1)),
                    New.num(Pattern.compile("speed y =" + spaceAndNumber).matcher(reader.readLine()).group(1)),
                    Number.ZERO));
            reader.readLine();
            /** We don't need megnitude */

            // If we want to calculate mass for solid sphere from its radius
            // Volume of sphere = 4/3 * pi * r^3, where r is radius
            // Consider density is 1
            // For 2D we decrease radius for better looking, but not correct looking

            // ten = 10;
            // temp_radius = radius / ten;
            // if (mass == -1)
            // mass = RATIO_FOUR_THREE * PI * pow(radius / ten, 3);
            String mass = Pattern.compile("mass =" + spaceAndNumber).matcher(reader.readLine()).group(1);
            if ("-1".equals(mass)) {
                simulationObject
                        .setMass(Number.RATIO_FOUR_THREE.multiply(ScientificConstants.PI).multiply(
                                simulationObject.getRadius().pow(3)));
            } else {
                simulationObject
                        .setMass(New.num(mass));
            }
            simulationObject.setRadius(New.num(Pattern.compile("radius =" + spaceAndNumber).matcher(reader.readLine())
                    .group(1)));
            simulationObject
                    .setColor(new TripleInt(
                            Integer.valueOf(Pattern.compile("color R =" + spaceAndNumber).matcher(reader.readLine())
                                    .group(1)),
                            Integer.valueOf(Pattern.compile("color G =" + spaceAndNumber).matcher(reader.readLine())
                                    .group(1)),
                            Integer.valueOf(Pattern.compile("color B =" + spaceAndNumber).matcher(reader.readLine())
                                    .group(1))));
            simulationObject.setMotionless(Boolean.valueOf(Pattern.compile("is static =" + spaceAndNumber)
                    .matcher(reader.readLine()).group(1)));
            simulationObject.setLabel(Pattern.compile("label=" + spaceAndNumber).matcher(reader.readLine()).group(1));



            reader.readLine();
        } catch (IOException e) {
            logger.error("Cannot read object from file", e);
        }

        return simulationObject;
    }
}
