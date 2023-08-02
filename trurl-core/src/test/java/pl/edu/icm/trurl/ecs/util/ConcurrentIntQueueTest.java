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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrentIntQueueTest {

    @Test
    @DisplayName("Push and pop should work correctly in a single-threaded environment")
    void push() {
        // given
        ConcurrentIntQueue concurrentIntQueue = new ConcurrentIntQueue(1000000);

        // execute
        for (int i = 0; i < 1000000; i++) {
            concurrentIntQueue.push(i);
        }

        // assert
        for (int i = 0; i < 1000000; i++) {
            assertThat(concurrentIntQueue.pop()).isEqualTo(i);
        }
        assertThat(concurrentIntQueue.pop()).isEqualTo(Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Overflow should just return false and ignore the value (i.e., lose data)")
    void push__overflow() {
        // given
        ConcurrentIntQueue concurrentIntQueue = new ConcurrentIntQueue(1000000);

        // execute & assert
        for (int i = 0; i < 1000000; i++) {
            assertThat(concurrentIntQueue.push(i)).isTrue();
        }
        for (int i = 1000000; i < 2000000; i++) {
            assertThat(concurrentIntQueue.push(i)).isFalse();
        }
        for (int i = 0; i < 1000000; i++) {
            assertThat(concurrentIntQueue.pop()).isEqualTo(i);
        }
        assertThat(concurrentIntQueue.pop()).isEqualTo(Integer.MIN_VALUE);
    }


    @Test
    void pop() {
    }
}