package info.trekto.jos.core.impl;

import info.trekto.jos.core.impl.arbitrary_precision.DataAP;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.io.JsonReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
import static info.trekto.jos.util.Utils.info;
import static info.trekto.jos.util.Utils.isNullOrBlank;

/**
 * A scene with the exact state of a stopped run. Loading it continues the run.
 * The saved iterations are not enough for that: usually they have no velocities and masses.
 */
public class ResumeScene {
    private static final Logger logger = LoggerFactory.getLogger(ResumeScene.class);

    public static void write(SimulationProperties properties, DataAP data) {
        if (!properties.isSaveToFile() || isNullOrBlank(properties.getOutputFile())) {
            return;
        }

        List<SimulationObject> objects = new ArrayList<>();
        for (int i = 0; i < data.n; i++) {
            if (!data.deleted[i]) {
                SimulationObject object = new SimulationObjectImpl();
                object.setId(data.id[i]);
                object.setX(data.positionX[i]);
                object.setY(data.positionY[i]);
                object.setZ(ZERO);
                object.setVelocity(new TripleNumber(data.velocityX[i], data.velocityY[i], ZERO));
                object.setAcceleration(new TripleNumber());
                object.setMass(data.mass[i]);
                object.setRadius(data.radius[i]);
                object.setColor(data.color[i]);
                objects.add(object);
            }
        }

        SimulationProperties scene = new SimulationProperties(properties);
        scene.setInitialObjects(objects);
        scene.setNumberOfObjects(objects.size());
        /* The continued run must not overwrite the file of the stopped one. */
        scene.setOutputFile("");

        String fileName = fileName(properties.getOutputFile());
        new JsonReaderWriter().writeProperties(scene, fileName);
        info(logger, "Scene for continuing the run: " + fileName);
    }

    /* "run.json.gz" -> "run_resume.json" */
    static String fileName(String outputFile) {
        String name = outputFile;
        if (name.endsWith(".gz")) {
            name = name.substring(0, name.length() - ".gz".length());
        }
        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - ".json".length());
        }
        return name + "_resume.json";
    }
}
