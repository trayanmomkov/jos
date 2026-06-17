package info.trekto.jos.core.impl;

/**
 * Calculates the parameters of the soft contact (DEM) from the scene.
 * The force between two overlapping objects is: stiffness * overlap - damping * approachSpeed (never negative).
 * The stiffness is chosen so that neither the fastest object nor the weight of the whole body
 * presses two objects into each other deeper than a small part of the radius.
 * A stiff contact needs small steps, so one iteration is split into subSteps.
 */
public class SoftContactParameters {
    /** Contacts deeper than this fraction of a radius are considered too soft (sets the stiffness). */
    public static final double DEFAULT_OVERLAP_FRACTION = 0.05;
    /** Internal steps per contact oscillation period - the integrator must resolve the spring. */
    private static final double CONTACT_RESOLUTION = 30.0;
    /** An object is "real" (not count padding) if its mass is at least this fraction of the largest. */
    private static final double PADDING_MASS_FRACTION = 1e-9;
    private static final double GRAVITY = 0.000000000066743; // 6.6743e-11

    /** Spring stiffness k (N/m). Zero means the model is inert (nothing can collide). */
    public final double stiffness;
    /** Damping factor d such that the damping coefficient of a pair is {@code d*sqrt(mEff*k)}. */
    public final double dampingFactor;
    /** Number of internal integration steps per visible iteration. At least 1. */
    public final int subSteps;
    /** Seconds advanced per internal step ({@code secondsPerIteration / subSteps}). */
    public final double subStepSeconds;
    /** The overlap fraction the stiffness was sized for (for logging). */
    public final double overlapFraction;
    /** Whether the impact or the self-gravity criterion set the stiffness (for logging). */
    public final boolean gravityLimited;
    /** Number of real (non-padding) objects the derivation used (for logging). */
    public final int realCount;

    private SoftContactParameters(double stiffness, double dampingFactor, int subSteps, double subStepSeconds,
                                  double overlapFraction, boolean gravityLimited, int realCount) {
        this.stiffness = stiffness;
        this.dampingFactor = dampingFactor;
        this.subSteps = subSteps;
        this.subStepSeconds = subStepSeconds;
        this.overlapFraction = overlapFraction;
        this.gravityLimited = gravityLimited;
        this.realCount = realCount;
    }

    /** Convenience: derive with the default overlap fraction. */
    public static SoftContactParameters derive(int n, double[] x, double[] y, double[] vx, double[] vy,
                                               double[] mass, double[] radius, boolean[] deleted,
                                               double secondsPerIteration, double coefficientOfRestitution,
                                               double minDistance) {
        return derive(n, x, y, vx, vy, mass, radius, deleted, secondsPerIteration, coefficientOfRestitution,
                      minDistance, DEFAULT_OVERLAP_FRACTION);
    }

