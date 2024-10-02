package dev.fredpena;

import java.util.List;

/**
 * Hello world!
 */
public class ExampleStream {

    private static long countLongWords(List<String> words, int minLength) {
        return words.stream()
                .map(String::length)
                .filter(length -> length >= minLength)
                .count();
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

        long count = countLongWords(words, 5);
        System.out.println(count);

    }
}
