package dev.fredpena.gatherers;

import java.util.Optional;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * @author me@fredpena.dev
 * @created 02/10/2024  - 15:28
 */
public class Fold {


    public static void main(String[] args) {

        Optional<String> numberString = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.fold(() -> "", (str, num) -> str + num)
                )
                .findFirst();

        System.out.println(numberString);

    }
}
