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

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SoftEnumTable<R extends SoftEnum, C extends SoftEnum, V> {
    private final Object[] values;
    private final SoftEnumManager<R> rows;
    private final SoftEnumManager<C> cols;
    private final int rowCount;
    private final int columnCount;

    public SoftEnumTable(SoftEnumManager<R> rowManager, SoftEnumManager<C> colManager) {
        rows = rowManager;
        cols = colManager;
        rowCount = rows.values().size();
        columnCount = cols.values().size();
        values = new Object[rowCount * columnCount];
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void put(R row, C col, V value) {
        values[row.ordinal() * columnCount + col.ordinal()] = value;
    }

    public V get(R row, C col) {
        return (V) values[row.ordinal() * columnCount + col.ordinal()];
    }

    public Stream<Triple<R, C, V>> stream() {
        return IntStream.range(0, values.length)
                .mapToObj(idx -> {
                    int row = idx / columnCount;
                    int col = idx % columnCount;
                    return values[idx] == null ? null : Triple.of(rows.getByOrdinal(row), cols.getByOrdinal(col), (V)values[idx]);
                }).filter(triple -> triple != null);
    }
}
