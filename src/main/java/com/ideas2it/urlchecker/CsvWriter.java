package com.ideas2it.urlchecker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvWriter {

    public static void writeCsv(List<UrlStatus> results, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("URL,Status,Latency(ms),Attempt\n");
            for (UrlStatus status : results) {
                writer.write(String.format("%s,%s,%d,%d\n",
                    status.getUrl(),
                    status.isUp() ? "UP" : "DOWN",
                    status.getLatencyMillis(),
                    status.getAttempt()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
