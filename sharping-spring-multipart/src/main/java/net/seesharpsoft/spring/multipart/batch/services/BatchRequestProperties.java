package net.seesharpsoft.spring.multipart.batch.services;

public class BatchRequestProperties {

    public BatchRequestProperties() {};

    public BatchRequestProperties(BatchRequestProperties original) {
        if (original != null) {
            this.setIncludeOriginalHeader(original.getIncludeOriginalHeader());
            this.setParallelProcessing(original.isParallelProcessing());
            this.setThreadPoolSize(original.getThreadPoolSize());
        }
    }

    private boolean includeOriginalHeader = true;

    private boolean parallelProcessing = true;

    private int threadPoolSize = 10;

    public void setIncludeOriginalHeader(boolean includeOriginalHeader) {
        this.includeOriginalHeader = includeOriginalHeader;
    }

    public boolean getIncludeOriginalHeader() {
        return this.includeOriginalHeader;
    }

    public void setParallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing;
    }

    public boolean isParallelProcessing() {
        return this.parallelProcessing;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}
