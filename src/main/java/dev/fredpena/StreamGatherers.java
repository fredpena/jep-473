package dev.fredpena;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Gatherer;

/**
 * @author me@fredpena.dev
 * @created 02/10/2024  - 15:13
 */
public class StreamGatherers {

    public static <T> Gatherer<T, AtomicReference<T>, T> maximumBy(Comparator<T> comparator) {

        return Gatherer.of(
                // Initializer
                AtomicReference::new,

                // Integrator
                Gatherer.Integrator.ofGreedy(
                        (state, element, downstream) -> {
                            T bestElement = state.get();
                            if (bestElement == null || comparator.compare(element, bestElement) > 0) {
                                state.set(element);
                            }
                            return true;
                        }),

                // Combiner
                (state1, state2) -> {
                    T bestElement1 = state1.get();
                    T bestElement2 = state2.get();

                    if (bestElement1 == null) {
                        return state2;
                    } else if (bestElement2 == null) {
                        return state1;
                    } else if (comparator.compare(bestElement1, bestElement2) > 0) {
                        return state1;
                    } else {
                        return state2;
                    }
                },

                // Finisher
                (state, downstream) -> {
                    T bestElement = state.get();
                    if (bestElement != null) {
                        downstream.push(bestElement);
                    }
                }
        );
    }

    private static Optional<String> getLongest(List<String> words) {
        return words.parallelStream()
                .gather(maximumBy(Comparator.comparing(String::length)))
                .findFirst();
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

        Optional<String> maximumBy = getLongest(words);
        System.out.println(maximumBy);

    }
}
