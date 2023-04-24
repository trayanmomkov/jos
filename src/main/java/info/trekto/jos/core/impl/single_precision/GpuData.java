package info.trekto.jos.core.impl.single_precision;

import static info.trekto.jos.util.Utils.deepCopy;

public class GpuData {
    public String[] id;
    public int[] color;
    public boolean[] deleted;
    public boolean[] readOnlyDeleted;
    public int[] readOnlyColor;
    public int n;

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
