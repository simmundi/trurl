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

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;

/**
 * A concurrent queue of ints with a fixed capacity.
 *
 * The queue is not blocking. If the capacity is extended:
 * <li>> shift will return Integer.MIN_VALUE
 * <li> push will return false
 */
public class ConcurrentIntQueue {
    // The table acts as a ring buffer.
    private final int[] table;

    // All the indices are one-based, 0 is not used.
    // head is special: it can be negative, which means that the queue is full
    private AtomicInteger head = new AtomicInteger(1);
    private AtomicInteger tail = new AtomicInteger(1);
    private AtomicInteger sequencedHead = new AtomicInteger(1);

    public ConcurrentIntQueue(int size) {
        this.table = new int[size + 1];
    }

    public boolean push(int value) {
        int nextHead = head.accumulateAndGet(0, (previous, unused) -> {
            previous = abs(previous);
            int candidate = incrementAndWrapOneBased(previous);
            if (candidate == tail.get()) {
                return -previous;
            } else {
                return candidate;
            }
        });

        if (nextHead < 0) {
            return false;
        }


        int formerHead = nextHead == 1 ? table.length : nextHead - 1;
        table[formerHead - 1] = value;

        while (true) {
            if (sequencedHead.compareAndSet(formerHead, nextHead)) {
                return true;
            }
        }
    }

    public int shift() {
        while (true) {
            int knownHead = sequencedHead.get();
            int knownTail = tail.get();

            if (knownHead == knownTail) {
                return Integer.MIN_VALUE;
            }

            int nextTail = incrementAndWrapOneBased(knownTail);

            if (tail.compareAndSet(knownTail, nextTail)) {
                return table[knownTail - 1];
            }
        }
    }

    private int incrementAndWrapOneBased(int index) {
        return (index + 1) == table.length + 1 ? 1 : (index + 1);
    }
}
