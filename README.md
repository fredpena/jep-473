# JEP 473: Stream Gatherers (Second Preview)

Es el segundo preview relacionado con `Stream Gatherers`, que busca mejorar la `API Stream` para que admita operaciones
intermedias personalizadas. Esto permitirá que las canalizaciones de Stream transformen los datos de maneras que no son
fáciles de lograr con las operaciones intermedias integradas existentes.

`Stream Gatherers` se propusu como una función de preview en el `JEP 461` en el `JDK 22` y se volvio a
presentar en el `JEP 473` en el `JDK 23`.

- [JEP 461: Stream Gatherers (Preview)](https://openjdk.org/jeps/461) - `JDK 22`
- [JEP 473: Stream Gatherers (Second Preview)](https://openjdk.org/jeps/473) - `JDK 23`
- [JEP 485: Stream Gatherers](https://openjdk.org/jeps/485) - `JDK 24` Aquí se proponemos finalizar la API

### Motivación

La motivación radica en superar las limitaciones de las operaciones intermedias predefinidas en Java 8, las cuales
pueden no ser suficientes para tareas más complejas.

### Goals vs Non-Goals

##### Objetivos

- Hacer las canalizaciones de transmisión más flexibles y expresivas.
- Permitir operaciones intermedias personalizadas para flujos infinitos.

##### No objetivos

- No se busca cambiar el lenguaje Java ni facilitar la compilación de código que usa la API Stream.

### Stream Collector vs Stream Gatherer

El diseño de la interfaz Gatherer está fuertemente influenciado por el diseño de Collector. Las principales diferencias
son:

- Gatherer utiliza un `Integrator` en lugar de un `BiConsumer` para el procesamiento por elemento porque necesita un
  parámetro de entrada adicional para el objeto `Downstream` y porque necesita devolver un booleano para indicar si el
  procesamiento debe continuar.
- Gatherer utiliza un `BiConsumer` para su finalizador en lugar de una `Function` porque necesita un parámetro de
  entrada adicional para su objeto `Downstream`.

### Etapas de la API de Stream

Los streams de Java constan de tres etapas:

- Fuente de Stream: `IntStream.of(...)`, `Collection.stream()`.
- Operaciones intermedias: `map(...)`, `filter(...)`, `limit(...)`.
- Operaciones terminadas: `toList()`, `collect(Collectors.toMap(...))`, `count()`.

##### ExampleStream

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/ExampleStream.java 
```

##### ExampleCollect

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/ExampleCollect.java 
```

##### ExampleGather

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/ExampleGather.java 
```

##### StreamGatherers

Los Gatherers están compuestos por hasta cuatro componentes:

- **Initializer (Inicializador):** Inicializa el estado privado para procesar elementos.
- **Integrator (Integrador):** Procesa elementos de entrada, actualizando estado y emitiendo salida.
- **Combiner (Combinador):** Permite evaluación paralela al combinar resultados parciales.
- **Finisher (Finalizador):** Finaliza el procesamiento, inspeccionando el estado y emitiendo salida final.

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/StreamGatherers.java 
```

### Built-in gatherers

Se introducen gatherers integrados como `fold`, `mapConcurrent`, `scan`, `windowFixed` y `windowSliding`, proporcionando
operaciones comunes que pueden utilizarse directamente o como bloques de construcción para transformaciones más
complejas.

- `fold` es un recolector con estado de muchos a uno, que construye un agregado de forma incremental y emite ese
  agregado cuando no hay más elementos de entrada.

- `mapConcurrent` es un recolector con estado de uno a uno que invoca una función proporcionada para cada elemento de
  entrada de manera concurrente, hasta un límite especificado.

- `scan` es un recolector con estado de uno a uno que aplica una función proporcionada al estado actual y al elemento
  actual para producir el siguiente elemento, que pasa al siguiente estado.

- `windowFixed` es un recolector con estado de muchos a muchos que agrupa elementos de entrada en listas de un tamaño
  especificado.

- `windowSliding` es un recolector con estado de muchos a muchos que agrupa elementos de entrada en listas de un tamaño
  especificado. Después de la primera ventana, cada ventana subsiguiente se crea a partir de una copia de su predecesora
  al eliminar el primer elemento y agregar el siguiente elemento del flujo de entrada.

##### Gatherers.fold(...)

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/gatherers/Fold.java 
```

##### Gatherers.mapConcurrent(...)

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/gatherers/MapConcurrent.java 
```

##### Gatherers.scan(...)

```java
public class Scan {

    public static void main(String[] args) {

        List<String> numberStrings = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.scan(() -> "", (str, num) -> str + num)
                )
                .toList();

        numberStrings.forEach(System.out::println);
    }
}
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/gatherers/Scan.java 
```

##### Gatherers.windowFixed(...)

```java
public class WindowFixed {

    public static void main(String[] args) {

        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .gather(
                        Gatherers.windowFixed(3)
                ).toList();

        windows.forEach(System.out::println);
    }

}
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/gatherers/WindowFixed.java 
```

##### Gatherers.windowSliding(...)

```java
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
```

```shell
java --source 23 --enable-preview $PWD/src/main/java/dev/fredpena/gatherers/WindowSliding.java 
```


