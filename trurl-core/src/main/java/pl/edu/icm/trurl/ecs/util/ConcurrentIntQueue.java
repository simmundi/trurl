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

    // sequencedHead is the index of the first free slot; setting sequencedHead happens-after
    // the last value is written to the table, and (except for wrapping from the end to the beginning) - it
    // is guaranteed to me monotonic.
    private AtomicInteger sequencedHead = new AtomicInteger(1);
    // tail is the index of the first potentially occupied slot. If tail == sequencedHead, the queue is empty.
    private AtomicInteger tail = new AtomicInteger(1);
    // head is the index of the first free slot, but it has additional meaning - if it is negative, it means that
    // the last try to push failed because the queue was full. This is because the operation of generating the next
    // value must be atomic, but needs to return two values: whether the operation succeeded, and the value itself.
    // We are using the fact that the queue is one-based, so a valid index must be positive.
    private AtomicInteger head = new AtomicInteger(1);

    public ConcurrentIntQueue(int size) {
        this.table = new int[size + 1];
    }

    /**
     * Pushes a value to the queue. If the queue is full, returns false, otherwise returns true.
     * @param value
     * @return
     */
    public boolean push(int value) {
        // The algorithm is as follows:
        // 1. Try to increment head and wrap it around if necessary
        // 2. If the new head is equal to tail, the queue is full.
        // 3. The above operation is atomic, but it needs to return two values: whether the operation succeeded, and
        //    the value itself. We are using the fact that the queue is one-based (a valid index must be positive) to
        //    pass the information about the success of the operation as the sign of the value.
        // 3. If the new head is negative, it means that the queue was full, and the push fails.
        // 4. If the new head is positive, it means that the queue was not full, and the value can be written to the
        //    slot at the previous head.
        // 5. We set the sequencedHead to the new head, which means that the value is now visible to the readers. This
        //    must be done in a loop with a CAS, because the sequencedHead can be changed by other threads and we
        //    need to make sure this happens monotonically.

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

    /**
     * Returns the oldest value from the queue, or Integer.MIN_VALUE if the queue is empty.
     * @return
     */
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
