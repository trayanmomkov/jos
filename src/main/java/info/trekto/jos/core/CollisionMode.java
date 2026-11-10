package info.trekto.jos.core;

import info.trekto.jos.core.impl.SimulationProperties;

/* What happens when two objects overlap. The properties keep it as three flags. */
public enum CollisionMode {
    MERGE("Merge"),
    BOUNCE("Bounce"),
    SOFT_CONTACT("Soft contact (DEM)"),
    PASS_THROUGH("Pass through");

    private final String name;

    CollisionMode(String name) {
        this.name = name;
    }

    public static CollisionMode of(SimulationProperties properties) {
        if (!properties.isCollisionsEnabled()) {
            return PASS_THROUGH;
        }
        if (properties.isMergeOnCollision()) {
            return MERGE;
        }
        return properties.isSoftContact() ? SOFT_CONTACT : BOUNCE;
    }

    public void applyTo(SimulationProperties properties) {
        properties.setCollisionsEnabled(this != PASS_THROUGH);
        properties.setMergeOnCollision(this == MERGE);
        properties.setSoftContact(this == SOFT_CONTACT);
    }

    @Override
    public String toString() {
        return name;
    }
}
