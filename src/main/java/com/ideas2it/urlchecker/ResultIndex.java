package com.ideas2it.urlchecker;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResultIndex {

    private final Map<String, UrlStatus> results = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public void addResult(UrlStatus status) {
        results.put(status.getUrl(), status);
    }

    public List<UrlStatus> snapshot() {
        rwLock.readLock().lock();
        try {
            return new ArrayList<>(results.values());
        } finally {
            rwLock.readLock().unlock();
        }
    }

}
