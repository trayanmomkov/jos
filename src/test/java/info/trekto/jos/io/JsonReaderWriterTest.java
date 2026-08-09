package info.trekto.jos.io;

import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.numbers.New;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JsonReaderWriterTest {
    public static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(ARBITRARY_PRECISION, PRECISION);
    }

    /**
     * A run with numberOfIterations = 0 ("run until stopped") must still produce a readable file.
     * The separator between frames used to be written AFTER each one and skipped on whichever frame
     * was predicted to be last - a prediction that cannot be made for an open-ended run, so every
     * frame got a trailing comma, the file ended "},\n ] }", and the last frame was lost.
     */
    @Test
    public void infiniteRunProducesAFileWhoseEveryFrameCanBeReadBack() throws Exception {
        int objects = 3;
        int frames = 4;
        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(ARBITRARY_PRECISION);
        properties.setPrecision(PRECISION);
        properties.setNumberOfIterations(0);               // 0 => run until stopped
        properties.setSecondsPerIteration(New.num("0.01"));
        properties.setNumberOfObjects(objects);
        properties.setSaveEveryNthIteration(1);
        properties.setCoefficientOfRestitution(ONE);
        properties.setMinDistance(New.num(10));
        properties.setScale(1);
        properties.setBackgroundColor(Color.WHITE);
        properties.setInitialObjects(new ArrayList<>());
        assertTrue(properties.isInfiniteSimulation(), "the scene under test must be an open-ended run");

        File file = File.createTempFile("jos-infinite-run", ".json.gz");
        file.deleteOnExit();
        properties.setOutputFile(file.getAbsolutePath());

        double[] x = new double[objects], y = new double[objects], zero = new double[objects];
        double[] mass = new double[objects], radius = new double[objects];
        String[] id = new String[objects];
        int[] color = new int[objects];
        boolean[] deleted = new boolean[objects];
        for (int i = 0; i < objects; i++) {
            x[i] = i; y[i] = -i; mass[i] = 1e6; radius[i] = 1; id[i] = "o" + i; color[i] = 0x112233;
        }

        JsonReaderWriter writer = new JsonReaderWriter();
        for (int f = 0; f < frames; f++) {
            writer.appendObjectsToFile(properties, f, x, y, zero, zero, zero, zero, mass, radius,
                                       id, color, deleted, zero, zero, zero);
        }
        writer.endFile();

        JsonReaderWriter reader = new JsonReaderWriter();
        reader.readPropertiesForPlaying(file.getAbsolutePath());
        int read = 0;
        while (reader.readNextIteration() != null) {       // must not throw on the final frame
            read++;
        }
        assertEquals(read, frames, "every written frame must be readable back");
    }
}
