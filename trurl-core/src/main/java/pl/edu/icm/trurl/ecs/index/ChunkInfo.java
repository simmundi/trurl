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

package pl.edu.icm.trurl.ecs.index;

public class ChunkInfo {

    public static final String DEFAULT_LABEL = "";

    private final int chunkId;
    private final int approximateSize;


    private final String label;

    private ChunkInfo(int chunkId, int approximateSize) {
        this.chunkId = chunkId;
        this.approximateSize = approximateSize;
        label = DEFAULT_LABEL;
    }

    public ChunkInfo(int chunkId, int approximateSize, String label) {
        this.chunkId = chunkId;
        this.approximateSize = approximateSize;
        this.label = label;
    }

    public static ChunkInfo of(int chunkId, int size) {
        return new ChunkInfo(chunkId, size);
    }

    public static ChunkInfo of(int chunkId, int size, String label) {
        return new ChunkInfo(chunkId, size, label);
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getApproximateSize() {
        return approximateSize;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "ChunkInfo{" +
                "label=" + label +
                ", chunkId=" + chunkId +
                ", approxSize=" + approximateSize +
                '}';
    }
}
