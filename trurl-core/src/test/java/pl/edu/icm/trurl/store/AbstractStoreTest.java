package pl.edu.icm.trurl.store;

import org.assertj.core.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.util.Optional;

public abstract class AbstractStoreTest<T extends Store> {

    protected abstract T createStore();

    @Test
    @DisplayName("Should add a boolean column")
    public void addBoolean() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addBoolean("boolean");
        Attribute attribute = tablesawComponentStore.get("boolean");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(BooleanAttribute.class);
    }

    @Test
    @DisplayName("Should add a byte column")
    public void addByte() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addByte("byte");
        Attribute attribute = tablesawComponentStore.get("byte");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(ByteAttribute.class);
    }

    @Test
    @DisplayName("Should add a double column")
    public void addDouble() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addDouble("double");
        Attribute attribute = tablesawComponentStore.get("double");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(DoubleAttribute.class);
    }

    @Test
    @DisplayName("Should add an entity column")
    public void addEntity() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addEntity("entity");
        Attribute attribute = tablesawComponentStore.get("entity");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(EntityAttribute.class);
    }

    @Test
    @DisplayName("Should add an entity list column")
    public void addEntityList() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addEntityList("entities");
        Attribute attribute = tablesawComponentStore.get("entities");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(EntityListAttribute.class);
    }

    @Test
    @DisplayName("Should add an enum column")
    public void addEnum() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addEnum("enum", Letters.class);
        Attribute attribute = tablesawComponentStore.get("enum");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(EnumAttribute.class);
    }

    @Test
    @DisplayName("Should add a string column")
    public void addString() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addString("string");
        Attribute attribute = tablesawComponentStore.get("string");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(StringAttribute.class);
    }

    @Test
    @DisplayName("Should add a short column")
    public void addShort() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addShort("short");
        Attribute attribute = tablesawComponentStore.get("short");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(ShortAttribute.class);
    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addInt() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addInt("int");
        Attribute attribute = tablesawComponentStore.get("int");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(IntAttribute.class);
    }

    @Test
    @DisplayName("Should add a boolean column")
    public void addFloat() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.addFloat("float");
        Attribute attribute = tablesawComponentStore.get("float");

        // assert
        Assertions.assertThat(attribute).isInstanceOf(FloatAttribute.class);
    }

    @Test
    @DisplayName("Should report count according to last event")
    public void getCountAsNotified() {
        // given
        Store tablesawComponentStore = createStore();

        // execute
        tablesawComponentStore.fireUnderlyingDataChanged(0, 2567);

        // assert
        Assertions.assertThat(tablesawComponentStore.getCount()).isEqualTo(2567);
    }

    private enum Letters { A, B, C };

}
