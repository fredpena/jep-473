package dev.fredpena;

import java.util.List;
import java.util.stream.Gatherers;

/**
 * Hello world!
 */
public class ExampleGather {

    private static List<List<String>> groupOfThree(List<String> words) {
        return words.stream()
                .gather(Gatherers.windowFixed(3))
                .toList();
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

        List<List<String>> groupOfThree = groupOfThree(words);
        System.out.println(groupOfThree);

    }
}
