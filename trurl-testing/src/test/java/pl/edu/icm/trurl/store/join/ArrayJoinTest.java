package pl.edu.icm.trurl.store.join;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;

import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

public class ArrayJoinTest extends AbstractJoinTest<ArrayJoin>{

    @Override
    Store createJoin() {
        return this.store.addJoin("hobby").arrayTyped(3, 1);
    }

    @Override
    ArrayJoin getJoin() {
        return (ArrayJoin) store.getJoin("hobby");
    }

    @Test
    void setSize() {
        // execute
        initializePeople();

        // assert
        int count = hobbyJoin.getTarget().getCounter().getCount();

        assertThat(count).isEqualTo(sanityCheckAndCountHobbies());
    }

    @Test
    void setSize__resize() {
        // given
        initializePeople();

        // resize
        Random random = new Random(1);
        int[] randoms = IntStream.range(0, RECORD_COUNT).map(unused -> random.nextInt(4)).toArray();
        IntStream.range(0, RECORD_COUNT).forEach(row -> {
            int newSize = randoms[row];
            sizeAttribute.setInt(row, newSize);
            hobbyJoin.setSize(row, newSize);
            for (int i = 0; i < newSize; i++) {
                int hoursRow = hobbyJoin.getRow(row, i);
                hoursAttribute.setFloat(hoursRow, row + i);
            }
        });

        // assert
        long count = hobbyJoin.getTarget().getCounter().getCount();
        long totalSize = sanityCheckAndCountHobbies();
        assertThat(count).isGreaterThanOrEqualTo(totalSize);
    }


}
