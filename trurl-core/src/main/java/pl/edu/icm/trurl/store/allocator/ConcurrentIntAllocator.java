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

package pl.edu.icm.trurl.store.allocator;

import pl.edu.icm.trurl.ecs.util.ConcurrentIntQueue;

import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentIntAllocator implements Allocator {

    private final AtomicInteger count = new AtomicInteger(0);
    private final ConcurrentIntQueue free;

    public ConcurrentIntAllocator(int initialCapacity) {
        free = new ConcurrentIntQueue(initialCapacity);
    }

    @Override
    public long getCount() {
        return count.get();
    }

    @Override
    public int allocate() {
        int result = free.shift();
        return result == 1 ? count.getAndIncrement() : result;
    }

    @Override
    public void free(int id) {
        free.push(id);
    }
}
