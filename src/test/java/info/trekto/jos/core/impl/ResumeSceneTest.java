package info.trekto.jos.core.impl;

import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.io.JsonReaderWriter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import static info.trekto.jos.core.numbers.NumberFactory.NumberType.DOUBLE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ONE;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.Utils.isNullOrBlank;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ResumeSceneTest {
    private static final int PRECISION = 16;

    @BeforeClass
    public void init() {
        createNumberFactory(DOUBLE, PRECISION);
    }

    @Test
    public void fileNameComesFromTheOutputFile() {
        assertEquals(ResumeScene.fileName("run.json.gz"), "run_resume.json");
        assertEquals(ResumeScene.fileName("run.gz"), "run_resume.json");
        assertEquals(ResumeScene.fileName("run"), "run_resume.json");
    }

    @Test
    public void sceneHoldsTheStateOfTheAliveObjects() throws Exception {
        SimulationProperties properties = properties(true);
        ResumeScene.write(properties, threeObjectsTheSecondDeleted());

        File scene = new File(ResumeScene.fileName(properties.getOutputFile()));
        scene.deleteOnExit();
        SimulationProperties read = new JsonReaderWriter().readPropertiesAndCreateNumberFactory(scene.getAbsolutePath());

        assertEquals(read.getNumberOfObjects(), 2);
        assertEquals(read.getInitialObjects().size(), 2);
        SimulationObject last = read.getInitialObjects().get(1);
        assertEquals(last.getId(), "2");
        assertEquals(last.getX().doubleValue(), 2.0);
        assertEquals(last.getVelocity().getY().doubleValue(), -20.0);
        assertEquals(last.getMass().doubleValue(), 300.0);
        assertEquals(last.getRadius().doubleValue(), 3.0);
        assertTrue(isNullOrBlank(read.getOutputFile()), "The continued run must not overwrite the stopped one");
    }

    @Test
    public void nothingIsWrittenWhenTheRunIsNotSaved() throws Exception {
        SimulationProperties properties = properties(false);
        ResumeScene.write(properties, threeObjectsTheSecondDeleted());

        assertFalse(new File(ResumeScene.fileName(properties.getOutputFile())).exists());
    }

    private static SimulationProperties properties(boolean saveToFile) throws Exception {
        File output = File.createTempFile("jos-run", ".json.gz");
        output.deleteOnExit();

        SimulationProperties properties = new SimulationProperties();
        properties.setNumberType(DOUBLE);
        properties.setPrecision(PRECISION);
        properties.setNumberOfIterations(100);
        properties.setSecondsPerIteration(New.num("0.01"));
        properties.setNumberOfObjects(3);
        properties.setSaveToFile(saveToFile);
        properties.setOutputFile(output.getAbsolutePath());
        properties.setCoefficientOfRestitution(ONE);
        properties.setMinDistance(New.num(10));
        properties.setScale(1);
        properties.setBackgroundColor(Color.WHITE);
        properties.setInitialObjects(new ArrayList<>());
        return properties;
    }

    private static DataAP threeObjectsTheSecondDeleted() {
        DataAP data = new DataAP(3);
        for (int i = 0; i < 3; i++) {
            data.id[i] = String.valueOf(i);
            data.positionX[i] = New.num(i);
            data.positionY[i] = New.num(-i);
            data.velocityX[i] = New.num(10 * i);
            data.velocityY[i] = New.num(-10 * i);
            data.mass[i] = New.num(100 * (i + 1));
            data.radius[i] = New.num(i + 1);
            data.color[i] = 0x112233;
        }
        data.deleted[1] = true;
        return data;
    }
}
