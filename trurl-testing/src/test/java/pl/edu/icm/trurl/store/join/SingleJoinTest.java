package pl.edu.icm.trurl.store.join;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.util.function.IntBinaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class SingleJoinTest {

    public static final int CAPACITY = 1000_000;
    Store store = new Store(new ArrayAttributeFactory(), CAPACITY);

    @Test
    void init() {
        // execute
        store.addString("name");
        Store images = this.store.addJoin("image").singleTyped();
        images.addInt("width");
        images.addInt("height");
        images.addString("blob");

        // assert
        assertThat(store.visibleAttributes().map(Attribute::name))
                .containsExactly("name");
        assertThat(store.attributes().map(Attribute::name))
                .containsExactly("name", "image");
        assertThat(images.visibleAttributes().map(Attribute::name))
                .containsExactly("width", "height", "blob");
        assertThat(store.getSubstores().map(Store::getName)).containsExactly("image");
        assertThat(store.getSubstore("image")).isSameAs(store.getJoin("image").getTarget());
    }

    @Test
    void getRow__empty() {
        // execute
        store.addString("name");
        Store imagesStore = this.store.addJoin("image").singleTyped();
        imagesStore.addString("blob");
        SingleJoin images = this.store.getJoin("image");

        // assert
        IntStream.range(0, CAPACITY).forEach(idx -> {
            assertThat(images.getRow(idx, 0)).isEqualTo(Integer.MIN_VALUE);
            assertThat(images.getExactSize(idx)).isEqualTo(0);
        });
    }

    @Test
    void setSize() {
        // given
        store.addString("name");
        Store imagesStore = this.store.addJoin("image").singleTyped();
        imagesStore.addInt("blobIndex");

        IntAttribute blobAttribute = imagesStore.get("blobIndex");
        SingleJoin images = this.store.getJoin("image");
        Store target = images.getTarget();

        // execute
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            images.setSize(idx, 1);
            int targetRow = images.getRow(idx, 0);
            blobAttribute.setInt(targetRow, 1 + idx);
        });

        // assert
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            assertThat(images.getExactSize(idx)).isEqualTo(1);
            assertThat(blobAttribute.getInt(images.getRow(idx, 0))).isEqualTo(1 + idx);
        });
        assertThat(target.getCounter().getCount()).isEqualTo(CAPACITY);
    }

    @Test
    void setSize__free() {
        // given
        store.addString("name");
        Store imagesStore = this.store.addJoin("image").singleTyped();
        imagesStore.addInt("blobIndex");

        IntAttribute blobAttribute = imagesStore.get("blobIndex");
        SingleJoin images = this.store.getJoin("image");

        // execute
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            if (idx % 10 == 0) {
                images.setSize(idx, 1);
                int targetRow = images.getRow(idx, 0);
                blobAttribute.setInt(targetRow, 1 + idx);
            } else {
                images.setSize(idx, 0);
            }
        });
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            if (idx % 7 == 0) {
                images.setSize(idx, 1);
                int targetRow = images.getRow(idx, 0);
                blobAttribute.setInt(targetRow, 2 + idx);
            } else {
                images.setSize(idx, 0);
            }
        });
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            if (idx % 2 == 0) {
                images.setSize(idx, 1);
                int targetRow = images.getRow(idx, 0);
                blobAttribute.setInt(targetRow, 3 + idx);
            } else {
                images.setSize(idx, 0);
            }
        });
        IntStream.range(0, CAPACITY).parallel().forEach(idx -> {
            if (idx % 3 == 0) {
                images.setSize(idx, 1);
                int targetRow = images.getRow(idx, 0);
                blobAttribute.setInt(targetRow, idx + 4);
            } else {
                images.setSize(idx, 0);
            }
        });

        // assert
        IntStream.range(0, CAPACITY).forEach(idx -> {
            if (idx % 3 == 0) {
                int targetRow = images.getRow(idx, 0);
                assertThat(images.getExactSize(idx)).isEqualTo(1);
                assertThat(blobAttribute.getInt(targetRow)).isEqualTo(idx + 4);
            } else {
                assertThat(images.getExactSize(idx)).isEqualTo(0);
                assertThat(images.getRow(idx, 0)).isEqualTo(Integer.MIN_VALUE);
            }
        });
        int count = images.getTarget().getCounter().getCount();
        int free = 0;
        while (images.getTarget().getCounter().next() != count) {
            free++;
        }

        // this is the top usage of the target store (idx % 2 was filled)
        assertThat(count).isEqualTo(CAPACITY / 2);
        // count - free is the current usage (idx % 3 was filled)
        assertThat(count - free).isEqualTo(CAPACITY / 3 + 1);
    }
}