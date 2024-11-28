///*
// * Copyright (c) 2024 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// *
// */
// TODO:
//package pl.edu.icm.trurl.ecs;
//
//import net.snowyhollows.bento.Bento;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import pl.edu.icm.trurl.exampledata.Person;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class SessionTest {
//    private Engine engine;
//    private final static int CAPACITY = 10000;
//    private final static int CHUNKS = 100;
//    private final static int CHUNK_SIZE = CAPACITY / CHUNKS;
//
//    @BeforeEach
//    void before() {
//        Bento root = Bento.createRoot();
//        root.register(EngineBuilder.INITIAL_CAPACITY, CAPACITY);
//        EngineBuilder eb = root.get(EngineBuilderFactory.IT);
//        eb.addComponentClass(Person.class);
//        engine = eb.getEngine();
//    }
//
//    @Test
//    public void testCreate() {
//        // execute
//        fillRandomly();
//
//        // assert
//        Assertions.assertThat(engine.getCount()).isEqualTo(CAPACITY);
//    }
//
//    @Test
//    public void testDelete() {
//        // given
//        fillRandomly();
//
//        // execute
//        IntStream.range(0, CHUNKS).parallel().forEach(i -> engine.execute(sf -> {
//            Session session = sf.createOrGet();
//            int first = i * CHUNK_SIZE;
//            int last = first + CHUNK_SIZE - 1;
//            for (int j = first; j < last; j++) {
//                Entity e = session.getEntity(j);
//                if (j == first) {
//                    e.delete();
//                }
//            }
//            session.close();
//        }));
//
//        // assert
//        Set<Integer> freeRows = IntStream.range(0, CHUNKS).mapToObj(i -> engine.allocateNextId()).collect(Collectors.toUnmodifiableSet());
//        Set<Integer> planned = IntStream.range(0, CHUNKS).mapToObj(i -> i * CHUNK_SIZE).collect(Collectors.toUnmodifiableSet());
//        int next = engine.allocateNextId();
//
//        Assertions.assertThat(freeRows).isEqualTo(planned);
//        Assertions.assertThat(next).isEqualTo(CAPACITY);
//    }
//
//    private void fillRandomly() {
//        engine.execute(sf -> {
//            Session session = sf.createOrGet();
//            for (int i = 0; i < CAPACITY; i++) {
//                session.createEntity(new Person("name" + i));
//            }
//        });
//    }
//}
