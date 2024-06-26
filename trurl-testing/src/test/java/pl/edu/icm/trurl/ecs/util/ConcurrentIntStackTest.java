/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.collection.ConcurrentIntStack;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ConcurrentIntStackTest {

    @Test
    @DisplayName("Push and pop should work correctly in a single-threaded environment")
    void push() {
        // given
        final int size = 10;
        ConcurrentIntStack concurrentIntStack = new ConcurrentIntStack(size);

        // execute
        for (int i = 0; i < size; i++) {
            concurrentIntStack.push(i);
        }

        Set<Integer> results = new HashSet<>(size);
        // assert
        for (int i = 0; i < size; i++) {
            results.add(concurrentIntStack.shift());
        }
        assertThat(concurrentIntStack.shift()).isEqualTo(Integer.MIN_VALUE);
        assertThat(results.containsAll(Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))).isTrue();
    }

    @Test
    @DisplayName("Overflow should just return false and ignore the value (i.e., lose data)")
    void push__overflow() {
        // given
        ConcurrentIntStack concurrentIntStack = new ConcurrentIntStack(3);

        assertThat(concurrentIntStack.push(1)).isTrue();
        assertThat(concurrentIntStack.push(7)).isTrue();
        assertThat(concurrentIntStack.push(7)).isTrue();
        assertThat(concurrentIntStack.push(7)).isFalse();
        assertThat(concurrentIntStack.push(7)).isFalse();
        assertThat(concurrentIntStack.push(7)).isFalse();
        assertThat(concurrentIntStack.push(7)).isFalse();

    }

    int size = 10_000_000;

    @Test
    @DisplayName("Push and pop should work correctly in a multi-threaded environment, under extreme contention")
    void pop() {
        // given
        ConcurrentIntStack concurrentIntStack = new ConcurrentIntStack(size);

        // execute
        IntStream.range(0, size).parallel().forEach(e -> {
            concurrentIntStack.push(e);
            Thread.yield();
        });
        assertThat(concurrentIntStack.push(1)).isFalse();

        int[] output = new int[size];
        IntStream.range(0, size).parallel().forEach(e -> {
            output[concurrentIntStack.shift()] = 1;
            Thread.yield();
        });

        // assert
        assertThat(concurrentIntStack.shift()).isEqualTo(Integer.MIN_VALUE);
        for (int i = 0; i < size; i++) {
            assertThat(output[i]).isEqualTo(1);
        }
    }

    @Test
    @Disabled
    @DisplayName("This test is meant to compare the ConcurrentIntQueue to the ConcurrentLinkedDeque. It is not a real test, it is meant to copy-paste into JMH")
    void alternative() {
        // For small sizes, the ConcurrentLinkedDeque is about 1.3 times faster (even though it allocates millions of objects instead of six)
        // On my machine, the inflection point happens around 7 million elements, when ConcurrentLinkedDeque starts being faster.
        // On 8 millions, it's two times as fast. With 30 million elements, the ConcurrentLinkedDeque throws an OOME after 2 minutes,
        // while the ConcurrentIntQueue finishes in 18 seconds. Admittedly, this is due to GC spinning out of control.
        // given
        ConcurrentLinkedDeque<Integer> concurrentLinkedDeque = new ConcurrentLinkedDeque();
        IntStream.range(0, size).forEach(concurrentLinkedDeque::push);
        assertThat(concurrentLinkedDeque.size()).isEqualTo(size);

        // execute
        int[] output = new int[size];
        IntStream.range(0, size).parallel().forEach(e -> {
            output[concurrentLinkedDeque.pop()] = 1;
            Thread.yield();
        });
        assertThat(concurrentLinkedDeque.isEmpty()).isTrue();
        for (int i = 0; i < size; i++) {
            assertThat(output[i]).isEqualTo(1);
        }
    }
}
