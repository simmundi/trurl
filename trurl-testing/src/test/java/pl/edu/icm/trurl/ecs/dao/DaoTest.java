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
package pl.edu.icm.trurl.ecs.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.Color;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Texture;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaoTest {

    @Mock
    Store store;

    @Mock
    EnumAttribute colorAttribute;

    @Mock
    EnumAttribute textureAttribute;

    Dao<Looks> dao;

    @BeforeEach
    void before() {
        dao = new Daos().createDao(Looks.class);
        when(store.get("color")).thenReturn(colorAttribute);
        when(store.get("texture")).thenReturn(textureAttribute);
        dao.configureStore(store);
        dao.attachStore(store);
    }

    @Test
    void construct() {
        // assert
        verify(store).addEnum("color", Color.class);
        verify(store).addEnum("texture", Texture.class);

        verify(store).get("color");
        verify(store).get("texture");
    }

    @Test
    void load() {
        // given
        Looks looks = new Looks();
        when(colorAttribute.getEnum(4)).thenReturn(Color.BLUE);
        when(textureAttribute.getEnum(4)).thenReturn(Texture.ROUGH);

        // execute
        dao.load(null, looks, 4);

        // assert
        assertThat(looks.getColor()).isEqualTo(Color.BLUE);
        assertThat(looks.getTexture()).isEqualTo(Texture.ROUGH);
    }

    @Test
    void save() {
        // given
        Looks looks = new Looks(Color.BLUE, Texture.SHINY);

        // execute
        dao.save(null, looks, 99);

        // assert
        verify(colorAttribute).setEnum(99, Color.BLUE);
        verify(textureAttribute).setEnum(99, Texture.SHINY);
    }

    @Test
    void isPresent() {
        // given
        IntStream.of(10, 36, 50, 99).forEach(i ->
                lenient().when(colorAttribute.isEmpty(i)).thenReturn(true));
        IntStream.of(10, 37, 50, 98).forEach(i ->
                lenient().when(textureAttribute.isEmpty(i)).thenReturn(true));

        // execute
        Set<Integer> emptyRows = IntStream.range(0, 100)
                .filter(i -> !dao.isPresent(i))
                .boxed()
                .collect(Collectors.toSet());

        // assert
        assertThat(emptyRows).containsExactlyInAnyOrder(10, 50);
    }

    @Test
    void create() {
        // execute
        Looks looks = dao.create();

        // assert
        assertThat(looks).isNotNull();
    }
}
