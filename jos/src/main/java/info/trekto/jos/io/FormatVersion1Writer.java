/**
 * 
 */
package info.trekto.jos.io;

import info.trekto.jos.model.SimulationObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trayan Momkov
 * @date 18 Mar 2016
 */
public class FormatVersion1Writer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    FileOutputStream fileStream;
    BufferedWriter writer;

    /**
     * 
     */
    public FormatVersion1Writer(String file) {
        Charset charset = Charset.forName("UTF-8");
        try {
            writer = Files.newBufferedWriter(new File(file).toPath(), charset);
        } catch (IOException e) {
            logger.error("Cannot open file " + file, e);
        }
    }

    public void appendToFile(SimulationObject simulationObject) {
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

        try {
            writer.write("x = \t\t" + simulationObject.getX() + "\n");
            writer.write("y = \t\t" + simulationObject.getY() + "\n");
            writer.write("speed x = \t" + simulationObject.getSpeed().getX() + "\n");
            writer.write("speed y = \t" + simulationObject.getSpeed().getY() + "\n");
            writer.write("mag = \t\t" + simulationObject.calculateSpeedMagnitude() + "\n");
            writer.write("mass = \t\t" + simulationObject.getMass() + "\n");
            writer.write("radius = \t" + simulationObject.getRadius() + "\n");
            writer.write("color R = \t" + simulationObject.getColor().getR() + "\n");
            writer.write("color G = \t" + simulationObject.getColor().getG() + "\n");
            writer.write("color B = \t" + simulationObject.getColor().getB() + "\n");
            writer.write("is static = \t" + simulationObject.isMotionless() + "\n");
            writer.write("label= \t\t'" + simulationObject.getLabel() + "'" + "\n");
            writer.write("\n");
        } catch (IOException e) {
            logger.error("Cannot write to file ", e);
        }



    }
}
