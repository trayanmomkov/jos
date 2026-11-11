package info.trekto.jos.core.impl;

/**
 * Calculates the parameters of the soft contact (DEM) from the scene.
 * The force between two overlapping objects is:
 * effectiveMass * (stiffness * overlap + damping * approachSpeed), never negative,
 * where effectiveMass = m1 * m2 / (m1 + m2). The spring of a pair is proportional to its masses,
 * so a contact lasts equally long for light and for heavy objects.
 * The stiffness is chosen so that neither the fastest object nor the weight of a pile of objects
 * presses two objects into each other deeper than a small part of the radius.
 * A stiff contact needs small steps, so one iteration is split into subSteps.
 */
public class SoftContactParameters {
    /* A contact deeper than this part of the smallest radius is too soft. */
    private static final double OVERLAP_FRACTION = 0.05;
    /* Small steps per oscillation of the contact spring. */
    private static final double CONTACT_RESOLUTION = 30.0;
    private static final double GRAVITY = 0.000000000066743; // 6.6743e-11

    /** Stiffness of the spring for a unit of effective mass (1/s^2). */
    public final double stiffness;
    /** Damping for a unit of effective mass (1/s). */
    public final double damping;
    /** Number of small steps per iteration. At least 1. */
    public final int subSteps;
    public final double subStepSeconds;
    /** Whether the weight of a pile, and not the fastest object, has set the stiffness (for logging). */
    public final boolean gravityLimited;

    private SoftContactParameters(double stiffness, double damping, int subSteps, double subStepSeconds,
                                  boolean gravityLimited) {
        this.stiffness = stiffness;
        this.damping = damping;
        this.subSteps = subSteps;
        this.subStepSeconds = subStepSeconds;
        this.gravityLimited = gravityLimited;
    }

    public static SoftContactParameters derive(int n, double[] x, double[] y, double[] vx, double[] vy,
                                               double[] mass, double[] radius, boolean[] deleted,
                                               double secondsPerIteration, double coefficientOfRestitution,
                                               double minDistance) {
        double minRadius = Double.MAX_VALUE;
        double maxSpeed = 0;
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (!deleted[i]) {
                count++;
                minRadius = Math.min(minRadius, radius[i]);
                maxSpeed = Math.max(maxSpeed, Math.sqrt(vx[i] * vx[i] + vy[i] * vy[i]));
            }
        }
        double overlap = OVERLAP_FRACTION * minRadius;

        /* The spring stops the fastest object within the overlap: speed^2 = stiffness * overlap^2. */
        double impactStiffness = maxSpeed * maxSpeed / (overlap * overlap);

        /* An object under a pile of about sqrt(count) others does not sink deeper than the overlap. */
        double gravityStiffness = 2 * maxGravity(n, x, y, mass, radius, deleted, minDistance, minRadius) * Math.sqrt(count) / overlap;

        boolean gravityLimited = gravityStiffness > impactStiffness;
        double stiffness = gravityLimited ? gravityStiffness : impactStiffness;

        /* The damping which gives the coefficient of restitution e: -2 ln(e) / sqrt(pi^2 + ln(e)^2) * sqrt(stiffness). */
        double le = Math.log(Math.max(coefficientOfRestitution, 1e-4));
        double damping = -2.0 * le / Math.sqrt(Math.PI * Math.PI + le * le) * Math.sqrt(stiffness);

        double dt = Math.max(secondsPerIteration, 0);
        int subSteps = 1;
        double contactPeriod = 2.0 * Math.PI / Math.sqrt(stiffness);
        double safeStep = contactPeriod / CONTACT_RESOLUTION;
        if (dt > safeStep) {
            subSteps = (int) Math.ceil(dt / safeStep);
        }

        return new SoftContactParameters(stiffness, damping, subSteps, dt / subSteps, gravityLimited);
    }

    /* The strongest gravity an object can feel: where it is now, or lying on the surface of another object. */
    private static double maxGravity(int n, double[] x, double[] y, double[] mass, double[] radius, boolean[] deleted,
                                     double minDistance, double minRadius) {
        double maxGravity = 0;
        for (int i = 0; i < n; i++) {
            if (deleted[i]) {
                continue;
            }
            double surface = Math.max(radius[i] + minRadius, minDistance);
            maxGravity = Math.max(maxGravity, GRAVITY * mass[i] / (surface * surface));

            double ax = 0;
            double ay = 0;
            for (int j = 0; j < n; j++) {
                if (i == j || deleted[j]) {
                    continue;
                }
                double dx = x[j] - x[i];
                double dy = y[j] - y[i];
                double distance = Math.max(Math.sqrt(dx * dx + dy * dy), minDistance);
                if (distance <= 0) {
                    continue;
                }
                double a = GRAVITY * mass[j] / (distance * distance);
                ax += a * dx / distance;
                ay += a * dy / distance;
            }
            maxGravity = Math.max(maxGravity, Math.sqrt(ax * ax + ay * ay));
        }
        return maxGravity;
    }

    @Override
    public String toString() {
        return String.format("SoftContact[stiffness=%.3e damping=%.3e subSteps=%d subStepSeconds=%.3e %s]",
                             stiffness, damping, subSteps, subStepSeconds,
                             gravityLimited ? "gravity-limited" : "impact-limited");
    }
}
