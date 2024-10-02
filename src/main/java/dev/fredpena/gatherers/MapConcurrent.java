package dev.fredpena.gatherers;

import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * @author me@fredpena.dev
 * @created 02/10/2024  - 15:28
 */
public class MapConcurrent {


    public static void main(String[] args) {

        List<String> numberStrings = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.mapConcurrent(2, Object::toString)
                )
                .toList();

        numberStrings.forEach(System.out::println);

    }
}
