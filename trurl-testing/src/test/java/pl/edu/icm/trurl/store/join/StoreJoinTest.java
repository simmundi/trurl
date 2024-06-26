package pl.edu.icm.trurl.store.join;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.Proficiency;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.IntListAttribute;

import static org.assertj.core.api.Assertions.*;

public class StoreJoinTest {

    Store store = new Store(new BasicAttributeFactory(), 1000);

    @Test
    void addJoin__ArrayTyped() {
        // given
        store.addString("name");
        store.addInt("age");

        // execute
        Store hobbiesStore = this.store.addJoin("hobby").arrayTyped(3, 5);
        hobbiesStore.addString("name");
        hobbiesStore.addEnum("proficiency", Proficiency.class);
        ArrayJoin hobbyJoin = store.getJoin("hobby");

        // assert
        assertThat(hobbyJoin.getTarget()).isSameAs(hobbiesStore);
        assertThat(store.getDataAttributes().stream().map(Attribute::name)).containsExactly("name", "age");
        assertThat((Attribute) store.get("hobby")).isInstanceOf(IntListAttribute.class);
    }

    @Test
    void addJoin__RangeTyped() {
        // given
        store.addString("name");
        store.addInt("age");

        // execute
        Store hobbiesStore = this.store.addJoin("hobby").rangeTyped(3, 5);
        hobbiesStore.addString("name");
        hobbiesStore.addEnum("proficiency", Proficiency.class);
        RangedJoin hobbyJoin = store.getJoin("hobby");

        // assert
        assertThat(hobbyJoin.getTarget()).isSameAs(hobbiesStore);
        assertThat(store.getDataAttributes().stream().map(Attribute::name)).containsExactly("name", "age");
        assertThat((Attribute) store.get("hobby_start")).isInstanceOf(IntAttribute.class);
        assertThat((Attribute) store.get("hobby_length")).isInstanceOf(ByteAttribute.class);
    }
}
