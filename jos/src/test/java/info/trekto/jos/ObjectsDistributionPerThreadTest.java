package info.trekto.jos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import info.trekto.jos.core.impl.SimulationImpl;

public class ObjectsDistributionPerThreadTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testGetObjectsDistributionPerThread() {

        SimulationImpl simulation = new SimulationImpl();

        Assert.assertEquals(simulation.getObjectsDistributionPerThread(1, 0).toString(), "[0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(1, 1).toString(), "[1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(1, 2).toString(), "[2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(1, 3).toString(), "[3]");

        simulation = new SimulationImpl();

        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 0).toString(), "[0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 1).toString(), "[1, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 2).toString(), "[1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 3).toString(), "[2, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 4).toString(), "[2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 5).toString(), "[3, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 6).toString(), "[3, 3]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 7).toString(), "[4, 3]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(2, 8).toString(), "[4, 4]");

        simulation = new SimulationImpl();

        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 0).toString(), "[0, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 1).toString(), "[1, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 2).toString(), "[1, 1, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 3).toString(), "[1, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 4).toString(), "[2, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 5).toString(), "[2, 2, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 6).toString(), "[2, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 7).toString(), "[3, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 8).toString(), "[3, 3, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 9).toString(), "[3, 3, 3]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(3, 10).toString(), "[4, 3, 3]");

        simulation = new SimulationImpl();

        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 0).toString(), "[0, 0, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 1).toString(), "[1, 0, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 2).toString(), "[1, 1, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 3).toString(), "[1, 1, 1, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 4).toString(), "[1, 1, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 5).toString(), "[2, 1, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 6).toString(), "[2, 2, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 7).toString(), "[2, 2, 2, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 8).toString(), "[2, 2, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 9).toString(), "[3, 2, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 10).toString(), "[3, 3, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 11).toString(), "[3, 3, 3, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 12).toString(), "[3, 3, 3, 3]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(4, 13).toString(), "[4, 3, 3, 3]");

        simulation = new SimulationImpl();

        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 0).toString(), "[0, 0, 0, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 1).toString(), "[1, 0, 0, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 2).toString(), "[1, 1, 0, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 3).toString(), "[1, 1, 1, 0, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 4).toString(), "[1, 1, 1, 1, 0]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 5).toString(), "[1, 1, 1, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 6).toString(), "[2, 1, 1, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 7).toString(), "[2, 2, 1, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 8).toString(), "[2, 2, 2, 1, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 9).toString(), "[2, 2, 2, 2, 1]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 10).toString(), "[2, 2, 2, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 11).toString(), "[3, 2, 2, 2, 2]");
        Assert.assertEquals(simulation.getObjectsDistributionPerThread(5, 12).toString(), "[3, 3, 2, 2, 2]");
    }
}

