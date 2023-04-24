package info.trekto.jos.core.impl;

import static info.trekto.jos.util.Utils.deepCopy;

public class GpuData {
    public final String[] id;
    public final int[] color;
    public final boolean[] deleted;
    public final boolean[] readOnlyDeleted;
    public final int[] readOnlyColor;
    public final int n;

    public GpuData(int n) {
        this.n = n;
        id = new String[n];
        color = new int[n];
        deleted = new boolean[n];
        readOnlyDeleted = new boolean[n];
        readOnlyColor = new int[n];
    }
    
    public void copyToReadOnly() {
        deepCopy(deleted, readOnlyDeleted);
        deepCopy(color, readOnlyColor);
    }
}
