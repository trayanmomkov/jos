package info.trekto.jos.core.impl;

import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
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
import static info.trekto.jos.core.Controller.JSON_GZIP_FILE_EXTENSION;
import static info.trekto.jos.core.impl.arbitrary_precision.SimulationLogicAP.calculateVolumeFromRadius;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.*;
import static info.trekto.jos.util.Utils.info;

public class SimulationGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SimulationGenerator.class);

    public static void generateObjects(SimulationProperties properties, boolean printInfo) {
        String filename = System.getProperty("user.home") + File.separator
                + new SimpleDateFormat("yyyy-MMM-dd_HH-mm-ss").format(new Date()) + JSON_GZIP_FILE_EXTENSION;
        properties.setOutputFile(filename);

        Rectangle maxWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int buffer = 50;
        double width = maxWindowBounds.getWidth() - buffer;
        double height = maxWindowBounds.getHeight() - buffer;

        if (height > width) {
            // Ignore vertical screens for simplicity.
            height = width;
        }

        double screenRatio = width / height;
        double screenTranslationX = width / 2.0;
        double screenTranslationY = height / 2.0;

        Random random = new Random(System.currentTimeMillis());
        int n = properties.getNumberOfObjects();
        double areaForObject = Math.floor(width * height / (double) n);
        double sideY = Math.sqrt(areaForObject / screenRatio);
        double sideX = sideY * screenRatio;

        long horizontalZones = 0;
        long verticalZones;
        do {
            if (horizontalZones != 0) {
                sideX -= 0.1;
                if (sideX < sideY) {
                    sideY = sideX / screenRatio;
                }
            }
            horizontalZones = Math.round(Math.floor(width / sideX));
            verticalZones = Math.round(Math.floor(height / sideY));
        } while (!fit(sideX, sideY, horizontalZones, verticalZones, width, height, n));

        List<SimulationObject> objects = new ArrayList<>();
int generatedObjects = 0;
        outerloop:
        for (int i = 0; i < horizontalZones; i++) {
            for (int j = 0; j < verticalZones; j++) {
                double radius = random.nextDouble() * sideY / 5.0;
                SimulationObject o = C.createNewSimulationObject();

                double boxContainingCenterWidth = sideX - 2 * radius;
                double boxContainingCenterHeight = sideY - 2 * radius;
                double centerRelativeToTheBoxX = radius + random.nextDouble() * boxContainingCenterWidth;
                double centerRelativeToTheBoxY = radius + random.nextDouble() * boxContainingCenterHeight;

                o.setX(New.num(i * sideX + centerRelativeToTheBoxX - screenTranslationX));
                o.setY(New.num(j * sideY + centerRelativeToTheBoxY - screenTranslationY));
                o.setZ(ZERO);
                o.setRadius(New.num(radius));
                o.setVelocity(new TripleNumber(New.num((random.nextDouble() - 0.5) * 10), New.num((random.nextDouble() - 0.5) * 10), ZERO));
                o.setColor(Color.BLUE.getRGB());

                // density = mass / volume
                o.setMass(calculateVolumeFromRadius(o.getRadius()).multiply(New.num(100_000_000_000L)));
                o.setId(String.valueOf(generatedObjects));

                objects.add(o);
                generatedObjects++;
                if (printInfo) {
                    info(logger, generatedObjects + " objects generated.");
                }
                if (generatedObjects == n) {
                    break outerloop;
                }
            }
        }
        properties.setInitialObjects(objects);
    }

    public static List<SimulationObject> generateComplexObject(Number x, Number y, Number radius, Number mass, String id, Number rotationDirection,
                                                               Number velocityX, Number velocityY, Color color) {
        final Number DEG_TO_RAD = PI.divide(New.num("180"));
        List<SimulationObject> objectParticles = new ArrayList<>();
        Number objectsInOneCircleDivisor = New.num("1");    // bigger = less objects
        Number numberOfNestedCirclesDivisor = New.num("1");    // bigger = less objects
        Number velocityFactor = New.num("0.2");   // smaller = lower velocity
        Number massChangeInDepthFactor = New.num("1e10");
        Number particleRadius = New.num("1");

        // Circles
        Number centerObjectRadius = ONE;
        Number minRadius = centerObjectRadius.multiply(TWO);
        Number minDistanceBetweenCircles = particleRadius.multiply(TWO).add(ONE);
        Number maxNumberOfCircles = radius.subtract(minRadius).divide(minDistanceBetweenCircles);
        Number outerLoopStep = numberOfNestedCirclesDivisor.compareTo(ONE) <= 0 ?
                minDistanceBetweenCircles :
                maxNumberOfCircles.divide(numberOfNestedCirclesDivisor);
                
        for (Number r = radius; r.compareTo(minRadius) >= 0; r = r.subtract(outerLoopStep)) {
            Number particlesInCurrentCircle = New.num(Math.round(r.divide(objectsInOneCircleDivisor).doubleValue()));
            Number innerLoopStep = New.num("360").divide(particlesInCurrentCircle);

            // Objects in the current circle
            for (Number i = ZERO; i.compareTo(New.num("359.9")) < 0; i = i.add(innerLoopStep)) {
                Number radians = i.multiply(DEG_TO_RAD); //convert degrees into radians

                SimulationObject o = C.createNewSimulationObject();
                o.setX(IGNORED.cos(radians).multiply(r).add(x));
                o.setY(IGNORED.sin(radians).multiply(r).add(y));
                o.setVelocity(new TripleNumber(IGNORED.cos(radians.subtract(New.num("300"))).multiply(r.multiply(velocityFactor))
                                                    .multiply(rotationDirection).add(velocityX),
                                               IGNORED.sin(radians.subtract(New.num("300"))).multiply(r.multiply(velocityFactor))
                                                    .multiply(rotationDirection).add(velocityY),
                                               ZERO));
                o.setAcceleration(new TripleNumber());
                o.setMass(mass.multiply(ONE.divide(r).multiply(massChangeInDepthFactor)));
                o.setRadius(particleRadius);
                o.setId(id + r + i);
                o.setColor(color.getRGB());
                objectParticles.add(o);
            }
        }
        return objectParticles;
    }

    private static boolean fit(double sideX, double sideY, long horizontalZones, long verticalZones, double width, double height, int n) {
        return sideX * horizontalZones <= width && sideY * verticalZones <= height && horizontalZones * verticalZones >= n;
    }
}
