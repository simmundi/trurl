package pl.edu.icm.trurl.store.reference;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

class SingleReferenceTest {
    public static final int CAPACITY = 500_000;
    Store store = new Store(new BasicAttributeFactory(), CAPACITY);

    @Test
    public void init() {
        // execute
        store.addReference("next").single();
        store.addString("name");
        Reference next = store.getReference("next");

        // assert
        assertThat(next).isInstanceOf(SingleReference.class);
        assertThat(next.attributes().stream().map(Attribute::name)).containsExactly("next");
        assertThat(store.getDataAttributes().stream().map(Attribute::name)).containsExactly("name");
        assertThat(store.getAllAttributes().stream().map(Attribute::name)).containsExactly("next", "name");
    }

    @Test
    public void getActualSize__empty() {
        store.addReference("next").single();
        store.addString("name");
        Reference next = store.getReference("next");
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            assertThat(next.getExactSize(idx)).isEqualTo(0);
            assertThat(next.getId(idx, 0)).isEqualTo(Integer.MIN_VALUE);
        });
    }


    @Test
    public void setId() {
        // given
        store.addReference("next").single();
        store.addString("name");
        Reference next = store.getReference("next");

        // execute
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            int nextId = (idx + 1) % CAPACITY;
            next.setId(idx, 0, nextId);
            if (nextId % 1111 == 0) next.setSize(idx, 0);
        });
        long emptySizeA = IntStream.range(0, CAPACITY).parallel().filter(idx -> next.getExactSize(idx) == 0).count();
        long emptySizeB = IntStream.range(0, CAPACITY).parallel().filter(idx -> next.getId(idx, 0) == Integer.MIN_VALUE).count();
        long hasSizeOne = IntStream.range(0, CAPACITY).parallel().filter(idx -> next.getExactSize(idx) == 1).count();

        // assert
        assertThat(emptySizeA).isEqualTo(CAPACITY / 1111 + 1);
        assertThat(emptySizeB).isEqualTo(emptySizeA);
        assertThat(hasSizeOne + emptySizeA).isEqualTo(CAPACITY);
        IntStream.range(0, CAPACITY).parallel().filter(idx -> next.getExactSize(idx) == 1).forEach(idx -> {
            assertThat(next.getId(idx, 0)).isEqualTo((idx + 1) % CAPACITY);
        });
    }
}