    public static SoftContactParameters derive(int n, double[] x, double[] y, double[] vx, double[] vy,
                                               double[] mass, double[] radius, boolean[] deleted,
                                               double secondsPerIteration, double coefficientOfRestitution,
                                               double minDistance, double overlapFraction) {
        double dt = secondsPerIteration > 0 ? secondsPerIteration : 0;

        /* Largest mass, to tell real objects from the near-massless count padding. */
        double maxMass = 0;
        for (int i = 0; i < n; i++) {
            if (!deleted[i] && mass[i] > maxMass) {
                maxMass = mass[i];
            }
        }
        if (maxMass <= 0) {
            return inert(dt);
        }
        double realThreshold = maxMass * PADDING_MASS_FRACTION;

        /* Representative particle = the smallest real one (hardest to hold apart); characteristic
         * speed = the fastest real one (worst-case impact). */
        double minMass = Double.MAX_VALUE;
        double minRadius = Double.MAX_VALUE;
        double maxSpeed = 0;
        int real = 0;
        for (int i = 0; i < n; i++) {
            if (deleted[i] || mass[i] < realThreshold) {
                continue;
            }
            real++;
            if (mass[i] < minMass) {
                minMass = mass[i];
            }
            if (radius[i] > 0 && radius[i] < minRadius) {
                minRadius = radius[i];
            }
            double speed = Math.sqrt(vx[i] * vx[i] + vy[i] * vy[i]);
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
        }
        if (real == 0 || minRadius == Double.MAX_VALUE || minRadius <= 0) {
            return inert(dt);
        }

        double mEff = minMass / 2.0;
        double overlapLength = overlapFraction * minRadius;

        double kImpact = mEff * maxSpeed * maxSpeed / (overlapLength * overlapLength);

        /* Self-gravity floor: the largest gravitational force on any single real object, times the
         * estimated pile depth (~sqrt of the real count), must not crush a contact past the overlap. */
        double maxGravityForce = maxGravityForce(n, x, y, mass, deleted, realThreshold, minDistance);
        double depth = Math.sqrt(real);
        double kGravity = maxGravityForce * depth / overlapLength;

        boolean gravityLimited = kGravity > kImpact;
        double k = gravityLimited ? kGravity : kImpact;
        if (k <= 0) {
            return inert(dt);
        }

        /* Damping coefficient for the target restitution: c = -2 ln(e) sqrt(mEff k)/sqrt(pi^2+ln(e)^2).
         * Stored as the per-pair factor d = -2 ln(e)/sqrt(pi^2+ln(e)^2); the pair multiplies by sqrt(mEff k). */
        double le = Math.log(Math.max(coefficientOfRestitution, 1e-4));
        double dampingFactor = -2.0 * le / Math.sqrt(Math.PI * Math.PI + le * le);

        int subSteps = 1;
        double subStepSeconds = dt;
        if (dt > 0) {
            double contactPeriod = 2.0 * Math.PI * Math.sqrt(mEff / k);
            double safeStep = contactPeriod / CONTACT_RESOLUTION;
            if (safeStep > 0 && dt > safeStep) {
                subSteps = (int) Math.ceil(dt / safeStep);
            }
            subStepSeconds = dt / subSteps;
        }

        return new SoftContactParameters(k, dampingFactor, subSteps, subStepSeconds, overlapFraction, gravityLimited, real);
    }

    /** The largest gravitational force magnitude on any single real object, with softening. O(real^2). */
    private static double maxGravityForce(int n, double[] x, double[] y, double[] mass, boolean[] deleted,
                                          double realThreshold, double minDistance) {
        double maxForce = 0;
        for (int i = 0; i < n; i++) {
            if (deleted[i] || mass[i] < realThreshold) {
                continue;
            }
            double ax = 0;
            double ay = 0;
            for (int j = 0; j < n; j++) {
                if (i == j || deleted[j] || mass[j] < realThreshold) {
                    continue;
                }
                double dx = x[j] - x[i];
                double dy = y[j] - y[i];
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < minDistance) {
                    distance = minDistance;
                }
                if (distance <= 0) {
                    continue;
                }
                double a = GRAVITY * mass[j] / (distance * distance);
                ax += a * dx / distance;
                ay += a * dy / distance;
            }
            double force = mass[i] * Math.sqrt(ax * ax + ay * ay);
            if (force > maxForce) {
                maxForce = force;
            }
        }
        return maxForce;
    }

    private static SoftContactParameters inert(double dt) {
        return new SoftContactParameters(0, 0, 1, dt, DEFAULT_OVERLAP_FRACTION, false, 0);
    }

    @Override
    public String toString() {
        return String.format("SoftContact[k=%.3e damping=%.3f subSteps=%d subStepSeconds=%.3e %s realObjects=%d]",
                             stiffness, dampingFactor, subSteps, subStepSeconds,
                             gravityLimited ? "gravity-limited" : "impact-limited", realCount);
    }
}
