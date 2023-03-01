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

package pl.edu.icm.trurl.ecs.util;

import it.unimi.dsi.fastutil.ints.IntArrays;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.util.ConcurrentInsertIntArray;

import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.*;

public class ArraySelector implements Selector {
    private final static int DEFAULT_CHUNK_SIZE = 1024;
    private final static int DEFAULT_INITIAL_SIZE = DEFAULT_CHUNK_SIZE * 24;

    private final ConcurrentInsertIntArray ids;
    private final int chunkSize;

    public ArraySelector(int initialSize, int chunkSize) {
        ids = new ConcurrentInsertIntArray(initialSize);
        this.chunkSize = chunkSize;
    }

    public ArraySelector(int[] array, int chunkSize) {
        this(array.length, chunkSize);
       addAll(array);
    }

    @Override
    public int estimatedChunkSize() {
        return chunkSize;
    }

    public void shuffle(Random random) {
        IntArrays.shuffle(ids.elements(), 0, ids.getCurrentSize(), random);
    }

    public ArraySelector(int[] array) {
        this(array, DEFAULT_CHUNK_SIZE);
    }

    public void addAll(int[] array) {
        for(int i : array){
            ids.add(i);
        }
    }

    public ArraySelector(int initialSize) {
        this(initialSize, DEFAULT_CHUNK_SIZE);
    }

    public ArraySelector() {
        this(DEFAULT_INITIAL_SIZE);
    }

    public void add(int id) {
        ids.add(id);
    }

    public int getIdByIndex(int index) {
        return ids.getInt(index);
    }

    public int getCount() {
        return ids.getCurrentSize();
    }

    @Override
    public Stream<Chunk> chunks() {
        int size = ids.getCurrentSize();
        int units = size / chunkSize;
        int lastSize = size % chunkSize;
        return concat(
                range(0, units).mapToObj(unit ->
                        new Chunk(ChunkInfo.of(unit, chunkSize), range(unit * chunkSize, unit * chunkSize + chunkSize).map(ids::getInt))),
                lastSize > 0 ? of(new Chunk(ChunkInfo.of(units, lastSize), range(units * chunkSize, size).map(ids::getInt))) : empty());
    }
}
