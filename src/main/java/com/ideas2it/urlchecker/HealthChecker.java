package com.ideas2it.urlchecker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class HealthChecker {

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Semaphore semaphore;
    private final ResultIndex index;
    private final Metrics metrics;
    private final int maxRetries;
    private final int timeoutSeconds;

    public HealthChecker(int poolSize, int maxPermits, int timeoutSeconds, int maxRetries, ResultIndex index, Metrics metrics) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.semaphore = new Semaphore(maxPermits);
        this.timeoutSeconds = timeoutSeconds;
        this.maxRetries = maxRetries;
        this.index = index;
        this.metrics = metrics;
    }

    public void submitUrls(List<String> urls) {
        for (String url : urls) {
            submitUrlTask(url, 1);
        }
    }

    private void submitUrlTask(String url, int attempt) {
        if (url == null || url.trim().isEmpty() || url.isBlank()) return;
        executor.submit(() -> {
            try {
                semaphore.acquire();
                UrlTask task = new UrlTask(url, attempt, timeoutSeconds);
                UrlStatus status = task.call();

                metrics.incrementTotal();
                if (status.isUp()) metrics.incrementSuccess();
                else metrics.incrementFailed();

                index.addResult(status);

                // Retry logic
                if (!status.isUp() && attempt < maxRetries) {
                    metrics.incrementRetried();
                    int nextAttempt = attempt + 1;
                    long backoff = (long) Math.pow(2, attempt) * 1000; // exponential backoff
                    System.out.printf("Retrying %s in %d ms (attempt %d)%n", url, backoff, nextAttempt);
                    scheduler.schedule(() -> submitUrlTask(url, nextAttempt), backoff, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        });
    }

    public void shutdown() {
        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
            scheduler.shutdown();
            scheduler.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printMetrics() {
        System.out.println(metrics);
    }
}
