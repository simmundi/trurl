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

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EnumTable<R extends Enum, C extends Enum, V> {
    private final Object[] values;
    private final int rows;
    private final int cols;

    public EnumTable(Class<R> rowClass, Class<C> columnClass) {
        rows = rowClass.getEnumConstants().length;
        cols = columnClass.getEnumConstants().length;
        values = new Object[rows * cols];
    }

    public void put(R row, C col, V value) {
        values[row.ordinal() * cols + col.ordinal()] = value;
    }

    public V get(R row, C col) {
        return (V) values[row.ordinal() * cols + col.ordinal()];
    }

    public Stream<V> stream() {
        return IntStream.range(0, values.length)
                .mapToObj(idx -> (V)values[idx])
                .filter(v -> v != null);
    }
}
