package org.example;

import java.util.*;

public class PrefixTree {
    private static class Node {
        private final Map<Character, Node> children = new HashMap<>();
        private final List<Integer> rowNumbers = new ArrayList<>();
    }

    private final Node root;

    public PrefixTree() {
        this.root = new Node();
    }

    public void insert(String word, int rowNumber) {
        Node current = root;
        for (char c : word.toCharArray()) {
            current.children.putIfAbsent(c, new Node());
            current = current.children.get(c);
        }

        if (!current.rowNumbers.contains(rowNumber)) {
            current.rowNumbers.add(rowNumber);
        }
    }

    public List<Integer> search(String prefix) {
        Node current = root;
        for (char c : prefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return Collections.emptyList();
            }
        }
        List<Integer> result = new ArrayList<>();
        collectRows(current, result);
        return result;
    }

    private void collectRows(Node node, List<Integer> result) {
        result.addAll(node.rowNumbers);
        for (Node child : node.children.values()) {
            collectRows(child, result);
        }
    }
}