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

package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.exampledata.*;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelectorsTest {

    @Mock
    Engine engine;

    @Mock
    Selector selector;

    @Mock
    Chunk chunk;

    @Mock
    LooksMapper looksMapper;

    Looks looks1 = new Looks(Color.GOLD, Texture.ROUGH);
    Looks looks2 = new Looks(Color.BLUE, Texture.SHINY);

    Person person1 = new Person();
    Person person3 = new Person();
    Person person2 = new Person();
    @Mock
    PersonMapper personMapper;
    @Mock
    MapperSet mapperSet;

    @BeforeEach
    void setUp() {
        when(chunk.ids()).thenReturn(IntStream.of(0, 1, 2, 3));
        when(chunk.getChunkInfo()).thenReturn(ChunkInfo.of(0,4));

        when(looksMapper.isPresent(0)).thenReturn(true);
        when(looksMapper.createAndLoad(0)).thenReturn(looks2); // nie
        when(looksMapper.isPresent(1)).thenReturn(true);
        when(looksMapper.createAndLoad(1)).thenReturn(looks1); //tak
        when(looksMapper.isPresent(2)).thenReturn(false);
        when(looksMapper.isPresent(3)).thenReturn(false);

        person2.setName("Pomponik");
        when(personMapper.isPresent(1)).thenReturn(true);
        when(personMapper.createAndLoad(1)).thenReturn(person2); //tak
        person3.setName("Niewiadomska");
        when(personMapper.isPresent(2)).thenReturn(true);
        when(personMapper.createAndLoad(2)).thenReturn(person3); //tak
        when(personMapper.isPresent(3)).thenReturn(false);

        when(selector.chunks()).thenReturn(Stream.of(chunk));
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Looks.class)).thenReturn(looksMapper);
        when(mapperSet.classToMapper(Person.class)).thenReturn(personMapper);
    }

    @Test
    void filtered() {
        //given
        Selectors selectors = new Selectors(engine);

        //execute
        Selector filtered = selectors.filtered(selector,
                Looks.class, looks -> looks.getColor() == Color.GOLD, true,
                Person.class, person -> person.getName().equals("Pomponik") || person.getName().equals("Niewiadomska"), false);

        //assert
        assertThat(filtered.chunks().flatMapToInt(Chunk::ids).toArray())
                .containsExactlyInAnyOrder(1,2);

    }
}