package dev.fredpena.gatherers;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * @author me@fredpena.dev
 * @created 02/10/2024  - 15:28
 */
public class WindowFixed {


    public static void main(String[] args) {

        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.windowFixed(3)
                ).toList();

        windows.forEach(System.out::println);

    }
}
