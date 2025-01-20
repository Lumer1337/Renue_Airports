package org.example;

import java.util.HashMap;
import java.util.Map;

public class ArgumentValidator {
    private final Map<String, String> arguments;

    public ArgumentValidator(String[] args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Аргументы должны быть в виде пар ключ-значение");
        }

        arguments = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (arguments.containsKey(args[i])) {
                throw new IllegalArgumentException("Дублирующийся ключ аргумента: " + args[i]);
            }
            arguments.put(args[i], args[i + 1]);
        }
    }


    public String getRequiredArgument(String key) {
        String value = arguments.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Ошибка: " + key + " не указано");
        }
        return value;
    }

    public int getRequiredIntArgument(String key) {
        String value = getRequiredArgument(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверное значение для " + key);
        }
    }
}
