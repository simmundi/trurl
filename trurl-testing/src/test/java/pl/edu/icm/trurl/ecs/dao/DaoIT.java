/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.ecs.dao;

import net.snowyhollows.bento.BentoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.exampledata.*;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.*;
import pl.edu.icm.trurl.store.reference.Reference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class DaoIT {
    @Spy
    Daos daos = new Daos();
    Map<Class<?>, BentoFactory<?>> factories = new HashMap<>();
    @Spy
    DaoManager daoManager = new DaoManager(new DynamicComponentAccessor(Collections.emptyList()), factories, daos);
    @Mock
    Session session;

    Dao<BunchOfData> dao;

    Store store = new Store(new ArrayAttributeFactory(), 1000);
    private BooleanAttribute booleanAttribute;
    private ByteAttribute byteAttribute;
    private DoubleAttribute doubleAttribute;
    private EnumAttribute<Color> enumAttribute;
    private FloatAttribute floatAttribute;
    private IntAttribute intAttribute;
    private ShortAttribute shortAttribute;
    private StringAttribute stringAttribute;
    private EnumAttribute<Color> looksPropColor;
    private EnumAttribute<Texture> looksPropTexture;
    private Reference entityReference;
    private Reference entitiesReference;

    @BeforeEach
    void before() {
        assert daoManager != null;
        dao = new Daos().createDao(BunchOfData.class);
        dao.configureStore(store);
        dao.attachStore(store);
        booleanAttribute = store.get("booleanProp");
        byteAttribute = store.get("byteProp");
        doubleAttribute = store.get("doubleProp");
        enumAttribute = store.get("enumProp");
        floatAttribute = store.get("floatProp");
        intAttribute = store.get("intProp");
        shortAttribute = store.get("shortProp");
        stringAttribute = store.get("stringProp");
        looksPropColor = store.get("looksProp.color");
        looksPropTexture = store.get("looksProp.texture");
        entityReference = store.getReference("entityProp");
        entitiesReference = store.getReference("entitiesProp");

        lenient().when(session.getEntity(anyInt())).thenAnswer(call -> createEntity(call.getArgument(0, Integer.class)));
    }

    @Test
    public void save() {
        // given
        BunchOfData dto = createBunchOfData();

        // execute
        dao.save(null, dto, 5);

        // assert
        assertThat(booleanAttribute.getBoolean(5)).isTrue();
        assertThat(byteAttribute.getByte(5)).isEqualTo((byte) 121);
        assertThat(doubleAttribute.getDouble(5)).isEqualTo(56.34);
        assertThat(enumAttribute.getEnum(5)).isEqualTo(Color.GOLD);
        assertThat(floatAttribute.getFloat(5)).isEqualTo(1.1f);
        assertThat(intAttribute.getInt(5)).isEqualTo(-10);
        assertThat(stringAttribute.getString(5)).isEqualTo("blebleble");
        assertThat(shortAttribute.getShort(5)).isEqualTo((short) 78);

        assertThat(entitiesReference.getExactSize(5)).isEqualTo(3);
        assertThat(entitiesReference.getId(5, 0)).isEqualTo(4);
        assertThat(entitiesReference.getId(5, 1)).isEqualTo(5);
        assertThat(entitiesReference.getId(5, 2)).isEqualTo(6);
        assertThat(entitiesReference.getId(5, 3)).isEqualTo(Integer.MIN_VALUE);
        assertThat(entityReference.getId(5, 0)).isEqualTo(34);
        assertThat(entityReference.getId(5, 1)).isEqualTo(Integer.MIN_VALUE);
        assertThat(entityReference.getExactSize(5)).isEqualTo(1);
    }

    @Test
    public void save__embedded() {
        // given
        BunchOfData dto = createBunchOfData();
        dto.setLooksProp(new Looks(Color.SILVER, Texture.SHINY));

        // execute
        dao.save(null, dto, 10);

        // assert
        assertThat(looksPropColor.getEnum(10)).isEqualTo(Color.SILVER);
        assertThat(looksPropTexture.getEnum(10)).isEqualTo(Texture.SHINY);
    }

    @Test
    public void save__no_entities() {
        // given
        BunchOfData dto = createBunchOfData();
        dto.setEntityProp(null);
        dto.getEntitiesProp().clear();
        List<Integer> entityList = new ArrayList<>();

        // execute
        dao.save(dto, 7);

        // assert
        assertThat(entityList).isEmpty();
    }

    @Test
    public void load() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 12;
        dao.save(original, idx);

        // execute
        BunchOfData copy = dao.create();
        dao.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void load__embedded() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 12;
        original.setLooksProp(new Looks(Color.BLUE, null));
        dao.save(original, idx);

        // execute
        BunchOfData copy = dao.create();
        dao.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 12;
        original.getStatsProp().add(new Stats(15, 67, 2));
        original.getStatsProp().add(new Stats(16, 67, 1));
        original.getStatsProp().add(new Stats(17, 67, 0));
        dao.save(original, idx);

        // execute
        BunchOfData copy = dao.create();
        dao.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list_overwrite_shorter() {
        // given
        BunchOfData original = createBunchOfData();
        int row = 10;
        original.getStatsProp().add(new Stats(15, 67, 2));
        original.getStatsProp().add(new Stats(16, 67, 1));
        original.getStatsProp().add(new Stats(17, 67, 0));
        dao.save(original, row);
        original.getStatsProp().remove(2);
        original.getStatsProp().remove(1);
        dao.save(original, row);

        // execute
        BunchOfData copy = dao.create();
        dao.load(session, copy, row);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list_overwrite_longer() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 10;
        original.getStatsProp().add(new Stats(15, 67, 2));
        dao.save(original, idx);
        original.getStatsProp().add(new Stats(16, 67, 1));
        original.getStatsProp().add(new Stats(17, 67, -2));
        dao.save(original, idx);

        // execute
        BunchOfData copy = dao.create();
        dao.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    void save__multiple_items_with_embedded_lists() {
        // given
        createBunchOfDataWithMultipleStats().forEach(bod -> {
            dao.save(bod, store.getCounter().next());
        });

        // execute
        List<BunchOfData> readBack = IntStream.range(0, store.getCounter().getCount())
                .mapToObj(i -> {
                    BunchOfData bod = dao.create();
                    dao.load(session, bod, i);
                    return bod;
                }).collect(Collectors.toList());

        // assert
        assertThat(readBack).isEqualTo(createBunchOfDataWithMultipleStats());
    }

    private List<BunchOfData> createBunchOfDataWithMultipleStats() {
        return Arrays.asList(
                withStats(
                        new Stats(1, 67, 1)),
                withStats(
                        new Stats(3, 67, 1),
                        new Stats(2, 7, 1),
                        new Stats(4, 7, 1),
                        new Stats(6, 7, 1),
                        new Stats(16, 17, 1),
                        new Stats(16, 117, 1)),
                withStats(
                        new Stats(16, 124, 1)),
                withStats(
                        new Stats(16, 1457464, 0),
                        new Stats(16, 67, -1),
                        new Stats(16, 67, -2),
                        new Stats(16, 67, -3),
                        new Stats(16, 67, -4)),
                withStats()
        );
    }

    private BunchOfData createBunchOfData() {
        BunchOfData bunchOfData = new BunchOfData();
        bunchOfData.setBooleanProp(true);
        bunchOfData.setByteProp((byte) 121);
        bunchOfData.setDoubleProp(56.34);
        bunchOfData.setEnumProp(Color.GOLD);
        bunchOfData.setFloatProp(1.1f);
        bunchOfData.setIntProp(-10);
        bunchOfData.setShortProp((short) 78);
        bunchOfData.setStringProp("blebleble");
        bunchOfData.setEntityProp(createEntity(34));
        bunchOfData.getEntitiesProp().add(createEntity(4));
        bunchOfData.getEntitiesProp().add(createEntity(5));
        bunchOfData.getEntitiesProp().add(createEntity(6));
        return bunchOfData;
    }

    private Entity createEntity(int id) {
        return Entity.stub(id);
    }

    private BunchOfData withStats(Stats... stats) {
        BunchOfData bod = createBunchOfData();
        bod.getStatsProp().addAll(Arrays.asList(stats));
        return bod;
    }

}
