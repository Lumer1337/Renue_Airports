package org.example;

import java.util.Collections;
import java.util.List;

public class SearchResult {
    String search;
    List<Integer> result;
    long time;

    SearchResult(String search, List<Integer> result, long time) {
        this.search = search;
        this.result = result;
        if (result != null) {
            Collections.sort(result);
        }
        this.time = time;
    }

    String toJson() {
        return String.format("\t{\"search\":\"%s\", \"result\":%s, \"time\":%d}", search, result, time);
    }
}