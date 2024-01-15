package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CounterTest {
    @Test
    void next() {
        // given
        Counter counter = new Counter(100);

        // execute
        int a = counter.next();
        int b = counter.next();

        // assert
        assertThat(a).isEqualTo(0);
        assertThat(b).isEqualTo(1);
    }

    @Test
    void getCount() {
        // given
        Counter counter = new Counter(100);

        // execute
        IntStream.range(0, 1_000_000).parallel().forEach(i -> counter.next());
        int count = counter.getCount();

        // assert
        assertThat(count).isEqualTo(1_000_000);
    }
    final int FREE_LIST_SIZE = 100_000;

    @Test
    void free() {
        // given
        Counter counter = new Counter(FREE_LIST_SIZE);



        // execute
        IntStream.range(0, 1_000_000).parallel().forEach(i -> counter.next());
        IntStream.range(0, FREE_LIST_SIZE).parallel().forEach(counter::free);
        Set<Integer> collect = IntStream.range(0, FREE_LIST_SIZE).parallel().map(i -> counter.next()).boxed().collect(Collectors.toSet());

        // assert
        Set<Integer> expected = IntStream.range(0, FREE_LIST_SIZE).boxed().collect(Collectors.toSet());
        assertThat(collect).isEqualTo(expected);
    }
    @Test
    void free__interleaved() {
        IntStream.range(0, FREE_LIST_SIZE * 10);

        Counter counter = new Counter(FREE_LIST_SIZE);
        IntStream.range(0, FREE_LIST_SIZE).parallel().forEach(idx -> {
            if (idx % 2 == 0) {
                counter.next();
            } else {
                counter.free(idx);
            }
        });

    }

    @Test
    void nextSlab() {
        // given
        Counter counter = new Counter(100);

        // execute
        counter.next(10);
        int next = counter.next();

        // assert
        assertThat(next).isEqualTo(10);
    }

    @Test
    void freeSlab() {
        // given
        Counter counter = new Counter(100);
        counter.next(123);
        int slabStart = counter.next(3);

        // execute
        counter.free(slabStart, 3);

        // assert
        assertThat(counter.next()).isEqualTo(slabStart + 2);
        assertThat(counter.next()).isEqualTo(slabStart + 1);
        assertThat(counter.next()).isEqualTo(slabStart);
    }
}