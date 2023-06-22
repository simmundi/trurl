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

package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.util.ConcurrentIntQueue;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles used/unused slots in a contignous, zero-based range of integers.
 *
 * It is meant to use by Store, to handle assigning ids to new entities and
 * to recycle ids of deleted entities.
 *
 * Counters are thread-safe.
 *
 * A future plans include addind a slab allocator, to help avoid fragmentation;
 * to measure performance of a bitmap-based allocator.
 *
 * A short term plan is to publish the free list, so that clients can persist
 * it and reuse the counter after restart (a store, for example, should save the free list
 * as an attribute.)
 */
final public class Counter {
    private final AtomicInteger count = new AtomicInteger();
    private final ConcurrentIntQueue freeList;

    public Counter(int freeListSize) {
        this.freeList = new ConcurrentIntQueue(freeListSize);
    }

    public int next() {
        int free = freeList.shift();
        return free == Integer.MIN_VALUE ? count.getAndIncrement() : free;
    }

    public int next(int delta) {
        return count.getAndAdd(delta);
    }

    public int getCount() {
        return count.get();
    }

    public void free(int id) {
        freeList.push(id);
    }

    public void free(int id, int delta) {
        for (int i = id; i < id + delta; i++) {
            freeList.push(i);
        }
    }
}
