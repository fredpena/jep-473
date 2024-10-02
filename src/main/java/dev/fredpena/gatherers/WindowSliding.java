package dev.fredpena.gatherers;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * @author me@fredpena.dev
 * @created 02/10/2024  - 15:28
 */
public class WindowSliding {


    public static void main(String[] args) {

        List<List<Integer>> windows1 = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.windowSliding(2)
                ).toList();

        List<List<Integer>> windows2 = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.windowSliding(4)
                ).toList();

        System.out.println("=== Start Windows 1 ===");
        windows1.forEach(System.out::println);
        System.out.println("=== End Windows 1 ===");

        System.out.println("=== Start Windows 2 ===");
        windows2.forEach(System.out::println);
        System.out.println("=== End Windows 2 ===");

    }
}
