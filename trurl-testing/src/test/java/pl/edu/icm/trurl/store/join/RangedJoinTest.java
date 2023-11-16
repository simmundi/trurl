package pl.edu.icm.trurl.store.join;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;

import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

public class RangedJoinTest extends AbstractJoinTest<RangedJoin>{

    @Override
    Store createJoin() {
        return this.store.addJoin("hobby").rangeTyped(2, 1);
    }

    @Override
    RangedJoin getJoin() {
        return (RangedJoin) store.getJoin("hobby");
    }

    @Test
    void setSize() {
        // execute
        initializePeople();
        ByteAttribute hobbyLength = store.get("hobby_length");

        // assert
        long allocatedRowsCount = IntStream.range(0, RECORD_COUNT)
                .filter(row -> !hobbyLength.isEmpty(row)).map(hobbyLength::getByte).sum();
        long  hobbyCount = hobbyJoin.getTarget().getCounter().getCount();

        assertThat(allocatedRowsCount).isEqualTo(hobbyCount);
    }

    @Test
    void setSize__resize() {
        // given
        initializePeople();
        ByteAttribute hobbyLength = store.get("hobby_length");

        // resize
        Random random = new Random(1);
        int[] randoms = IntStream.range(0, RECORD_COUNT).map(unused -> random.nextInt(3)).toArray();
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
        long allocatedRowsCount = IntStream.range(0, RECORD_COUNT)
                .filter(row -> !hobbyLength.isEmpty(row)).map(hobbyLength::getByte).sum();
        int freeCount = 0;
        while (hobbyJoin.getTarget().getCounter().next() < count) {
            freeCount++;
        }
        sanityCheckAndCountHobbies();
        assertThat(allocatedRowsCount).isEqualTo(count - freeCount);
    }

}
