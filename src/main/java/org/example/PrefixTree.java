package org.example;

import java.util.*;

public class PrefixTree {
    static class Node {
        Map<Character, Node> children = new HashMap<>();
        List<Integer> rowNumbers = new ArrayList<>();
    }

    private final Node root;

    public PrefixTree() {
        this.root = new Node();
    }

    public void insert(String word, int rowNumber) {
        word = word.intern();

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
        return collectRows(current);
    }

    private List<Integer> collectRows(Node node) {
        List<Integer> result = new ArrayList<>(node.rowNumbers);
        for (Node child : node.children.values()) {
            result.addAll(collectRows(child));
        }
        return result;
    }
}