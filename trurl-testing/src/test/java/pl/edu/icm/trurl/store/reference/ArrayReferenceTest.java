package pl.edu.icm.trurl.store.reference;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

public class ArrayReferenceTest {

    public static final int CAPACITY = 500_000;
    Store store = new Store(new ArrayAttributeFactory(), CAPACITY);

    @Test
    public void init() {
        // execute
        store.addReference("next").single();
        store.addString("name");
        Reference next = store.getReference("next");

        // assert
        assertThat(next).isInstanceOf(SingleReference.class);
        assertThat(next.attributes().stream().map(Attribute::name)).containsExactly("next");
        assertThat(store.visibleAttributes().map(Attribute::name)).containsExactly("name");
        assertThat(store.attributes().map(Attribute::name)).containsExactly("next", "name");
    }


    @Test
    public void getActualSize__empty() {
        // given
        store.addReference("next").arrayTyped(1, 3);
        store.addString("name");
        Reference next = store.getReference("next");

        // execute
        long emptyA = IntStream.range(0, CAPACITY).parallel().filter(idx -> next.getExactSize(idx) == 0).count();
        long emptyB = IntStream.range(0, CAPACITY).parallel().filter(idx -> next.getId(idx, 0) == Integer.MIN_VALUE).count();

        // assert
        assertThat(emptyA).isEqualTo(CAPACITY);
        assertThat(emptyB).isEqualTo(CAPACITY);
    }

    @Test
    public void setId() {
        // given
        store.addReference("next").arrayTyped(3, 3);
        store.addString("name");
        Reference next = store.getReference("next");

        // execute
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            int size = idx % 5;
            next.setSize(idx, size);
            for (int i = 0; i < size; i++) {
                next.setId(idx, i, 10 * idx + i);
            }
        });

        // assert
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            int size = idx % 5;
            assertThat(next.getExactSize(idx)).isEqualTo(size);
            assertThat(next.getId(idx, size)).isEqualTo(Integer.MIN_VALUE);
            for (int i = 0; i < size; i++) {
                assertThat(next.getId(idx, i)).isEqualTo(10 * idx + i);
            }
        });
    }

    @Test
    public void setId__rewrite() {
        // given
        store.addReference("next").arrayTyped(3, 3);
        store.addString("name");
        Reference next = store.getReference("next");

        // execute
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            int size = idx % 5;
            next.setSize(idx, size);
            for (int i = 0; i < size; i++) {
                next.setId(idx, i, 10 * idx + i);
            }
        });
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            int size = idx % 10;
            next.setSize(idx, size);
            for (int i = 0; i < size; i++) {
                next.setId(idx, i, 100 * idx - i);
            }
        });

        // assert
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            int size = idx % 10;
            assertThat(next.getExactSize(idx)).isEqualTo(size);
            assertThat(next.getId(idx, size)).isEqualTo(Integer.MIN_VALUE);
            for (int i = 0; i < size; i++) {
                assertThat(next.getId(idx, i)).isEqualTo(100 * idx - i);
            }
        });
    }
}
