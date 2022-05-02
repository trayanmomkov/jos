package info.trekto.jos.core.impl;

import info.trekto.jos.core.Simulation;
import info.trekto.jos.core.impl.arbitrary_precision.SimulationAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.impl.arbitrary_precision.SimulationLogicAP.calculateVolumeFromRadius;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.util.Utils.info;

public class SimulationGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SimulationGenerator.class);

    public static void generateObjects(SimulationProperties properties) {
        String filename = System.getProperty("user.home") + File.separator
                + new SimpleDateFormat("yyyy-MMM-dd_HH-mm-ss").format(new Date()) + ".json.gz";
        properties.setOutputFile(filename);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        List<SimulationObject> objects = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        int n = properties.getNumberOfObjects();
        double areaForObject = width * height / (double) n;
        double areaSide = Math.sqrt(areaForObject);
        int generatedObjects = 0;

        long horizontalZones = Math.round(width / areaSide);
        long verticalZones = Math.round(n / (double) horizontalZones + 0.5);

        outerloop:
        for (int i = 0; i < horizontalZones; i++) {
            for (int j = 0; j < verticalZones; j++) {
                double radius = random.nextDouble() * areaSide / 10.0;
                SimulationObject o = C.createNewSimulationObject();

                o.setX(New.num(i * areaSide + radius * 1.1 + (random.nextDouble() * (areaSide - 2 * radius * 1.1)) - width / 2.0));
                o.setY(New.num(j * areaSide + radius * 1.1 + (random.nextDouble() * (areaSide - 2 * radius * 1.1)) - height / 2.0));
                o.setZ(ZERO);
                o.setRadius(New.num(radius));
                o.setSpeed(new TripleNumber(New.num((random.nextDouble() - 0.5) * 10), New.num((random.nextDouble() - 0.5) * 10), ZERO));
                o.setColor(Color.BLUE.getRGB());

                // density = mass / volume
                o.setMass(calculateVolumeFromRadius(o.getRadius()).multiply(New.num(100_000_000_000L)));
                o.setId(String.valueOf(generatedObjects));

                objects.add(o);
                generatedObjects++;
                info(logger, generatedObjects + " objects generated.");
                if (generatedObjects == n) {
                    break outerloop;
                }
            }
        }
        properties.setInitialObjects(objects);
    }
}
