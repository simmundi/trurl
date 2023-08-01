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

public class ConcurrentIntStack {
    private final int[] table;

    private AtomicInteger tail = new AtomicInteger(-1);
    private AtomicInteger head = new AtomicInteger(-1);
    private AtomicInteger sequencedHead = new AtomicInteger(-1);


    public ConcurrentIntStack(int size) {
        this.table = new int[size];
    }

    public boolean push(int value) {
        if (tail.get() == sequencedHead.get() + 1) {
            return false;
        }
        int newHead = head.accumulateAndGet(0, (oldHead, unused) -> {
            int newHeadValue = oldHead + 1;
            if (newHeadValue == table.length) {
                return 0;
            }

            return newHeadValue;
        });
        if (newHead != tail.get()) {
            table[newHead] = value;
        }
        do {} while (!sequencedHead.compareAndSet(newHead - 1, newHead));
        return true;
    }

    public int pop() {
        int newTail = tail.accumulateAndGet(0, (oldTail, unused) -> {
            int newTailValue = oldTail + 1;
            return newTailValue < table.length ? newTailValue : 0;
        });

        if (tail.get() == sequencedHead.get()) {
            return Integer.MIN_VALUE;
        }
        return table[newTail];
    }
}
