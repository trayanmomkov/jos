package info.trekto.jos.util;

import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.io.JsonReaderWriter;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.Number;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static info.trekto.jos.formulas.CommonFormulas.calculateVolumeFromRadius;
import static info.trekto.jos.formulas.ForceCalculator.InteractingLaw.NEWTON_LAW_OF_GRAVITATION;
import static info.trekto.jos.numbers.New.ZERO;
import static info.trekto.jos.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.Utils.collisionExists;

public class SimulationGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SimulationGenerator.class);

    public static void main(String[] args) {
        String filename = "/home/john/384_objects.json";
        if (args.length > 0) {
            filename = args[0];
        }

        String[] splitFileName = filename.split("\\.");
        String head = splitFileName[splitFileName.length - 2];
        String tail = splitFileName[splitFileName.length - 1];

        C.prop = new SimulationProperties();

        C.prop.setNumberType(DOUBLE);
        C.prop.setPrecision(32);
        C.prop.setScale(16);

        createNumberFactory(C.prop.getNumberType(), C.prop.getPrecision(), C.prop.getScale());

        C.prop.setInteractingLaw(NEWTON_LAW_OF_GRAVITATION);
        C.prop.setNumberOfIterations(100_000);
        C.prop.setSecondsPerIteration(New.num("0.001"));
        C.prop.setNumberOfObjects(384);
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
        Random random = new Random(897L);

        for (int i = 0; i < prop.getNumberOfObjects(); i++) {
            SimulationObject o = new SimulationObjectImpl();

            o.setX(New.num(random.nextDouble()).subtract(New.num("0.5")).multiply(New.num(1000)));
            o.setY(New.num(random.nextDouble()).subtract(New.num("0.5")).multiply(New.num(1000)));
            o.setZ(ZERO);
            Number radiusMultiplier = i % 7 == 0 ? New.num(4) : New.num(1.5);
            o.setRadius(New.num(random.nextDouble()).multiply(radiusMultiplier));

            outerloop:
            while (collisionExists(objects)) {
                for (int j = 0; j < 100; j++) {
                    o.setX(New.num(random.nextDouble()).subtract(New.num("0.5")).multiply(New.num(1000)));
                    if (!collisionExists(objects)) {
                        break outerloop;
                    }
                }
                for (int j = 0; j < 100 && collisionExists(objects); j++) {
                    o.setY(New.num(random.nextDouble()).subtract(New.num("0.5")).multiply(New.num(1000)));
                    if (!collisionExists(objects)) {
                        break outerloop;
                    }
                }

//                System.out.println("████████");
//                for (SimulationObject object : objects) {
//                    System.out.println("(" + object.getX() + ", " + object.getY() + ") " + object.getRadius());
//                }
                
                for (int j = 1; j <= 100 && collisionExists(objects); j++) {
                    o.setRadius(New.num(random.nextDouble()).multiply(New.num(5).divide(New.num(j))));
                    if (!collisionExists(objects)) {
                        break outerloop;
                    }
                }
            }

            o.setSpeed(new TripleNumber(New.num(random.nextDouble()).subtract(New.num("0.5")).multiply(New.num(10)),
                                        New.num(random.nextDouble()).subtract(New.num("0.5")).multiply(New.num(10)),
                                        ZERO));

            o.setColor(new TripleInt(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            
            // density = mass / volume
            o.setMass(New.num(random.nextDouble()).multiply(calculateVolumeFromRadius(o.getRadius()).multiply(New.num(100_000_000_000_000L))));
            o.setId(String.valueOf(i));

            objects.add(o);
            System.out.println(i + " objects generated.");
        }
        return objects;
    }
}
