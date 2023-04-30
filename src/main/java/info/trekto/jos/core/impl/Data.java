package info.trekto.jos.core.impl;

import static info.trekto.jos.util.Utils.deepCopy;

public class Data {
    public final String[] id;
    public final int[] color;
    public final boolean[] deleted;
    public final boolean[] readOnlyDeleted;
    public final int[] readOnlyColor;
    public final int n;

    public Data(int n) {
        this.n = n;
        id = new String[n];
        color = new int[n];
        deleted = new boolean[n];
        readOnlyDeleted = new boolean[n];
        readOnlyColor = new int[n];
    }

    public static int countObjects(Data data) {
        int numberOfObjects = 0;
        for (int j = 0; j < data.deleted.length; j++) {
            if (!data.deleted[j]) {
                numberOfObjects++;
            }
        }
        return numberOfObjects;
    }

    public void copyToReadOnly() {
        deepCopy(deleted, readOnlyDeleted);
        deepCopy(color, readOnlyColor);
    }
}
