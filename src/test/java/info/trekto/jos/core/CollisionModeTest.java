package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;
import org.testng.annotations.Test;

import static info.trekto.jos.core.CollisionMode.BOUNCE;
import static info.trekto.jos.core.CollisionMode.MERGE;
import static info.trekto.jos.core.CollisionMode.PASS_THROUGH;
import static info.trekto.jos.core.CollisionMode.SOFT_CONTACT;
import static org.testng.Assert.assertEquals;

public class CollisionModeTest {
    @Test
    public void everyModeComesBackFromTheProperties() {
        for (CollisionMode mode : CollisionMode.values()) {
            SimulationProperties properties = new SimulationProperties();
            mode.applyTo(properties);
            assertEquals(CollisionMode.of(properties), mode);
            assertEquals(properties.useSoftContact(), mode == SOFT_CONTACT);
        }
    }

    @Test
    public void theFlagsOfAFileGiveTheModeWhichTheSimulationUses() {
        assertEquals(mode(false, true, true), PASS_THROUGH);
        assertEquals(mode(true, true, true), MERGE);
        assertEquals(mode(true, false, true), SOFT_CONTACT);
        assertEquals(mode(true, false, false), BOUNCE);
    }

    private static CollisionMode mode(boolean collisions, boolean mergeOnCollision, boolean softContact) {
        SimulationProperties properties = new SimulationProperties();
        properties.setCollisionsEnabled(collisions);
        properties.setMergeOnCollision(mergeOnCollision);
        properties.setSoftContact(softContact);
        return CollisionMode.of(properties);
    }
}
