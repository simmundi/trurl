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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.RandomAccessSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ManuallyChunkedArraySelector implements RandomAccessSelector {
    private final static int DEFAULT_INITIAL_SIZE = 1_000_000;
    private final static int DEFAULT_INITIAL_CHUNKS = 1_000;

    private final IntArrayList ids;
    private final IntArrayList chunks;
    private final List<String> labels;

    public ManuallyChunkedArraySelector(int initialSize, int chunkCount) {
        ids = new IntArrayList(initialSize);
        chunks = new IntArrayList(chunkCount + 1);
        labels = new ArrayList<>(chunkCount);
        chunks.add(0);
    }

    @Override
    public synchronized int getInt(float index) {
        return ids.getInt((int) (ids.size() * index));
    }

    public synchronized void add(int id) {
        ids.add(id);
    }

    public synchronized void endChunk() {
        endChunk("default chunk");
    }

    public synchronized void endChunk(String label) {
        chunks.add(ids.size());
        labels.add(label);
    }

    public synchronized int getCount() {
        return ids.size();
    }

    public synchronized int getRunningSize() {
        return getCount() - chunks.getInt(chunks.size() - 1);
    }

    @Override
    public synchronized Stream<Chunk> chunks() {
        return IntStream.range(0, chunks.size() - 1)
                .mapToObj(chunkId -> {
                    int firstInc = chunks.getInt(chunkId);
                    int lastExc = chunks.getInt(chunkId + 1);
                    int size = lastExc - firstInc;
                    return
                            new Chunk(ChunkInfo.of(chunkId, size, labels.get(chunkId)),
                                    IntStream.range(firstInc, lastExc)
                                            .map(i -> ids.getInt(i)));
                });
    }
}
