package info.trekto.jos.core.impl;

import info.trekto.jos.util.Utils;

public class RuntimeProperties {
    private boolean benchmarkMode = false;
    private int writerBufferSize = 0;
    private int numberOfThreads = Utils.CORES;

    public boolean isBenchmarkMode() {
        return benchmarkMode;
    }

    public void setBenchmarkMode(boolean benchmarkMode) {
        this.benchmarkMode = benchmarkMode;
    }

    public int getWriterBufferSize() {
        return writerBufferSize;
    }

    public void setWriterBufferSize(int writerBufferSize) {
        this.writerBufferSize = writerBufferSize;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
