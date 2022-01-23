package info.trekto.jos.io;

import com.google.gson.*;
import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationImpl;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * @author Trayan Momkov
 */
public class JsonReaderWriter implements ReaderWriter {
    private static final Logger logger = LoggerFactory.getLogger(JsonReaderWriter.class);
    private OutputStreamWriter writer;

    public void writeProperties(SimulationProperties properties, String outputFilePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject json = mapPropertiesAndInitialObjects(properties, gson);
        try (Writer writer = new FileWriter(outputFilePath)) {
            writer.write("{\n  \"properties\":\n");
            gson.toJson(json, writer);
            writer.write("\n}");
        } catch (IOException e) {
            logger.error("Cannot save properties.");
        }
    }

    private JsonObject mapPropertiesAndInitialObjects(SimulationProperties properties, Gson gson) {
        JsonObject json = mapPropertiesToJson(properties);
        JsonArray initialObjects = new JsonArray();

        for (SimulationObject simulationObject : properties.getInitialObjects()) {
            initialObjects.add(mapSimulationObjectToJson(gson, simulationObject));
        }

        json.add("initialObjects", initialObjects);
        return json;
    }

    private JsonObject mapPropertiesToJson(SimulationProperties properties) {
        JsonObject json = new JsonObject();

        json.addProperty("numberOfIterations", properties.getNumberOfIterations());
        json.addProperty("secondsPerIteration", properties.getSecondsPerIteration());
        json.addProperty("numberOfObjects", properties.getNumberOfObjects());
        json.addProperty("outputFile", properties.getOutputFile());
        json.addProperty("saveToFile", properties.isSaveToFile());
        json.addProperty("numberType", "DOUBLE");
        json.addProperty("interactingLaw", "NEWTON_LAW_OF_GRAVITATION");
        json.addProperty("precision", properties.getPrecision());
        json.addProperty("scale", properties.getScale());
        json.addProperty("realTimeVisualization", properties.isRealTimeVisualization());
        json.addProperty("playingSpeed", properties.getPlayingSpeed());
        return json;
    }

    private JsonObject mapSimulationObjectToJson(Gson gson, SimulationObject simulationObject) {
        Map<String, Object> simulationObjectMap = new HashMap<>();
        simulationObjectMap.put("id", simulationObject.getId());
        simulationObjectMap.put("x", simulationObject.getX());
        simulationObjectMap.put("y", simulationObject.getY());
        simulationObjectMap.put("z", simulationObject.getZ());
        simulationObjectMap.put("mass", simulationObject.getMass());
        simulationObjectMap.put("speedX", simulationObject.getSpeedX());
        simulationObjectMap.put("speedY", simulationObject.getSpeedY());
        simulationObjectMap.put("speedZ", simulationObject.getSpeedZ());
        simulationObjectMap.put("radius", simulationObject.getRadius());
        simulationObjectMap.put("color", String.format("%08X", simulationObject.getColor()).substring(2));

        return gson.toJsonTree(simulationObjectMap).getAsJsonObject();
    }

    @Override
    public SimulationProperties readProperties(String inputFilePath) throws FileNotFoundException {
        SimulationProperties properties = new SimulationProperties();
        try {
            JsonObject json = JsonParser.parseReader(new FileReader(inputFilePath)).getAsJsonObject().get("properties").getAsJsonObject();
            properties.setPrecision(json.get("precision").getAsInt());
            properties.setScale(json.get("scale").getAsInt());

            properties.setNumberOfIterations(json.get("numberOfIterations").getAsLong());
            properties.setSecondsPerIteration(Double.parseDouble(json.get("secondsPerIteration").getAsString()));
            properties.setNumberOfObjects(json.get("numberOfObjects").getAsInt());

            C.simulation = new SimulationImpl(properties.getNumberOfObjects(), properties.getSecondsPerIteration());

            properties.setOutputFile(json.get("outputFile").getAsString());
            properties.setSaveToFile(json.get("saveToFile").getAsBoolean());
            properties.setRealTimeVisualization(json.get("realTimeVisualization").getAsBoolean());
            properties.setPlayingSpeed(json.get("playingSpeed").getAsInt());

            List<SimulationObject> initialObjects = new ArrayList<>();
            for (JsonElement jsonElement : json.get("initialObjects").getAsJsonArray()) {
                JsonObject o = jsonElement.getAsJsonObject();
                SimulationObject simo = new SimulationObjectImpl();

                simo.setX(o.get("x").getAsDouble());
                simo.setY(o.get("y").getAsDouble());
                simo.setZ(o.get("z").getAsDouble());

                simo.setSpeedX(o.get("speedX").getAsDouble());
                simo.setSpeedY(o.get("speedY").getAsDouble());
                simo.setSpeedZ(o.get("speedZ").getAsDouble());

                simo.setMass(Double.parseDouble(o.get("mass").getAsString()));
                simo.setRadius(Double.parseDouble(o.get("radius").getAsString()));
                simo.setColor(Integer.parseInt(o.get("color").getAsString(), 16));
                simo.setId(o.get("id").getAsString());

                initialObjects.add(simo);
            }

            properties.setInitialObjects(initialObjects);
        } catch (ClassCastException | IllegalStateException ex) {
            logger.error("Cannot parse properties file: '" + inputFilePath + "'", ex);
        }

        return properties;
    }

