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

import net.snowyhollows.bento.soft.SoftEnum;
import net.snowyhollows.bento.soft.SoftEnumManager;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SoftEnumCube<R extends SoftEnum, C extends SoftEnum, L extends SoftEnum, V> {
    private final Object[] values;
    private final SoftEnumManager<R> rows;
    private final SoftEnumManager<C> cols;
    private final SoftEnumManager<L> layers;
    private final int rowCount;
    private final int columnCount;
    private final int layerCount;
    private final int layerSize;

    public SoftEnumCube(SoftEnumManager<R> rowManager, SoftEnumManager<C> colManager, SoftEnumManager<L> layers) {
        rows = rowManager;
        cols = colManager;
        this.layers = layers;
        rowCount = rows.values().size();
        columnCount = cols.values().size();
        layerSize = rowCount * columnCount;
        layerCount = layers.values().size();
        values = new Object[rowCount * columnCount * layerCount];
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getLayerCount() { return layerCount; }

    public void put(R row, C col, L layer, V value) {
        values[layerSize * layer.ordinal() + row.ordinal() * columnCount + col.ordinal()] = value;
    }

    public V get(R row, C col, L layer) {
        return (V) values[layerSize * layer.ordinal() + row.ordinal() * columnCount + col.ordinal()];
    }

    public Stream<Quadruple<R, C, L, V>> stream() {
        return IntStream.range(0, values.length)
                .mapToObj(idx -> {
                    int layer = idx / layerSize;
                    int rest = idx % layerSize;
                    int row = rest / columnCount;
                    int col = rest % columnCount;
                    return values[idx] == null ? null : Quadruple.of(rows.getByOrdinal(row),
                            cols.getByOrdinal(col),
                            layers.getByOrdinal(layer),
                            (V) values[idx]);
                }).filter(quadruple -> quadruple != null);
    }
}
