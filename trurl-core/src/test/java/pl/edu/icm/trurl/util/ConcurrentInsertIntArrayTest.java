package pl.edu.icm.trurl.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ConcurrentInsertIntArrayTest {

    @Test
    void add() {
        //given
        ConcurrentInsertIntArray concurrentInsertIntArray = new ConcurrentInsertIntArray(20_000);

        //execute
        IntStream.range(0, 20_000).parallel().forEach(concurrentInsertIntArray::add);
        Set<Integer> output = concurrentInsertIntArray.stream().boxed().collect(Collectors.toSet());

        //assert
        assertThat(IntStream.range(0, 20_000).allMatch(output::contains)).isEqualTo(true);
    }

    @Test
    @DisplayName("appending over initial size should cause RuntimeException")
    void appendThrowsError() {
        //given
        ConcurrentInsertIntArray concurrentInsertIntArray = new ConcurrentInsertIntArray(20);

        //execute & assert
        assertThrows(IndexOutOfBoundsException.class, () -> IntStream.range(0, 100).parallel().forEach(concurrentInsertIntArray::add));
    }

    @Test
    void contains() {
        //given
        ConcurrentInsertIntArray concurrentInsertIntArray = new ConcurrentInsertIntArray(10);
        concurrentInsertIntArray.add(21);
        //execute
        boolean doesItContain21 = concurrentInsertIntArray.contains(21);
        boolean doesItContain37 = concurrentInsertIntArray.contains(37);
        //assert
        assertThat(doesItContain21).isEqualTo(true);
        assertThat(doesItContain37).isEqualTo(false);
    }

    @Test
    void stream() {
        //given
        ConcurrentInsertIntArray concurrentInsertIntArray = new ConcurrentInsertIntArray(2000);
        IntStream.range(0, 1000).forEach(concurrentInsertIntArray::add);
        //execute & assert
        assertThat(concurrentInsertIntArray.stream().count()).isEqualTo(concurrentInsertIntArray.getCurrentSize());
    }

    @Test
    void getInt() {
        //given
        ConcurrentInsertIntArray concurrentInsertIntArray = new ConcurrentInsertIntArray(20);
        //execute
        concurrentInsertIntArray.add(10);
        //assert
        assertThat(concurrentInsertIntArray.getInt(0)).isEqualTo(10);
        assertThrows(IndexOutOfBoundsException.class, () -> concurrentInsertIntArray.getInt(1));
    }
}