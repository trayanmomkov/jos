package info.trekto.jos.io;

import com.google.gson.*;
import info.trekto.jos.C;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.formulas.ForceCalculator;
import info.trekto.jos.model.SimulationObject;
import info.trekto.jos.model.impl.SimulationObjectImpl;
import info.trekto.jos.model.impl.TripleInt;
import info.trekto.jos.model.impl.TripleNumber;
import info.trekto.jos.numbers.New;
import info.trekto.jos.numbers.NumberFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.trekto.jos.numbers.NumberFactoryProxy.createNumberFactory;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Trayan Momkov
 */
public class JsonReaderWriter implements ReaderWriter {
    private static final Logger logger = LoggerFactory.getLogger(JsonReaderWriter.class);
    private BufferedWriter writer;

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
            initialObjects.add(mapToJson(gson, simulationObject));
        }

        json.add("initialObjects", initialObjects);
        return json;
    }

    private JsonObject mapPropertiesToJson(SimulationProperties properties) {
        JsonObject json = new JsonObject();

        json.addProperty("numberOfIterations", properties.getNumberOfIterations());
        json.addProperty("secondsPerIteration", properties.getSecondsPerIteration().toString());
        json.addProperty("numberOfObjects", properties.getNumberOfObjects());
        json.addProperty("outputFile", properties.getOutputFile());
        json.addProperty("saveToFile", properties.isSaveToFile());
        json.addProperty("numberType", properties.getNumberType().name());
        json.addProperty("interactingLaw", properties.getInteractingLaw().name());
        json.addProperty("precision", properties.getPrecision());
        json.addProperty("scale", properties.getScale());
        json.addProperty("realTimeVisualization", properties.isRealTimeVisualization());
        json.addProperty("playingSpeed", properties.getPlayingSpeed());
        json.addProperty("bounceFromWalls", properties.isBounceFromWalls());
        return json;
    }

    private JsonObject mapToJson(Gson gson, SimulationObject simulationObject) {
        Map<String, Object> simulationObjectMap = new HashMap<>();
        simulationObjectMap.put("label", simulationObject.getLabel());

        simulationObjectMap.put("x", simulationObject.getX().toString());
        simulationObjectMap.put("y", simulationObject.getY().toString());
        simulationObjectMap.put("z", simulationObject.getZ().toString());

        simulationObjectMap.put("mass", simulationObject.getMass().toString());

        Map<String, String> speed = new HashMap<>();
        speed.put("x", simulationObject.getSpeed().getX().toString());
        speed.put("y", simulationObject.getSpeed().getY().toString());
        speed.put("z", simulationObject.getSpeed().getZ().toString());
        simulationObjectMap.put("speed", speed);

        simulationObjectMap.put("radius", simulationObject.getRadius().toString());

        Map<String, Integer> color = new HashMap<>();
        color.put("r", simulationObject.getColor().getR());
        color.put("g", simulationObject.getColor().getG());
        color.put("b", simulationObject.getColor().getB());
        simulationObjectMap.put("color", color);

        simulationObjectMap.put("motionless", simulationObject.isMotionless());
        return gson.toJsonTree(simulationObjectMap).getAsJsonObject();
    }

    @Override
    public SimulationProperties readProperties(String inputFilePath) throws FileNotFoundException {
        SimulationProperties properties = new SimulationProperties();
        try {
            JsonObject json = JsonParser.parseReader(new FileReader(inputFilePath)).getAsJsonObject().get("properties").getAsJsonObject();
            properties.setNumberType(NumberFactory.NumberType.valueOf(json.get("numberType").getAsString()));
            properties.setPrecision(json.get("precision").getAsInt());
            properties.setScale(json.get("scale").getAsInt());

            createNumberFactory(properties.getNumberType(), properties.getPrecision(), properties.getScale());

            properties.setNumberOfIterations(json.get("numberOfIterations").getAsLong());
            properties.setSecondsPerIteration(New.num(json.get("secondsPerIteration").getAsString()));
            properties.setNumberOfObjects(json.get("numberOfObjects").getAsInt());
            properties.setOutputFile(json.get("outputFile").getAsString());
            properties.setSaveToFile(json.get("saveToFile").getAsBoolean());
            properties.setInteractingLaw(ForceCalculator.InteractingLaw.valueOf(json.get("interactingLaw").getAsString()));
            properties.setRealTimeVisualization(json.get("realTimeVisualization").getAsBoolean());
            properties.setPlayingSpeed(json.get("playingSpeed").getAsInt());
            properties.setBounceFromWalls(json.get("bounceFromWalls").getAsBoolean());

            List<SimulationObject> initialObjects = new ArrayList<>();
            for (JsonElement jsonElement : json.get("initialObjects").getAsJsonArray()) {
                JsonObject o = jsonElement.getAsJsonObject();
                SimulationObject simo = new SimulationObjectImpl();

                simo.setLabel(o.get("label").getAsString());

                simo.setX(New.num(o.get("x").getAsString()));
                simo.setY(New.num(o.get("y").getAsString()));
                simo.setZ(New.num(o.get("z").getAsString()));

                simo.setMass(New.num(o.get("mass").getAsString()));

                JsonObject speed = o.get("speed").getAsJsonObject();
                simo.setSpeed(new TripleNumber(New.num(speed.get("x").getAsString()),
                                               New.num(speed.get("y").getAsString()),
                                               New.num(speed.get("z").getAsString())));

                simo.setRadius(New.num(o.get("radius").getAsString()));
                JsonObject color = o.get("color").getAsJsonObject();
                simo.setColor(new TripleInt(color.get("r").getAsInt(), color.get("g").getAsInt(), color.get("b").getAsInt()));
                simo.setMotionless(o.get("motionless").getAsBoolean());

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
            objectsAsJsonArray.add(mapToJson(gson, simulationObject));
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
            writer = Files.newBufferedWriter(new File(properties.getOutputFile()).toPath(), UTF_8);
        } catch (IOException e) {
            logger.info("Cannot open output file " + inputFilePath, e);
        }
    }
}
