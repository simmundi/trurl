///*
// * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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
//package pl.edu.icm.trurl.ecs.parallel;
// TODO:
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import pl.edu.icm.trurl.ecs.*;
//import pl.edu.icm.trurl.ecs.Session;
//import pl.edu.icm.trurl.ecs.dao.Dao;
//import pl.edu.icm.trurl.ecs.dao.LifecycleEvent;
//import pl.edu.icm.trurl.ecs.parallel.domain.*;
//import pl.edu.icm.trurl.ecs.parallel.domain.Counter;
//import pl.edu.icm.trurl.ecs.parallel.domain.ParallelCounterDao;
//import pl.edu.icm.trurl.store.Store;
//import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
//import pl.edu.icm.trurl.util.Status;
//
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.IntStream;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class CounterWithSetupParallelIT {
//    final int SIZE = 10_000;
//    final int CONTENTION = 4;
//    final int PER_SESSION = 1000;
//    final int LOAD = 3;
//
//    @Mock
//    Engine engine;
//    @Mock
//    DaoManager daoManager;
//
//    @Test
//    void test_parallel() {
//        // given
//        when(engine.getDaoManager()).thenReturn(daoManager);
//        Store store = new Store(new BasicAttributeFactory(), SIZE);
//        ParallelCounterDao parallelDao = new ParallelCounterDao("");
//        parallelDao.configureAndAttach(store);
//        this.prepareZeroedCounters(parallelDao);
//        parallelDao.fireEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);
//
//        // execute
//        Status status = Status.of("using counters in parallel: " + createMessage());
//        SessionFactory sessionFactory = new SessionFactory(engine, 0);
//
//        IntStream.range(0, SIZE * CONTENTION).parallel().forEach(chunkId -> {
//            int startId = ThreadLocalRandom.current().nextInt(0, SIZE);
//            Session session = sessionFactory.createOrGet();
//            session.setOwnerId(chunkId + 1);
//            for (int i = 0; i < PER_SESSION; i++) {
//                int id = (startId + i) % SIZE;
//                ParallelCounter counter = parallelDao.create();
//                parallelDao.load(session, counter, id);
//                performLogicOnCounter(counter);
//                parallelDao.save(session, counter, id);
//            }
//        });
//        status.done();
//
//        // assert
//        int problems = verifyAndDumpDebugInfo(parallelDao);
//        assertThat(problems).isZero();
//    }
//
//    @Test
//    void test_sequential() {
//        // given
//        Store store = new Store(new BasicAttributeFactory(), SIZE);
//        CounterDao counterDao = new CounterDao("");
//        counterDao.configureAndAttach(store);
//        prepareZeroedCounters(counterDao);
//
//        Status status = Status.of("using counters in sequence: " + createMessage());
//        // execute
//        IntStream.range(0, SIZE * CONTENTION).sequential().forEach(chunkId -> {
//            int startId = ThreadLocalRandom.current().nextInt(0, SIZE);
//
//            for (int i = 0; i < PER_SESSION; i++) {
//                int id = (startId + i) % SIZE;
//                Counter counter = counterDao.create();
//                counterDao.load(null, counter, id);
//                performLogicOnCounter(counter);
//                counterDao.save(counter, id);
//            }
//        });
//
//        status.done();
//
//        // assert
//        int problems = verifyAndDumpDebugInfo(counterDao);
//        assertThat(problems).isZero();
//    }
//
//    @Test
//    void test_sequential_incorrect_results() {
//        // given
//        Store store = new Store(new BasicAttributeFactory(), SIZE);
//        CounterDao sequentialDao = new CounterDao("");
//        sequentialDao.configureAndAttach(store);
//        prepareZeroedCounters(sequentialDao);
//
//        // execute
//        Status status = Status.of("using sequential dao in parallel (which is wrong): " + createMessage());
//        IntStream.range(0, SIZE * CONTENTION).parallel().forEach(chunkId -> {
//            int startId = ThreadLocalRandom.current().nextInt(0, SIZE);
//
//            for (int i = 0; i < PER_SESSION; i++) {
//                int id = (startId + i) % SIZE;
//                Counter counter = sequentialDao.createAndLoad(id);
//                performLogicOnCounter(counter);
//                sequentialDao.save(counter, id);
//            }
//        });
//
//        status.done();
//
//        // assert
//        int problems = verifyAndDumpDebugInfo(sequentialDao);
//        assertThat(problems).isNotZero();
//    }
//
//    private void performLogicOnCounter(HasAAndB counter) {
//        counter.setA(counter.getA() + 1);
//        int a = counter.getA();
//        counter.setB(counter.getB() - 1);
//
//        double calculations = 1 / a;
//        for (int i = 0; i < LOAD; i++) {
//            calculations *= Math.sin(calculations);
//        }
//
//        // The check below is so that `calculations` can theoretically
//        // have effect on execution; otherwise JIT
//        // could optimize away the whole loop above.
//        if (calculations > LOAD) {
//            throw new IllegalStateException("sinus returned more than one");
//        }
//    }
//
//    private String createMessage() {
//        return "(" + SIZE + " counters, " + (SIZE * CONTENTION * PER_SESSION) + " operations)";
//    }
//
//    private <T> void prepareZeroedCounters(Dao<T> dao) {
//        Status status = Status.of("creating counters");
//        T counter = dao.create();
//        for (int i = 0; i < SIZE; i++) {
//            dao.save(counter, i);
//        }
//        status.done();
//    }
//
//    private <T extends HasAAndB> int verifyAndDumpDebugInfo(Dao<T> dao) {
//        int sum = 0;
//        int corrupt = 0;
//        for (int i = 0; i < SIZE; i++) {
//            T counter = dao.create();
//            dao.load(null, counter, i);
//            sum += counter.getA();
//            if (counter.getA() != -counter.getB()) {
//                corrupt++;
//            }
//        }
//        int off = Math.abs((SIZE * CONTENTION * PER_SESSION) - sum);
//        System.out.println("off by: " + off + "; corrupt: " + corrupt);
//        return off + corrupt;
//    }
//
//
//}
