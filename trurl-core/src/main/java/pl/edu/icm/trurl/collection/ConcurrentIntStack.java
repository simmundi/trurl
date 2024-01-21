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

package pl.edu.icm.trurl.collection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * A concurrent stack of ints with a fixed capacity.
 *
 * <p>
 * The queue is a bit blocking (synchronized), it should be rewritten with spin locks,
 * but this (due to the ABA problem) will probably require changing the API, by including
 * the chunk id.
 *
 * <p>
 * If the capacity is reached:
 * <li> shift will return Integer.MIN_VALUE
 * <li> push will return false
 */
public class ConcurrentIntStack {
    private final int[] table;

    private final AtomicInteger head = new AtomicInteger(0);

    public ConcurrentIntStack(int size) {
        this.table = new int[size];
    }

    public synchronized boolean push(int value) {
        int oldHead = getAndUpdate(head -> {
            if (head < table.length) {
                table[head] = value;
                return head + 1;
            } else {
                return head;
            }
        });
        return oldHead < table.length;
    }

    /**
     * Returns the lst pushed value from the stack, or Integer.MIN_VALUE if the stack is empty.
     * @return
     */
    synchronized public int shift() {
        AtomicInteger valueRef = new AtomicInteger(Integer.MIN_VALUE);
        getAndUpdate(head -> {
            if (head == 0) {
                return head;
            } else {
                valueRef.set(table[head - 1]);
                return head - 1;
            }
        });
        return valueRef.get();
    }

    private final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev, next;
        do {
            prev = head.get();
            next = updateFunction.applyAsInt(prev);
        } while (!head.compareAndSet(prev, next));
        return prev;
    }

}
