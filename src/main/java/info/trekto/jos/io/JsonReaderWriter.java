package info.trekto.jos.io;

import com.google.gson.*;
import info.trekto.jos.core.ForceCalculator;
import info.trekto.jos.core.impl.Iteration;
import info.trekto.jos.core.impl.SimulationProperties;
import info.trekto.jos.core.model.SimulationObject;
import info.trekto.jos.core.model.impl.SimulationObjectImpl;
import info.trekto.jos.core.model.impl.TripleNumber;
import info.trekto.jos.core.numbers.New;
import info.trekto.jos.core.numbers.Number;
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
import static info.trekto.jos.core.model.impl.SimulationObjectImpl.DEFAULT_COLOR;
import static info.trekto.jos.core.model.impl.SimulationObjectImpl.DEFAULT_COLOR_SIMPLIFIED;
import static info.trekto.jos.core.numbers.NumberFactory.NumberType.ARBITRARY_PRECISION;
import static info.trekto.jos.core.numbers.NumberFactoryProxy.ZERO;
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
            initialObjects.add(mapSimulationObjectToJson(gson, simulationObject, true, true, false));
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
        json.addProperty("saveMass", properties.isSaveMass());
        json.addProperty("saveVelocity", properties.isSaveVelocity());
        json.addProperty("saveAcceleration", properties.isSaveAcceleration());
        json.addProperty("numberType", properties.getNumberType().name());
        json.addProperty("interactingLaw", properties.getInteractingLaw().name());
        json.addProperty("precision", properties.getPrecision());
        json.addProperty("realTimeVisualization", properties.isRealTimeVisualization());
        json.addProperty("playingSpeed", properties.getPlayingSpeed());
        json.addProperty("bounceFromWalls", properties.isBounceFromWalls());
        return json;
    }

    private JsonObject mapSimulationObjectToJson(Gson gson, SimulationObject simulationObject, boolean saveMass, boolean saveVelocity,
                                                 boolean saveAcceleration) {
        Map<String, Object> simulationObjectMap = new HashMap<>();
        simulationObjectMap.put("id", simulationObject.getId());

        simulationObjectMap.put("x", simulationObject.getX().toString());
        simulationObjectMap.put("y", simulationObject.getY().toString());
        if (simulationObject.getZ().compareTo(ZERO) != 0) {
            simulationObjectMap.put("z", simulationObject.getZ().toString());
        }

        if (saveMass) {
            simulationObjectMap.put("mass", simulationObject.getMass().toString());
        }

        if (saveVelocity) {
            simulationObjectMap.put("velocityX", simulationObject.getVelocity().getX().toString());
            simulationObjectMap.put("velocityY", simulationObject.getVelocity().getY().toString());
            if (simulationObject.getVelocity().getZ().compareTo(ZERO) != 0) {
                simulationObjectMap.put("velocityZ", simulationObject.getVelocity().getZ().toString());
            }
        }

        if (saveAcceleration) {
            simulationObjectMap.put("accelerationX", simulationObject.getAcceleration().getX().toString());
            simulationObjectMap.put("accelerationY", simulationObject.getAcceleration().getY().toString());
            if (simulationObject.getAcceleration().getZ().compareTo(ZERO) != 0) {
                simulationObjectMap.put("accelerationZ", simulationObject.getAcceleration().getZ().toString());
            }
        }

        simulationObjectMap.put("radius", simulationObject.getRadius().toString());

        if (simulationObject.getColor() != DEFAULT_COLOR && simulationObject.getColor() != DEFAULT_COLOR_SIMPLIFIED) {
            simulationObjectMap.put("color", String.format("%08X", simulationObject.getColor()).substring(2));
        }
        return gson.toJsonTree(simulationObjectMap).getAsJsonObject();
    }

    private JsonObject mapSimulationObjectToJson(Gson gson, Object positionX, Object positionY, Object positionZ, Object velocityX, Object velocityY,
                                                 Object velocityZ, Object mass, Object radius, String id, int color, Object accelerationX,
                                                 Object accelerationY, Object accelerationZ, boolean saveMass, boolean saveVelocity,
                                                 boolean saveAcceleration) {
        Map<String, Object> simulationObjectMap = new HashMap<>();
        simulationObjectMap.put("id", id);

        simulationObjectMap.put("x", positionX);
        simulationObjectMap.put("y", positionY);
        if (positionZ != null) {
            simulationObjectMap.put("z", positionZ);
        }

        if (saveMass) {
            simulationObjectMap.put("mass", mass);
        }

        if (saveVelocity) {
            simulationObjectMap.put("velocityX", velocityX);
            simulationObjectMap.put("velocityY", velocityY);
            if (velocityZ != null) {
                simulationObjectMap.put("velocityZ", velocityZ);
            }
        }
        
        if (saveAcceleration) {
            simulationObjectMap.put("accelerationX", accelerationX);
            simulationObjectMap.put("accelerationY", accelerationY);
            if (accelerationZ != null) {
                simulationObjectMap.put("accelerationZ", accelerationZ);
            }
        }

        simulationObjectMap.put("radius", radius);

        if (color != DEFAULT_COLOR && color != DEFAULT_COLOR_SIMPLIFIED) {
            simulationObjectMap.put("color", String.format("%08X", color).substring(2));
        }
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
        String numberTypeFromFile = json.get("numberType").getAsString();
        String numberType = numberTypeFromFile.equals("APFLOAT") || numberTypeFromFile.equals("BIG_DECIMAL") ?
                ARBITRARY_PRECISION.name()
                : json.get("numberType").getAsString();
        properties.setNumberType(NumberFactory.NumberType.valueOf(numberType));
        properties.setPrecision(json.get("precision").getAsInt());

        createNumberFactory(properties.getNumberType(), properties.getPrecision());

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
        properties.setSaveMass(json.get("saveMass") != null && json.get("saveMass").getAsBoolean());
        properties.setSaveVelocity(json.get("saveVelocity") != null && json.get("saveVelocity").getAsBoolean());
        properties.setSaveAcceleration(json.get("saveAcceleration") != null && json.get("saveAcceleration").getAsBoolean());
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
            simo.setZ(o.get("z") != null ? New.num(o.get("z").getAsString()) : ZERO);

            simo.setMass(New.num(o.get("mass").getAsString()));

            Number velocityZ = ZERO;
            if (o.get("velocityZ") != null) {
                velocityZ = New.num(o.get("velocityZ").getAsString());
            } else if (o.get("speedZ") != null) {
                velocityZ = New.num(o.get("speedZ").getAsString());
            }

            simo.setVelocity(new TripleNumber(New.num((o.get("velocityX") != null ? o.get("velocityX"): o.get("speedX")).getAsString()),
                                              New.num((o.get("velocityY") != null ? o.get("velocityY"): o.get("speedY")).getAsString()),
                                              velocityZ));
                                           
            simo.setAcceleration(new TripleNumber());

            simo.setRadius(New.num(o.get("radius").getAsString()));
            
            if (o.get("color") != null) {
                simo.setColor(Integer.parseInt(o.get("color").getAsString(), 16));
            }

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
                o.setZ(node.get("z") != null ? New.num(node.get("z").asText()) : ZERO);
                o.setRadius(New.num(node.get("radius").asText()));
                if (node.get("color") != null) {
                    o.setColor(Integer.parseInt(node.get("color").getTextValue(), 16));
                }
                objects.add(o);
            }
        }
        parser.nextToken(); // Object end
        return new Iteration(cycle, numberOfObjects, objects);
    }

    @Override
    public void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, float[] positionX, float[] positionY,
                                    float[] positionZ, float[] velocityX, float[] velocityY, float[] velocityZ, float[] mass, float[] radius,
                                    String[] id, int[] color, boolean[] deleted, float[] accelerationX, float[] accelerationY,
                                    float[] accelerationZ) {
        Gson gson = createGsonAndWriteFileHead(properties);

        JsonArray objectsAsJsonArray = new JsonArray();
        for (int i = 0; i < positionX.length; i++) {
            if (!deleted[i]) {
                objectsAsJsonArray.add(mapSimulationObjectToJson(gson, positionX[i], positionY[i], zeroToNull(positionZ[i]), velocityX[i],
                                                                 velocityY[i], zeroToNull(velocityZ[i]), mass[i], radius[i], id[i], color[i],
                                                                 accelerationX[i], accelerationY[i], zeroToNull(accelerationZ[i]),
                                                                  properties.isSaveMass(), properties.isSaveVelocity(),
                                                                  properties.isSaveAcceleration()));
            }
        }

        writeFileTail(currentIterationNumber, objectsAsJsonArray, gson, properties);
    }

    private Object zeroToNull(float v) {
        return v == 0 ? null : v;
    }

    private Object zeroToNull(double v) {
        return v == 0 ? null : v;
    }

    @Override
    public void appendObjectsToFile(SimulationProperties properties, long currentIterationNumber, double[] positionX, double[] positionY,
                                    double[] positionZ, double[] velocityX, double[] velocityY, double[] velocityZ, double[] mass, double[] radius,
                                    String[] id, int[] color, boolean[] deleted, double[] accelerationX, double[] accelerationY,
                                    double[] accelerationZ) {
        Gson gson = createGsonAndWriteFileHead(properties);

        JsonArray objectsAsJsonArray = new JsonArray();
        for (int i = 0; i < positionX.length; i++) {
            if (!deleted[i]) {
                objectsAsJsonArray.add(mapSimulationObjectToJson(gson, positionX[i], positionY[i], zeroToNull(positionZ[i]), velocityX[i],
                                                                 velocityY[i], zeroToNull(velocityZ[i]), mass[i], radius[i], id[i], color[i],
                                                                 accelerationX[i], accelerationY[i], zeroToNull(accelerationZ[i]),
                                                                 properties.isSaveMass(), properties.isSaveVelocity(),
                                                                 properties.isSaveAcceleration()));
            }
        }

        writeFileTail(currentIterationNumber, objectsAsJsonArray, gson, properties);
    }

    @Override
    public void appendObjectsToFile(List<SimulationObject> simulationObjects, SimulationProperties properties, long currentIterationNumber) {
        Gson gson = createGsonAndWriteFileHead(properties);

        JsonArray objectsAsJsonArray = new JsonArray();
        for (SimulationObject simulationObject : simulationObjects) {
            objectsAsJsonArray.add(mapSimulationObjectToJson(gson, simulationObject, properties.isSaveMass(), properties.isSaveVelocity(),
                                                             properties.isSaveAcceleration()));
        }

        writeFileTail(currentIterationNumber, objectsAsJsonArray, gson, properties);
    }

    private void writeFileTail(long currentIterationNumber, JsonArray objectsAsJsonArray, Gson gson, SimulationProperties properties) {
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

    private Gson createGsonAndWriteFileHead(SimulationProperties properties) {
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
        return gson;
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
