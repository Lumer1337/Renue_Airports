package org.example;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            Map<String, String> arguments = parseArguments(args);

            String dataFile = arguments.get("--data");
            if (dataFile == null) {
                throw new IllegalArgumentException("Ошибка: --data не указано");
            }

            String inputFile = arguments.get("--input-file");
            if (inputFile == null) {
                throw new IllegalArgumentException("Ошибка: --input-file не указано");
            }

            String outputFile = arguments.get("--output-file");
            if (outputFile == null) {
                throw new IllegalArgumentException("Ошибка: --output-file не указано");
            }

            String columnIndexStr = arguments.get("--indexed-column-id");
            if (columnIndexStr == null) {
                throw new IllegalArgumentException("Ошибка: --indexed-column-id не указано");
            }

            int columnIndex;
            try {
                columnIndex = Integer.parseInt(columnIndexStr) - 1;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Неверное значение для --indexed-column-id");
            }

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

    private static Map<String, String> parseArguments(String[] args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Аргументы должны быть в виде пар ключ-значение");
        }

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
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