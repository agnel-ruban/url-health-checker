package com.ideas2it.urlchecker;

import java.util.concurrent.atomic.LongAdder;

public class Metrics {

    private final LongAdder total = new LongAdder();
    private final LongAdder success = new LongAdder();
    private final LongAdder failed = new LongAdder();
    private final LongAdder retried = new LongAdder();

    public void incrementTotal() {
        total.increment();
    }

    public void incrementSuccess() {
        success.increment();
    }

    public void incrementFailed() {
        failed.increment();
    }

    public void incrementRetried() {
        retried.increment();
    }

    public long getTotal() {
        return total.sum();
    }

    public long getSuccess() {
        return success.sum();
    }

    public long getFailed() {
        return failed.sum();
    }

    public long getRetried() {
        return retried.sum();
    }

    @Override
    public String toString() {
        return "Metrics{" +
            "total=" + getTotal() +
            ", success=" + getSuccess() +
            ", failed=" + getFailed() +
            ", retried=" + getRetried() +
            '}';
    }
}
