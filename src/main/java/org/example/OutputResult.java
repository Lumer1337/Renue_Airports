package org.example;

import java.util.List;

public class OutputResult {
    long initTime;
    List<SearchResult> results;

    OutputResult(long initTime, List<SearchResult> results) {
        this.initTime = initTime;
        this.results = results;
    }

    String toJson() {
        StringBuilder json = new StringBuilder();
        json.append(String.format("{\"initTime\":%d, \"result\":[\n", initTime));
        for (int i = 0; i < results.size(); i++) {
            json.append(results.get(i).toJson());
            if (i < results.size() - 1) {
                json.append(",\n");
            }
        }
        json.append("\n]}");
        return json.toString();
    }
}
