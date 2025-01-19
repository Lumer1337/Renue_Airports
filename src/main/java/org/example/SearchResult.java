package org.example;

import java.util.Collections;
import java.util.List;

public class SearchResult {
    private final String search;
    private final List<Integer> result;
    private final long time;

    SearchResult(String search, List<Integer> result, long time) {
        this.search = search;
        this.result = result;
        Collections.sort(result);
        this.time = time;
    }

    String toJson() {
        return String.format("\t{\"search\":\"%s\", \"result\":%s, \"time\":%d}", search, result, time);
    }
}