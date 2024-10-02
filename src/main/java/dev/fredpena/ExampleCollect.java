package dev.fredpena;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hello world!
 */
public class ExampleCollect {

    private static Map<Integer, List<String>> groupByLength(List<String> words) {
        return words.stream()
                .map(String::toUpperCase)
                .collect(Collectors.groupingBy(String::length));
    }

    public static void main(String[] args) {

        List<String> words = List.of(
                "Java",
                "Stream",
                "Gatherers",
                "JVM",
                "JDBC",
                "JDK",
                "Garbage Collection",
                "Multithreading"
        );

        Map<Integer, List<String>> groupByLength = groupByLength(words);
        System.out.println(groupByLength);

    }
}
