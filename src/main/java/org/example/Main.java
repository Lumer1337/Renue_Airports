package org.example;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            ArgumentValidator validator = new ArgumentValidator(args);

            String dataFile = validator.getRequiredArgument("--data");
            String inputFile = validator.getRequiredArgument("--input-file");
            String outputFile = validator.getRequiredArgument("--output-file");
            int columnIndex = validator.getRequiredIntArgument("--indexed-column-id") - 1;

            long startTime = System.nanoTime();
            PrefixTree prefixTree = new PrefixTree();
            Set<String> queryPrefixes = loadQueryPrefixes(inputFile);
            buildIndex(dataFile, columnIndex, prefixTree, queryPrefixes);
            long initTime = System.nanoTime() - startTime;

            List<String> searchQueries = readLines(inputFile);

            List<SearchResult> results = new ArrayList<>();
            for (String query : searchQueries) {
                long searchStart = System.nanoTime();
                List<Integer> resultRows = prefixTree.search(query.trim());
                long searchTime = System.nanoTime() - searchStart;
                results.add(new SearchResult(query, resultRows, searchTime / 1_000_000));
            }

            OutputResult outputResult = new OutputResult(initTime / 1_000_000, results);
            writeJson(outputFile, outputResult);

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> readLines(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    private static void buildIndex(String dataFile, int indexedColumnId, PrefixTree prefixTree, Set<String> queryPrefixes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = parseCsvLine(line);
                if (indexedColumnId < columns.length) {
                    String columnValue = columns[indexedColumnId].trim();

                    boolean matchesQuery = queryPrefixes.stream().anyMatch(columnValue::startsWith);
                    if (!matchesQuery) {
                        continue;
                    }

                    int rowNumber = Integer.parseInt(columns[0]);
                    prefixTree.insert(columnValue, rowNumber);
                }
            }
        }
    }

    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ',' && !insideQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (insideQuotes) {
            throw new IllegalArgumentException("Недопустимый формат CSV: несовпадающие кавычки в строке: " + line);
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private static void writeJson(String filePath, OutputResult result) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            writer.write(result.toJson());
        }
    }

    private static Set<String> loadQueryPrefixes(String inputFile) throws IOException {
        List<String> queries = readLines(inputFile);
        return queries.stream().map(String::trim).collect(Collectors.toSet());
    }
}