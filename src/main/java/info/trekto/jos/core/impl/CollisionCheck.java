package info.trekto.jos.core.impl;

import com.aparapi.Kernel;

import java.util.Arrays;

public class CollisionCheck extends Kernel {
    public final boolean[] collisions;
    public final int n;

    public final double[] positionX;
    public final double[] positionY;
    public final double[] radius;
    public final boolean[] deleted;

    public CollisionCheck(int n, double[] positionX, double[] positionY, double[] radius, boolean[] deleted) {
        this.n = n;
        collisions = new boolean[n];

        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        this.deleted = deleted;
    }

    public void prepare() {
        Arrays.fill(collisions, false);
    }

    public boolean collisionExists() {
        for (boolean collision : collisions) {
            if (collision) {
                return true;
            }
        }
        return false;
    }

    /**
     * !!! DO NOT CHANGE THIS METHOD and methods called from it if you don't have experience with Aparapi library!!!
     * This code is translated to OpenCL and executed on the GPU.
     * You cannot use even simple 'break' here - it is not supported by Aparapi.
     */
    @Override
    public void run() {
        int i = getGlobalId();
        if (!deleted[i]) {
            boolean collision = false;
            for (int j = 0; j < n; j++) {
                if (!collision && i != j && !deleted[j]) {
                    // distance between centres
                    double x = positionX[j] - positionX[i];
                    double y = positionY[j] - positionY[i];
                    double distance = Math.sqrt(x * x + y * y);

                    if (distance < radius[i] + radius[j]) {
                        collision = true;
                        collisions[i] = true;
                    }
                }
            }
        }
    }
}
