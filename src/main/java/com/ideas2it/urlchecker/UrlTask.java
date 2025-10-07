package com.ideas2it.urlchecker;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class UrlTask implements Callable<UrlStatus> {

    private final String url;
    private final int attempt;
    private final int timeoutSeconds;

    public UrlTask(String url, int attempt, int timeoutSeconds) {
        this.url = url;
        this.attempt = attempt;
        this.timeoutSeconds = timeoutSeconds;
    }


    @Override
    public UrlStatus call() {
        long start = System.currentTimeMillis();
        boolean up = false;

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeoutSeconds * 1000);
            connection.setReadTimeout(timeoutSeconds * 1000);
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            System.out.println("The code for this url == " + url + "  is number   =======   " + code);
            if (code >= 200 && code < 400) {
                up = true;
            }
        } catch (Exception e) {
            up = false;
        }

        long latency = System.currentTimeMillis() - start;
        return new UrlStatus(url, up, latency, attempt);
    }
}
