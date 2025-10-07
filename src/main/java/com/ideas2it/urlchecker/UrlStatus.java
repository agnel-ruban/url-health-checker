package com.ideas2it.urlchecker;


public class UrlStatus {

    private final String url;
    private final boolean up;
    private final long latencyMillis;
    private final int attempt;

    public UrlStatus(String url, boolean up, long latencyMillis, int attempt) {
        this.url = url;
        this.up = up;
        this.latencyMillis = latencyMillis;
        this.attempt = attempt;
    }

    public String getUrl() {
        return url;
    }

    public boolean isUp() {
        return up;
    }

    public long getLatencyMillis() {
        return latencyMillis;
    }

    public int getAttempt() {
        return attempt;
    }

    @Override
    public String toString() {
        return url + " | " + (up ? "UP" : "DOWN") + " | latency=" + latencyMillis + "ms | attempt=" + attempt;
    }
}
