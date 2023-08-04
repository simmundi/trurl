/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.ecs.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.exampledata.BunchOfData;
import pl.edu.icm.trurl.exampledata.Color;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Stats;
import pl.edu.icm.trurl.exampledata.Texture;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.attribute.*;
import pl.edu.icm.trurl.store.attribute.CategoricalStaticAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MapperIT {

    @Spy
    Mappers mappers = new Mappers();
    @Spy
    MapperSet mapperSet = new MapperSet(new DynamicComponentAccessor(Collections.emptyList()), mappers);

    @Mock
    Session session;

    @Mock
    MapperListener<BunchOfData> mapperListener;

    Mapper<BunchOfData> mapper;

    Store store = new ArrayAttributeFactory(10);
    private BooleanAttribute booleanAttribute;
    private ByteAttribute byteAttribute;
    private DoubleAttribute doubleAttribute;
    private EntityListAttribute entitiesAttribute;
    private EntityAttribute entityAttribute;
    private CategoricalStaticAttribute<Color> categoricalStaticAttribute;
    private FloatAttribute floatAttribute;
    private IntAttribute intAttribute;
    private ShortAttribute shortAttribute;
    private StringAttribute stringAttribute;
    private CategoricalStaticAttribute<Color> looksPropColor;
    private CategoricalStaticAttribute<Texture> looksPropTexture;

    @BeforeEach
    void before() {
        mapper = new Mappers().create(BunchOfData.class);
        mapper.configureStore(store);
        mapper.attachStore(store);
        booleanAttribute = store.get("booleanProp");
        byteAttribute = store.get("byteProp");
        doubleAttribute = store.get("doubleProp");
        entitiesAttribute = store.get("entitiesProp");
        entityAttribute = store.get("entityProp");
        categoricalStaticAttribute = store.get("enumProp");
        floatAttribute = store.get("floatProp");
        intAttribute = store.get("intProp");
        shortAttribute = store.get("shortProp");
        stringAttribute = store.get("stringProp");
        looksPropColor = store.get("looksProp.color");
        looksPropTexture = store.get("looksProp.texture");

        lenient().when(session.getEntity(anyInt())).thenAnswer(call -> createEntity(call.getArgument(0, Integer.class)));
    }

    @Test
    public void save() {
        // given
        BunchOfData dto = createBunchOfData();
        mapper.getMapperListeners().addSavingListener(mapperListener);

        // execute
        mapper.save(null, dto, 5);

        // assert
        List<Entity> entityList = new ArrayList<>();

        assertThat(booleanAttribute.getBoolean(5)).isTrue();
        assertThat(byteAttribute.getByte(5)).isEqualTo((byte) 121);
        assertThat(doubleAttribute.getDouble(5)).isEqualTo(56.34);
        assertThat(categoricalStaticAttribute.getEnum(5)).isEqualTo(Color.GOLD);
        assertThat(floatAttribute.getFloat(5)).isEqualTo(1.1f);
        assertThat(intAttribute.getInt(5)).isEqualTo(-10);
        assertThat(stringAttribute.getString(5)).isEqualTo("blebleble");
        assertThat(shortAttribute.getShort(5)).isEqualTo((short) 78);

        assertThat(entityAttribute.getEntity(5, session)).isEqualTo(createEntity(34));
        assertThat(entitiesAttribute.getSize(5)).isEqualTo(3);
        List<Integer> entityIds = new ArrayList<>();
        entitiesAttribute.loadIds(5, entityIds::add);
        assertThat(entityIds).containsExactly(4, 5, 6);

        verify(mapperListener).savingComponent(5, dto);
    }

    @Test
    public void save__embedded() {
        // given
        BunchOfData dto = createBunchOfData();
        dto.setLooksProp(new Looks(Color.SILVER, Texture.SHINY));

        // execute
        mapper.save(null, dto, 10);

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
        mapper.save(dto, 7);

        // assert
        entitiesAttribute.loadIds(7, entityList::add);
        assertThat(entityAttribute.getEntity(7, session)).isNull();
        assertThat(entityList).isEmpty();
    }

    @Test
    public void save__over_last_row() {
        // given
        BunchOfData dto = createBunchOfData();

        // execute
        mapper.save(dto, 15);

        // assert
        assertThat(mapper.getCount()).isEqualTo(16);
    }

    @Test
    public void isModified() {
        // given
        BunchOfData dto = createBunchOfData();
        dto.setEntityProp(null);
        dto.getEntitiesProp().clear();
        mapper.save(dto, 8);

        // execute
        boolean isModified = mapper.isModified(dto, 8);

        // assert
        assertThat(isModified).isFalse();
    }

    @Test
    public void isModified__embedded() {
        // given
        BunchOfData dto = createBunchOfData();
        dto.setLooksProp(new Looks(Color.SILVER, Texture.SHINY));
        mapper.save(dto, 8);
        dto.getLooksProp().setTexture(Texture.ROUGH);

        // execute
        boolean isModified = mapper.isModified(dto, 8);

        // assert
        assertThat(isModified).isTrue();
    }

    @Test
    public void isModified__entity() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 8;
        mapper.save(original, idx);

        // execute
        BunchOfData copy = createBunchOfData();
        copy.setEntityProp(null);
        boolean isModified = mapper.isModified(copy, idx);

        // assert
        assertThat(isModified).isTrue();
    }

    @Test
    public void isModified__entities() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 9;
        mapper.save(original, idx);

        // execute
        BunchOfData copy = createBunchOfData();
        copy.getEntitiesProp().add(createEntity(987));
        boolean isModified = mapper.isModified(copy, idx);

        // assert
        assertThat(isModified).isTrue();
    }

    @Test
    public void isModified__entity_original_null() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 9;
        original.setEntityProp(null);
        mapper.save(original, idx);

        // execute
        BunchOfData copy = createBunchOfData();
        boolean isModified = mapper.isModified(copy, idx);

        // assert
        assertThat(isModified).isTrue();
    }

    @Test
    public void isModified__entities_original_empty() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 9;
        original.getEntitiesProp().clear();
        mapper.save(original, idx);

        // execute
        BunchOfData copy = createBunchOfData();
        boolean isModified = mapper.isModified(copy, idx);

        // assert
        assertThat(isModified).isTrue();
    }

    @Test
    public void load() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 12;
        mapper.save(original, idx);

        // execute
        BunchOfData copy = mapper.create();
        mapper.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void load__embedded() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 12;
        original.setLooksProp(new Looks(Color.BLUE, null));
        mapper.save(original, idx);

        // execute
        BunchOfData copy = mapper.create();
        mapper.load(session, copy, idx);

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
        mapper.save(original, idx);

        // execute
        BunchOfData copy = mapper.create();
        mapper.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list_overwrite_shorter() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 10;
        original.getStatsProp().add(new Stats(15, 67, 2));
        original.getStatsProp().add(new Stats(16, 67, 1));
        original.getStatsProp().add(new Stats(17, 67, 0));
        mapper.save(original, idx);
        original.getStatsProp().remove(2);
        original.getStatsProp().remove(1);
        mapper.save(original, idx);

        // execute
        BunchOfData copy = mapper.create();
        mapper.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    public void save__list_overwrite_longer() {
        // given
        BunchOfData original = createBunchOfData();
        int idx = 10;
        original.getStatsProp().add(new Stats(15, 67, 2));
        mapper.save(original, idx);
        original.getStatsProp().add(new Stats(16, 67, 1));
        original.getStatsProp().add(new Stats(17, 67, -2));
        mapper.save(original, idx);

        // execute
        BunchOfData copy = mapper.create();
        mapper.load(session, copy, idx);

        // assert
        assertThat(copy).isEqualTo(original);
    }

    @Test
    void save__multiple_items_with_embedded_lists() {
        // given
        AtomicInteger id = new AtomicInteger();
        createBunchOfDataWithMultipleStats().forEach(bod -> {
            mapper.save(bod, id.getAndIncrement());
        });

        // execute
        List<BunchOfData> readBack = IntStream.range(0, mapper.getCount())
                .mapToObj(i -> {
                    BunchOfData bod = mapper.create();
                    mapper.load(session, bod, i);
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
        return new Entity(mapperSet, session, id);
    }

    private BunchOfData withStats(Stats... stats) {
        BunchOfData bod = createBunchOfData();
        bod.getStatsProp().addAll(Arrays.asList(stats));
        return bod;
    }

}
