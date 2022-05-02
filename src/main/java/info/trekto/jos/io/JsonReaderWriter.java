package info.trekto.jos.io;

import com.google.gson.*;
import info.trekto.jos.core.ForceCalculator;
import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.NumberFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static info.trekto.jos.core.Controller.C;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.createNumberFactory;
import static info.trekto.jos.util.Utils.error;
import static info.trekto.jos.util.Utils.info;
import static org.codehaus.jackson.JsonToken.END_OBJECT;

/**
 * @author Trayan Momkov
 */
public class JsonReaderWriter implements ReaderWriter {
    private static final Logger logger = LoggerFactory.getLogger(JsonReaderWriter.class);
    private OutputStreamWriter writer;
    private org.codehaus.jackson.JsonParser parser;

    public void writeProperties(SimulationProperties properties, String outputFilePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject json = mapPropertiesAndInitialObjects(properties, gson);
        try (Writer writer = new FileWriter(outputFilePath)) {
            writer.write("{\n  \"properties\":\n");
            gson.toJson(json, writer);
            writer.write("\n}");
        } catch (IOException e) {
            error(logger, "Cannot save properties.");
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
        json.addProperty("secondsPerIteration", properties.getSecondsPerIteration().toString());
        json.addProperty("numberOfObjects", properties.getNumberOfObjects());
        json.addProperty("outputFile", properties.getOutputFile());
        json.addProperty("saveToFile", properties.isSaveToFile());
        json.addProperty("saveEveryNthIteration", properties.getSaveEveryNthIteration());
        json.addProperty("numberType", properties.getNumberType().name());
        json.addProperty("interactingLaw", properties.getInteractingLaw().name());
        json.addProperty("precision", properties.getPrecision());
        json.addProperty("scale", properties.getScale());
        json.addProperty("realTimeVisualization", properties.isRealTimeVisualization());
        json.addProperty("playingSpeed", properties.getPlayingSpeed());
        json.addProperty("bounceFromWalls", properties.isBounceFromWalls());
        return json;
    }

    private JsonObject mapSimulationObjectToJson(Gson gson, SimulationObject simulationObject) {
        Map<String, Object> simulationObjectMap = new HashMap<>();
        simulationObjectMap.put("id", simulationObject.getId());

        simulationObjectMap.put("x", simulationObject.getX().toString());
        simulationObjectMap.put("y", simulationObject.getY().toString());
        simulationObjectMap.put("z", simulationObject.getZ().toString());

        simulationObjectMap.put("mass", simulationObject.getMass().toString());

        simulationObjectMap.put("speedX", simulationObject.getSpeed().getX().toString());
        simulationObjectMap.put("speedY", simulationObject.getSpeed().getY().toString());
        simulationObjectMap.put("speedZ", simulationObject.getSpeed().getZ().toString());

        simulationObjectMap.put("radius", simulationObject.getRadius().toString());

        simulationObjectMap.put("color", String.format("%08X", simulationObject.getColor()).substring(2));
        return gson.toJsonTree(simulationObjectMap).getAsJsonObject();
    }

    private JsonObject mapSimulationObjectToJson(Gson gson, double positionX, double positionY, double positionZ, double speedX, double speedY,
                                                 double speedZ, double mass, double radius, String id, int color) {
        Map<String, Object> simulationObjectMap = new HashMap<>();
        simulationObjectMap.put("id", id);

        simulationObjectMap.put("x", positionX);
        simulationObjectMap.put("y", positionY);
        simulationObjectMap.put("z", positionZ);

        simulationObjectMap.put("mass", mass);

        simulationObjectMap.put("speedX", speedX);
        simulationObjectMap.put("speedY", speedY);
        simulationObjectMap.put("speedZ", speedZ);

        simulationObjectMap.put("radius", radius);

        simulationObjectMap.put("color", String.format("%08X", color).substring(2));
        return gson.toJsonTree(simulationObjectMap).getAsJsonObject();
    }

    @Override
    public SimulationProperties readPropertiesAndCreateNumberFactory(String inputFilePath) throws FileNotFoundException {
        SimulationProperties properties = new SimulationProperties();
        try {
            JsonObject json = JsonParser.parseReader(new FileReader(inputFilePath)).getAsJsonObject().get("properties").getAsJsonObject();
            readPropertiesAndCreateNumberFactory(json, properties);
        } catch (ClassCastException | IllegalStateException ex) {
            error(logger, "Cannot parse properties file: '" + inputFilePath + "'", ex);
        }
        return properties;
    }

    private void readPropertiesAndCreateNumberFactory(JsonObject json, SimulationProperties properties) {
        properties.setNumberType(NumberFactory.NumberType.valueOf(json.get("numberType").getAsString()));
        properties.setPrecision(json.get("precision").getAsInt());
        properties.setScale(json.get("scale").getAsInt());

        createNumberFactory(properties.getNumberType(), properties.getPrecision(), properties.getScale());

        properties.setNumberOfIterations(json.get("numberOfIterations").getAsLong());
        properties.setSecondsPerIteration(New.num(json.get("secondsPerIteration").getAsString()));
        properties.setNumberOfObjects(json.get("numberOfObjects").getAsInt());
        properties.setOutputFile(json.get("outputFile").getAsString());
        properties.setSaveToFile(json.get("saveToFile").getAsBoolean());
        if (json.get("saveEveryNthIteration") != null) {
            properties.setSaveEveryNthIteration(json.get("saveEveryNthIteration").getAsInt());
        } else {
            properties.setSaveEveryNthIteration(1);
        }
        properties.setInteractingLaw(ForceCalculator.InteractingLaw.valueOf(json.get("interactingLaw").getAsString()));
        properties.setRealTimeVisualization(json.get("realTimeVisualization").getAsBoolean());
        properties.setPlayingSpeed(json.get("playingSpeed").getAsInt());
        JsonElement bounceFromWall = json.get("bounceFromWalls");
        properties.setBounceFromWalls(bounceFromWall != null && bounceFromWall.getAsBoolean());

        List<SimulationObject> initialObjects = new ArrayList<>();
        for (JsonElement jsonElement : json.get("initialObjects").getAsJsonArray()) {
            JsonObject o = jsonElement.getAsJsonObject();
            SimulationObject simo = new SimulationObjectImpl();

            simo.setId(o.get("id").getAsString());

            simo.setX(New.num(o.get("x").getAsString()));
            simo.setY(New.num(o.get("y").getAsString()));
            simo.setZ(New.num(o.get("z").getAsString()));

            simo.setMass(New.num(o.get("mass").getAsString()));

            simo.setSpeed(new TripleNumber(New.num(o.get("speedX").getAsString()),
                                           New.num(o.get("speedY").getAsString()),
                                           New.num(o.get("speedZ").getAsString())));

            simo.setRadius(New.num(o.get("radius").getAsString()));
            simo.setColor(Integer.parseInt(o.get("color").getAsString(), 16));

            initialObjects.add(simo);
        }

        properties.setInitialObjects(initialObjects);
    }

    @Override
    public SimulationProperties readPropertiesForPlaying(String inputFile) throws IOException {
        SimulationProperties prop = new SimulationProperties();
        try {
            parser = new MappingJsonFactory()
                    .createJsonParser(new InputStreamReader(new BufferedInputStream(new GZIPInputStream(new FileInputStream(inputFile)))));

            parser.nextToken(); // Start root
            parser.nextToken(); // Field properties
            parser.nextToken(); // Start object
            readPropertiesAndCreateNumberFactory(JsonParser.parseString(parser.readValueAsTree().toString()).getAsJsonObject(), prop);
            parser.nextToken(); // Field "simulation"
            parser.nextToken(); // Start array
        } catch (ClassCastException | IllegalStateException ex) {
            error(logger, "Cannot read input file: '" + inputFile + "'", ex);
        }

        return prop;
    }

    @Override
    public boolean hasMoreIterations() {
        return parser != null && !parser.isClosed();
    }

    @Override
    public Iteration readNextIteration() throws IOException {
        JsonToken currentToken = parser.nextToken(); // Object start
        if (currentToken.equals(END_OBJECT)) {
            return null;
        }
        JsonNode iteration = parser.readValueAsTree();
        long cycle = iteration.get("cycle").getLongValue();
        int numberOfObjects = iteration.get("numberOfObjects").getIntValue();
        List<SimulationObject> objects = new ArrayList<>();
        if (iteration.get("objects").isArray()) {
            for (final JsonNode node : iteration.get("objects")) {
                SimulationObject o = new SimulationObjectImpl();
                o.setId(node.get("id").getTextValue());
                o.setX(New.num(node.get("x").asText()));
                o.setY(New.num(node.get("y").asText()));
                o.setZ(New.num(node.get("z").asText()));
                o.setSpeed(new TripleNumber(New.num(node.get("speedX").asText()),
                                            New.num(node.get("speedY").asText()),
                                            New.num(node.get("speedZ").asText())));
                o.setMass(New.num(node.get("mass").asText()));
                o.setRadius(New.num(node.get("radius").asText()));
                o.setColor(Integer.parseInt(node.get("color").getTextValue(), 16));
                objects.add(o);
            }
        }
        parser.nextToken(); // Object end
        return new Iteration(cycle, numberOfObjects, objects);
    }

    @Override
    public void appendObjectsToFile(List<SimulationObject> simulationObjects, SimulationProperties properties, long currentIterationNumber) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (writer == null) {
            initWriter(properties.getOutputFile());
            try {
                writer.write("{\n  \"properties\":\n");
                gson.toJson(mapPropertiesAndInitialObjects(properties, gson), writer);
                writer.write(",\n  \"simulation\": [\n");
            } catch (IOException e) {
                error(logger, "Cannot write 'simulation' element to output JSON file.", e);
            }
        }

        JsonArray objectsAsJsonArray = new JsonArray();
        for (SimulationObject simulationObject : simulationObjects) {
            objectsAsJsonArray.add(mapSimulationObjectToJson(gson, simulationObject));
        }

        JsonObject cycleJson = new JsonObject();
        cycleJson.addProperty("cycle", currentIterationNumber);
        cycleJson.addProperty("numberOfObjects", simulationObjects.size());
        cycleJson.add("objects", objectsAsJsonArray);

        gson.toJson(cycleJson, writer);
        boolean lastIterationToSave = currentIterationNumber >= properties.getNumberOfIterations()
                || currentIterationNumber + properties.getSaveEveryNthIteration() > properties.getNumberOfIterations();
        if (!C.hasToStop() && (properties.isInfiniteSimulation() || !lastIterationToSave)) {
            try {
                writer.write(",\n");
            } catch (IOException e) {
                error(logger, "Cannot write comma after writing cycle in the output file.", e);
            }
        }
    }

