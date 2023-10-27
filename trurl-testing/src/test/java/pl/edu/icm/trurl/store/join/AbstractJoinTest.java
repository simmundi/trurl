package pl.edu.icm.trurl.store.join;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.Proficiency;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;
import pl.edu.icm.trurl.store.join.Join;

import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

abstract class AbstractJoinTest<J extends Join> {

    abstract Store createJoin();
    abstract J getJoin();

    public static final int RECORD_COUNT = 500_000;
    Store store;
    J hobbyJoin;
    StringAttribute nameAttribute;
    IntAttribute sizeAttribute;
    FloatAttribute hoursAttribute;

    @BeforeEach
    void before() {
        store = new Store(new ArrayAttributeFactory(), 10_000_000);
        store.addString("name");
        store.addInt("size");
        Store hobbiesStore = createJoin();
        hobbiesStore.addFloat("hours");
        hobbiesStore.addEnum("proficiency", Proficiency.class);
        hobbyJoin = getJoin();

        nameAttribute = store.get("name");
        sizeAttribute = store.get("size");
        hoursAttribute = hobbyJoin.getTarget().get("hours");

        // create 500 000 records
        IntStream.range(0, RECORD_COUNT).parallel().forEach(i -> {
            int row = store.getCounter().next();
            nameAttribute.setString(row, "name" + i);
        });
    }

    long sanityCheckAndCountHobbies() {
        long totalHobbiesExpected = IntStream.range(0, RECORD_COUNT).parallel().map(row -> {
            int rememberedSize = sizeAttribute.getInt(row);
            assertThat(rememberedSize).isEqualTo(hobbyJoin.getExactSize(row));
            return rememberedSize;
        }).sum();
        Store target = hobbyJoin.getTarget();
        long totalHobbies = IntStream.range(0, target.getCounter().getCount()).parallel()
                .filter(row -> !target.isEmpty(row)).count();
        assertThat(totalHobbies).isEqualTo(totalHobbiesExpected);
        return totalHobbies;
    }

    // initializes RECORD_COUNT people with predictable hobbies
    void initializePeople() {
        IntStream.range(0, RECORD_COUNT).parallel().forEach(row -> {
            int size = row % 5;
            hobbyJoin.setSize(row, size);
            sizeAttribute.setInt(row, size);
            for (int i = 0; i < size; i++) {
                int hoursRow = hobbyJoin.getRow(row, i);
                hoursAttribute.setFloat(hoursRow, row + i);
            }
        });
    }
}
