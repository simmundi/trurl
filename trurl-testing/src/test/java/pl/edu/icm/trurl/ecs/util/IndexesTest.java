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
import pl.edu.icm.trurl.ecs.DaoManager;
import pl.edu.icm.trurl.ecs.index.Chunk;
import pl.edu.icm.trurl.ecs.index.ChunkInfo;
import pl.edu.icm.trurl.ecs.index.Index;
import pl.edu.icm.trurl.exampledata.*;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexesTest {

    public static final int DEFAULT_CHUNK_SIZE = 25_000;
    @Mock
    Engine engine;

    @Mock
    Index index;

    @Mock
    Chunk chunk;

    @Mock
    LooksDao looksDao;

    Looks looks1 = new Looks(Color.GOLD, Texture.ROUGH);
    Looks looks2 = new Looks(Color.BLUE, Texture.SHINY);

    Person person1 = new Person();
    Person person3 = new Person();
    Person person2 = new Person();
    @Mock
    PersonDao personDao;
    @Mock
    DaoManager daoManager;

    @BeforeEach
    void setUp() {
        when(chunk.ids()).thenReturn(IntStream.of(0, 1, 2, 3));
        when(chunk.getChunkInfo()).thenReturn(ChunkInfo.of(0,4));

        when(looksDao.isPresent(0)).thenReturn(true);
        when(looksDao.createAndLoad(0)).thenReturn(looks2); // nie
        when(looksDao.isPresent(1)).thenReturn(true);
        when(looksDao.createAndLoad(1)).thenReturn(looks1); //tak
        when(looksDao.isPresent(2)).thenReturn(false);
        when(looksDao.isPresent(3)).thenReturn(false);

        person2.setName("Pomponik");
        when(personDao.isPresent(1)).thenReturn(true);
        when(personDao.createAndLoad(1)).thenReturn(person2); //tak
        person3.setName("Niewiadomska");
        when(personDao.isPresent(2)).thenReturn(true);
        when(personDao.createAndLoad(2)).thenReturn(person3); //tak
        when(personDao.isPresent(3)).thenReturn(false);

        when(index.chunks()).thenReturn(Stream.of(chunk));
        when(engine.getDaoManager()).thenReturn(daoManager);
        when(daoManager.classToDao(Looks.class)).thenReturn(looksDao);
        when(daoManager.classToDao(Person.class)).thenReturn(personDao);
    }

    @Test
    void filtered() {
        //given
        Indexes indexes = new Indexes(engine, DEFAULT_CHUNK_SIZE);

        //execute
        Index filtered = indexes.filtered(index,
                Looks.class, looks -> looks.getColor() == Color.GOLD, true,
                Person.class, person -> person.getName().equals("Pomponik") || person.getName().equals("Niewiadomska"), false);

        //assert
        assertThat(filtered.chunks().flatMapToInt(Chunk::ids).toArray())
                .containsExactlyInAnyOrder(1,2);

    }
}
