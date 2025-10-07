package com.ideas2it.urlchecker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlHealthChecker {

    private static final int MAX_RETRIES = 2;

    public static void main(String[] args) throws IOException, ParseException {

        Options options = new Options();
        options.addOption("p", "pool", true, "Thread pool size (default 8)");
        options.addOption("m", "max-permits", true, "Maximum concurrent checks (default 5)");
        options.addOption("t", "timeout", true, "Timeout per URL in seconds (default 3)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        int poolSize = Integer.parseInt(cmd.getOptionValue("pool", "8"));
        int maxPermits = Integer.parseInt(cmd.getOptionValue("max-permits", "5"));
        int timeoutSeconds = Integer.parseInt(cmd.getOptionValue("timeout", "3"));

        System.out.printf("Using pool=%d, maxPermits=%d, timeout=%ds%n", poolSize, maxPermits, timeoutSeconds);


        // Step 1: Read URLs from urls.txt and deduplicate
        List<String> urls = Files.readAllLines(Paths.get("urls.txt"));
        urls = new ArrayList<>(new HashSet<>(urls)); // deduplicate

        System.out.println("Total URLs to check: " + urls.size());

        // Step 2: Create ResultIndex and HealthChecker
        ResultIndex resultIndex = new ResultIndex();
        Metrics metrics = new Metrics();

        HealthChecker checker = new HealthChecker(poolSize, maxPermits, timeoutSeconds, MAX_RETRIES, resultIndex, metrics);

        // Step 3: Periodic progress logging every 5 seconds
        ScheduledExecutorService progressLogger = Executors.newScheduledThreadPool(1);
        List<String> finalUrls = urls;
        progressLogger.scheduleAtFixedRate(() -> {
            List<UrlStatus> snapshot = resultIndex.snapshot();
            checker.printMetrics();
            System.out.println("Progress: " + snapshot.size() + "/" + finalUrls.size() + " URLs checked");
        }, 0, 1, TimeUnit.SECONDS);

        // Step 4: Add shutdown hook to handle Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down...");
            checker.shutdown();
            CsvWriter.writeCsv(resultIndex.snapshot(), "results.csv");
            progressLogger.shutdownNow();
            System.out.println("Partial results written to results.csv");
        }));

        // Step 5: Submit URLs for checking
        checker.submitUrls(urls);

        // Step 6: Wait for all tasks to complete
        checker.shutdown();

        // Step 7: Write final CSV
        CsvWriter.writeCsv(resultIndex.snapshot(), "results.csv");
        progressLogger.shutdownNow();

        System.out.println("Done! Results written to results.csv");
    }
}