    @Override
    public void appendObjectsToFile(List<SimulationObject> simulationObjects) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (writer == null) {
            initWriter(C.prop, C.prop.getOutputFile());
            try {
                writer.write("{\n  \"properties\":\n");
                gson.toJson(mapPropertiesAndInitialObjects(C.prop, gson), writer);
                writer.write(",\n  \"simulation\": [\n");
            } catch (IOException e) {
                logger.error("Cannot write 'simulation' element to output JSON file.", e);
            }
        }

        JsonArray objectsAsJsonArray = new JsonArray();
        for (SimulationObject simulationObject : simulationObjects) {
            objectsAsJsonArray.add(mapSimulationObjectToJson(gson, simulationObject));
        }

        JsonObject cycleJson = new JsonObject();
        cycleJson.addProperty("cycle", C.simulation.getCurrentIterationNumber());
        cycleJson.addProperty("numberOfObjects", simulationObjects.size());
        cycleJson.add("objects", objectsAsJsonArray);

        gson.toJson(cycleJson, writer);
        if (C.simulation.getCurrentIterationNumber() < C.prop.getNumberOfIterations()) {
            try {
                writer.write(",\n");
            } catch (IOException e) {
                logger.error("Cannot write comma after writing cycle in the output file.", e);
            }
        }
    }

    @Override
    public void appendObjectsToFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (writer == null) {
            initWriter(C.prop, C.prop.getOutputFile());
            try {
                writer.write("{\n  \"properties\":\n");
                gson.toJson(mapPropertiesAndInitialObjects(C.prop, gson), writer);
                writer.write(",\n  \"simulation\": [\n");
            } catch (IOException e) {
                logger.error("Cannot write 'simulation' element to output JSON file.", e);
            }
        }

        JsonArray objectsAsJsonArray = new JsonArray();
        for (int i = 0; i < C.simulation.simulationLogicKernel.positionX.length; i++) {
            if (!C.simulation.simulationLogicKernel.deleted[i]) {
                objectsAsJsonArray.add(mapSimulationObjectToJson(gson, new SimulationObjectImpl(i)));
            }
        }

        JsonObject cycleJson = new JsonObject();
        cycleJson.addProperty("cycle", C.simulation.getCurrentIterationNumber());
        cycleJson.addProperty("numberOfObjects", objectsAsJsonArray.size());
        cycleJson.add("objects", objectsAsJsonArray);

        gson.toJson(cycleJson, writer);
        if (C.simulation.getCurrentIterationNumber() < C.prop.getNumberOfIterations()) {
            try {
                writer.write(",\n");
            } catch (IOException e) {
                logger.error("Cannot write comma after writing cycle in the output file.", e);
            }
        }
    }

    @Override
    public void endFile() {
        try {
            if (writer != null) {
                writer.write("\n    ]\n}");
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            logger.info("Error while closing file.", e);
        }
    }

    public void initWriter(SimulationProperties properties, String inputFilePath) {
        try {
            if (!inputFilePath.endsWith(".zip")) {
                inputFilePath = inputFilePath + ".zip";
            }
            writer = new OutputStreamWriter(new BufferedOutputStream(
                    new GZIPOutputStream(
                            new FileOutputStream(inputFilePath)
                    )));
        } catch (IOException e) {
            logger.info("Cannot open output file " + inputFilePath, e);
        }
    }
}
