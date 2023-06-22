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

package pl.edu.icm.trurl.util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkPositionIndex;

public class ConcurrentInsertIntArray {
    public static final int defaultMaxSize = 40_000_000;

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int maxSize;
    private final int[] table;

    public ConcurrentInsertIntArray(int maxSize) {
        this.maxSize = maxSize;
        this.table = new int[maxSize];
    }

    public void add(int value) {
        try {
            // tee-hee... this is an invalid publication of an int.
            table[counter.getAndIncrement()] = value;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Maximum table size exceeded");
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getCurrentSize() {
        return counter.get();
    }

    public boolean contains(int id) {
        for (int i = 0; i < counter.get(); i++) {
            if (id == table[i]) {
                return true;
            }
        }
        return false;
    }

    public IntStream stream() {
        return Arrays.stream(this.table, 0, counter.get());
    }

    public int[] elements() {
        return table;
    }

    public int getInt(int index) {
        checkPositionIndex(index, getCurrentSize() - 1, "index out of bounds");
        return table[index];
    }
}
