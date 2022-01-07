package info.trekto.jos.util;

import java.io.*;
import java.math.BigDecimal;

public class SimulationGenerator {
    private String filename = "input.sim";
    private int numberOfObject = 500;
    private boolean savingData = true;
    private int numberOfCycles = 200000;

    public static void main(String[] args) {
        SimulationGenerator simulationGenerator = new SimulationGenerator();
        simulationGenerator.generate();
    }

    public void generate() {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "utf-8"))) {
            generateToWriter(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateToWriter(Writer writer) throws IOException {
        writer.append(generateHead());
        generateSimulationObjects(writer);
    }

    private void generateSimulationObjects(Writer writer) throws IOException {

        BigDecimal x;
        BigDecimal y;
        BigDecimal speedX;
        BigDecimal speedY;
        String mag;
        String mass;
        String radius;
        String colorR;
        String colorG;
        String colorB;
        String isStatic;
        int label;

        for (int i = 0; i < numberOfObject; i++) {
            x = new BigDecimal(20).add(new BigDecimal(i * 100));
            y = new BigDecimal(20).add(new BigDecimal(i * 100));
            speedX = new BigDecimal("0.001");
            speedY = BigDecimal.ZERO;
            mag = "1";
            mass = "1e5";
            radius = "10";
            colorR = "0";
            colorG = "0";
            colorB = "255";
            isStatic = "0";
            label = i;
            writer.append(generateSimulationObject(
                    x.toString(), y.toString(), speedX.toString(), speedY.toString(), mag, mass, radius,
                    colorR, colorG, colorB, isStatic, String.valueOf(label)));
        }
    }

    private String generateSimulationObject(String x, String y, String speedX, String speedY, String mag, String mass,
                                            String radius, String colorR, String colorG, String colorB, String isStatic,
                                            String label) {
        return "\n\n" +
                "x = \t\t" + x + "\n" +
                "y = \t\t" + y + "\n" +
                "speed x = \t" + speedX + "\n" +
                "speed y = \t" + speedY + "\n" +
                "mag = \t\t" + mag + "\n" +
                "mass = \t\t" + mass + "\n" +
                "radius = \t" + radius + "\n" +
                "color R = \t" + colorR + "\n" +
                "color G = \t" + colorG + "\n" +
                "color B = \t" + colorB + "\n" +
                "is static = \t" + isStatic + "\n" +
                "label= \t\t'" + label + "'";
    }

    private String generateHead() {
        return "numbers type:\t\tDOUBLE\t\tPossible values: FLOAT, DOUBLE, BIG_DECIMAL\n" +
                "precision:\t\t32\t\tArithmetic precision in digits after point. 0 = infinite precision; -1 = native floating point type double\n" +
                "window size:\t\t1280 1024\tin pixels\n" +
                "\n" +
                "bounce from screen:\t0\t\t(1 = true, 0 = false)\tWhether objects bounce from the window borders\n" +
                "bounce from each other:\t0\t\t(1 = true, 0 = false)\tWhether objects bounce from each other\n" +
                "gravity:\t\t1\t\t(1 = true, 0 = false)\tWhether gravitation law is in force\n" +
                "absorbtion:\t\t1\t\t(1 = true, 0 = false)\tWhether one object can swallow (absorb) another\n" +
                "restitution:\t\t1\t\t[0, 1] perfectly inelastic = 0 to 1 = perfectly elastic Coeficient of objects elasticity.\n" +
                "\n" +
                "number of cycles:\t" + numberOfCycles + "\t\t-1 = infinite loop\n" +
                "seconds per cycle:\t1\n" +
                "simulation time:\t-1\t\t(Depend from numberOfCycles and secondsPerCycle and vice versa, -1 mean no value)\n" +
                "real time:\t\t0\t\t(1 = true, 0 = false)\tShow simulation in real time\n" +
                "playing:\t\t1 0\t\t(1 = true, 0 = false)\tWhether to read data form file and play it; Second digit toggles recording mode\n" +
                "playing speed:\t\t60\t\tin real time mode indicates 1 per how many cycle to be saved in file; in playing mode indicates how many cycles to be skipped in each visualisation step\n" +
                "sleep mseconds:\t\t0\t\tmiliseconds, how long to sleep for every amount of cycles to prevent system overheat for long simulations (0 = no sleep)\n" +
                "cycles before sleep:\t0\t\tnumber of cycles before sleep to prevent system overheat for long simulations\n" +
                "\n" +
                "3D:\t\t\t0\t\t(1 = 3D, 0 = 2D)\tWhether visualization to be 3D or 2D.\n" +
                "scene translation:\t0 0 2.7e-6\t\t(x, y, z)\n" +
                "scene rotation:\t\t0 0 0 0\t\t1 0 0 292\t\tx, y, z, angle\t\taxis of rotation, vector and angle of rotation in degrees\n" +
                "perspective:\t\t45 0 0.1 100\tfovy - field of view angle, in degrees, in the y direction; aspect - aspect ratio, 0 = default; zNear - Distance from the viewer to the near clipping plane (>0); zFar - Distance from the viewer to the far clipping plane (>0)\n" +
                "interaction plane:\t2\t\t(1 = xz; 2 = xy, 3 = yz)\n" +
                "\n" +
                "trajectory length:\t0\t\tNumber of point remembered for trajectory. 0 = no trajectory is drawn; -1 = no limit, keep all points\n" +
                "trajectory width:\t1\t\tWidth of lines linking trajectory points\n" +
                "showing labels:\t\t0\t\tWhether to show object labels.\n" +
                "\n" +
                "saving data:\t\t" + (savingData ? "1" : "0") + "\t\t(1 = true, 0 = false)\n" +
                "output file name:\t" + filename + ".out\n" +
                "number of objects:\t" + numberOfObject + "\n" +
                "\n" +
                "Objects:";
    }
}
