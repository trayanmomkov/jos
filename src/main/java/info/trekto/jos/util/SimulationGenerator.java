package info.trekto.jos.util;

import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static info.trekto.jos.util.Utils.collisionExists;

public class SimulationGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SimulationGenerator.class);

    public static void main(String[] args) {
        String filename = "/home/john/IdeaProjects/jos/src/test/resources/generated_400_profiling.json";
        if (args.length > 0) {
            filename = args[0];
        }
        
        String[] splitFileName = filename.split("\\.");
        String head = splitFileName[splitFileName.length - 2];
        String tail = splitFileName[splitFileName.length - 1];

        C.prop = new SimulationProperties();

        C.prop.setPrecision(32);
        C.prop.setScale(16);

        C.prop.setNumberOfIterations(1_000_000);
        C.prop.setSecondsPerIteration(0.001);
        C.prop.setNumberOfObjects(400);
        C.prop.setOutputFile(head + "_out." + tail);
        C.prop.setSaveToFile(true);
        C.prop.setRealTimeVisualization(true);
        C.prop.setBounceFromWalls(false);
        C.prop.setPlayingSpeed(1);
        C.prop.setInitialObjects(generateObjects(C.prop));

        new JsonReaderWriter().writeProperties(C.prop, filename);
    }

    public static List<SimulationObject> generateObjects(SimulationProperties prop) {
        List<SimulationObject> objects = new ArrayList<>();
        Random random = new Random(8976766707687L);

        for (int i = 0; i < prop.getNumberOfObjects(); i++) {
            SimulationObject o = new SimulationObjectImpl();

            do {
                o.setX((random.nextDouble() - 0.5) * 2000);
                o.setY((random.nextDouble() - 0.5) * 2000);
                o.setZ(0);
                o.setRadius(random.nextDouble() * 5);
            } while (collisionExists(objects));

            o.setSpeedX((random.nextDouble() - 0.5) * 10);
            o.setSpeedY((random.nextDouble() - 0.5) * 10);
            o.setSpeedZ(0);

            int rgb = random.nextInt(255);
            rgb = (rgb << 8) + random.nextInt(255);
            rgb = (rgb << 8) + random.nextInt(255);

            o.setColor(rgb);
            o.setMass(random.nextDouble() * 100_000_000_000_000L);
            o.setLabel(String.valueOf(i));

            objects.add(o);
            System.out.println(i + " objects generated.");
        }
        return objects;
    }
}
