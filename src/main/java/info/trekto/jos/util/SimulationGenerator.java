package info.trekto.jos.util;

import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.gui.MainForm;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static info.trekto.jos.core.impl.SimulationImpl.init;
import static info.trekto.jos.formulas.CommonFormulas.calculateVolumeFromRadius;
import static info.trekto.jos.formulas.ScientificConstants.DEG_TO_RAD;
import static info.trekto.jos.util.Utils.info;
import static java.lang.Math.*;

public class SimulationGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SimulationGenerator.class);

    public static void generateObjects(SimulationProperties prop, MainForm mainForm) {
        String filename = System.getProperty("user.home") + File.separator
                + new SimpleDateFormat("yyyy-MMM-dd_HH-mm-ss").format(new Date()) + ".json.gz";
        prop.setOutputFile(filename);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        List<SimulationObject> objects = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        int n = prop.getNumberOfObjects();
        double areaForObject = width * height / (double) n;
        double areaSide = sqrt(areaForObject);
        int generatedObjects = 0;

        long horizontalZones = round(width / areaSide);
        long verticalZones = round(n / (double) horizontalZones + 0.5);

        outerloop:
        for (int i = 0; i < horizontalZones; i++) {
            for (int j = 0; j < verticalZones; j++) {
                double radius = random.nextDouble() * areaSide / 10.0;
                SimulationObject o = new SimulationObjectImpl();

                o.setX(i * areaSide + radius * 1.1 + (random.nextDouble() * (areaSide - 2 * radius * 1.1)) - width / 2.0);
                o.setY(j * areaSide + radius * 1.1 + (random.nextDouble() * (areaSide - 2 * radius * 1.1)) - height / 2.0);
                o.setZ(0);
                o.setRadius(radius);
                o.setSpeedX((random.nextDouble() - 0.5) * 10);
                o.setSpeedY((random.nextDouble() - 0.5) * 10);
                o.setSpeedZ(0);
                o.setColor(Color.BLUE.getRGB());

                // density = mass / volume
                o.setMass(calculateVolumeFromRadius(o.getRadius()) * 100_000_000_000L);
                o.setId(String.valueOf(generatedObjects));

                objects.add(o);
                generatedObjects++;
                info(logger, generatedObjects + " objects generated.");
                if (generatedObjects == n) {
                    break outerloop;
                }
            }
        }
        prop.setInitialObjects(objects);
        init(prop);
        mainForm.refreshProperties(C.prop);
    }

    public static List<SimulationObject> generateComplexObject(double x, double y, double radius, double mass, String id, double rotationDirection,
                                                               double speedX, double speedY) {
        List<SimulationObject> objectParticles = new ArrayList<>();
        double object_number_factor = 0.4;    // bigger = less objects
        int object_radial_density = 1;    // bigger = less objects
        double exponential_speed_factor = 0.45;
        double exponential_mass_change_in_depth = -0.78;
        double particleRadius = 1;

        for (double r = radius; r >= particleRadius * 2 + 1; r -= object_radial_density) {
            double particlesInCurrentCircle = round(r / object_number_factor);
            double step = 360 / particlesInCurrentCircle;

            for (int ii = 0; ii < r / object_number_factor; ii++) {
                double i = ii * step;
                double radians = i * DEG_TO_RAD; //convert degrees into radians

                SimulationObject o = new SimulationObjectImpl();
                o.setX(cos(radians) * r * 5 + x);
                o.setY(sin(radians) * r * 5 + y);
                o.setSpeedX((cos(radians - 300) * pow(r, exponential_speed_factor)) * rotationDirection + speedX);
                o.setSpeedY((sin(radians - 300) * pow(r, exponential_speed_factor)) * rotationDirection + speedY);
                o.setMass(mass * pow(r, exponential_mass_change_in_depth)); 
                o.setRadius(particleRadius);
                o.setId(id + r + ii);
                objectParticles.add(o);
            }
        }
        return objectParticles;
    }
}
