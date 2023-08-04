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

package pl.edu.icm.trurl.ecs.parallel;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.parallel.domain.Counter;
import pl.edu.icm.trurl.ecs.parallel.domain.CounterMapper;
import pl.edu.icm.trurl.ecs.parallel.domain.HasAAndB;
import pl.edu.icm.trurl.ecs.parallel.domain.ParallelCounter;
import pl.edu.icm.trurl.ecs.parallel.domain.ParallelCounterMapper;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.util.Status;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

public class CounterWithSetupParallelIT {
    final int SIZE = 10_000;
    final int CONTENTION = 4;
    final int PER_SESSION = 1000;
    final int LOAD = 3;

    @Test
    void test_parallel() {
        // given
        Store store = new ArrayAttributeFactory(SIZE);
        ParallelCounterMapper parallelMapper = new ParallelCounterMapper();
        parallelMapper.configureAndAttach(store);
        this.prepareZeroedCounters(parallelMapper);
        parallelMapper.lifecycleEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);

        // execute
        Status status = Status.of("using counters in parallel: " + createMessage());
        SessionFactory sessionFactory = new SessionFactory(null, Session.Mode.STUB_ENTITIES, 0);

        IntStream.range(0, SIZE * CONTENTION).parallel().forEach(chunkId -> {
            int startId = ThreadLocalRandom.current().nextInt(0, SIZE);
            Session session = sessionFactory.create(chunkId + 1);
            for (int i = 0; i < PER_SESSION; i++) {
                int id = (startId + i) % SIZE;
                ParallelCounter counter = parallelMapper.create();
                parallelMapper.load(session, counter, id);
                performLogicOnCounter(counter);
                parallelMapper.save(session, counter, id);
            }
        });
        status.done();

        // assert
        int problems = verifyAndDumpDebugInfo(parallelMapper);
        assertThat(problems).isZero();
    }

    @Test
    void test_sequential() {
        // given
        Store store = new ArrayAttributeFactory(SIZE);
        CounterMapper counterMapper = new CounterMapper();
        counterMapper.configureAndAttach(store);
        prepareZeroedCounters(counterMapper);

        Status status = Status.of("using counters in sequence: " + createMessage());
        // execute
        IntStream.range(0, SIZE * CONTENTION).sequential().forEach(chunkId -> {
            int startId = ThreadLocalRandom.current().nextInt(0, SIZE);

            for (int i = 0; i < PER_SESSION; i++) {
                int id = (startId + i) % SIZE;
                Counter counter = counterMapper.create();
                counterMapper.load(null, counter, id);
                performLogicOnCounter(counter);
                counterMapper.save(counter, id);
            }
        });

        status.done();

        // assert
        int problems = verifyAndDumpDebugInfo(counterMapper);
        assertThat(problems).isZero();
    }

    @Test
    void test_sequential_incorrect_results() {
        // given
        Store store = new ArrayAttributeFactory(SIZE);
        CounterMapper sequentialMapper = new CounterMapper();
        sequentialMapper.configureAndAttach(store);
        prepareZeroedCounters(sequentialMapper);

        // execute
        Status status = Status.of("using sequential mapper in parallel (which is wrong): " + createMessage());
        IntStream.range(0, SIZE * CONTENTION).parallel().forEach(chunkId -> {
            int startId = ThreadLocalRandom.current().nextInt(0, SIZE);

            for (int i = 0; i < PER_SESSION; i++) {
                int id = (startId + i) % SIZE;
                Counter counter = sequentialMapper.createAndLoad(id);
                performLogicOnCounter(counter);
                sequentialMapper.save(counter, id);
            }
        });

        status.done();

        // assert
        int problems = verifyAndDumpDebugInfo(sequentialMapper);
        assertThat(problems).isNotZero();
    }

    private void performLogicOnCounter(HasAAndB counter) {
        counter.setA(counter.getA() + 1);
        int a = counter.getA();
        counter.setB(counter.getB() - 1);

        double calculations = 1 / a;
        for (int i = 0; i < LOAD; i++) {
            calculations *= Math.sin(calculations);
        }

        // The check below is so that `calculations` can theoretically
        // have effect on execution; otherwise JIT
        // could optimize away the whole loop above.
        if (calculations > LOAD) {
            throw new IllegalStateException("sinus returned more than one");
        }
    }

    private String createMessage() {
        return "(" + SIZE + " counters, " + (SIZE * CONTENTION * PER_SESSION) + " operations)";
    }

    private <T> void prepareZeroedCounters(Mapper<T> mapper) {
        Status status = Status.of("creating counters");
        SessionFactory sessionFactory = new SessionFactory(null, Session.Mode.STUB_ENTITIES);
        Session session = sessionFactory.create();
        T counter = mapper.create();
        for (int i = 0; i < SIZE; i++) {
            mapper.save(session, counter, i);
        }
        status.done();
    }

    private <T extends HasAAndB> int verifyAndDumpDebugInfo(Mapper<T> mapper) {
        int sum = 0;
        int corrupt = 0;
        for (int i = 0; i < SIZE; i++) {
            T counter = mapper.create();
            mapper.load(null, counter, i);
            sum += counter.getA();
            if (counter.getA() != -counter.getB()) {
                corrupt++;
            }
        }
        int off = Math.abs((SIZE * CONTENTION * PER_SESSION) - sum);
        System.out.println("off by: " + off + "; corrupt: " + corrupt);
        return off + corrupt;
    }


}