    @Override
    public void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, double[] positionX, double[] positionY,
                                    double[] positionZ, double[] speedX, double[] speedY, double[] speedZ, double[] mass, double[] radius,
                                    String[] id, int[] color, boolean[] deleted) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (writer == null) {
            initWriter(properties.getOutputFile());
            try {
                writer.write("{\n  \"properties\":\n");
                gson.toJson(mapPropertiesAndInitialObjects(properties, gson), writer);
                writer.write(",\n  \"simulation\": [\n");
            } catch (IOException e) {
                error(logger, "Cannot write 'simulation' element to output JSON file.", e);
            }
        }

        JsonArray objectsAsJsonArray = new JsonArray();
        for (int i = 0; i < positionX.length; i++) {
            if (!deleted[i]) {
                objectsAsJsonArray.add(mapSimulationObjectToJson(gson, positionX[i], positionY[i], positionZ[i], speedX[i], speedY[i], speedZ[i],
                                                                 mass[i], radius[i], id[i], color[i]));
            }
        }

        JsonObject cycleJson = new JsonObject();
        cycleJson.addProperty("cycle", currentIterationNumber);
        cycleJson.addProperty("numberOfObjects", objectsAsJsonArray.size());
        cycleJson.add("objects", objectsAsJsonArray);

        gson.toJson(cycleJson, writer);
        boolean lastIterationToSave = currentIterationNumber >= properties.getNumberOfIterations()
                || currentIterationNumber + properties.getSaveEveryNthIteration() > properties.getNumberOfIterations();
        if (!C.hasToStop() && (properties.isInfiniteSimulation() || !lastIterationToSave)) {
            try {
                writer.write(",\n");
            } catch (IOException e) {
                error(logger, "Cannot write comma after writing cycle in the output file.", e);
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
            info(logger, "Error while closing file.", e);
        }
    }

    public void initWriter(String inputFilePath) {
        try {
            if (!inputFilePath.endsWith(".gz")) {
                inputFilePath = inputFilePath + ".gz";
            }
            writer = new OutputStreamWriter(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(inputFilePath))));
        } catch (IOException e) {
            info(logger, "Cannot open output file " + inputFilePath, e);
        }
    }
}
