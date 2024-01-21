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

package pl.edu.icm.trurl.stats.bin;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import pl.edu.icm.trurl.util.Pair;

import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a multidimensional histogram, where bins are grouped into shapes,
 * such that one bin can be described by more than one shape.
 *
 * For example, given 4 places in a green car, 1 place on a red bike,
 * 1 places on a green scooter and 10 places in a red bus, we can
 * create a BinPoolsByShape object representing 4 bins with total of 16 places,
 * which will also allow us to sample "places in two-wheeled vehicles",
 * "places in green vehicles" and "places in red vehicles".
 *
 * The programmer must supply their own SHAPE implementation, which can
 * be as simple as a string, or as complex as a value objects extracting
 * chosen qualities of the LABEL objects.
 *
 * Typically, an instance will be created by the static method group, supplying:
 * <ul>
 *     <li>stream of LABELs</li>
 *     <li>function extracting count from a LABEL</li>
 *     <li>function extracting any number of shapes from a LABEL</li>
 * </ul>
 *
 * @param <SHAPE>
 * @param <LABEL>
 */
public final class BinPoolsByShape<SHAPE, LABEL> {
    private final BinPool<LABEL> allBins;
    private final Map<SHAPE, BinPool<LABEL>> groupedBins;

    public BinPoolsByShape(BinPool<LABEL> allBins, Map<SHAPE, BinPool<LABEL>> groupedBins) {
        this.allBins = allBins;
        this.groupedBins = groupedBins;
    }

    public BinPool<LABEL> getAllBins() {
        return allBins;
    }

    public Map<SHAPE, BinPool<LABEL>> getGroupedBins() {
        return groupedBins;
    }

    public static <SHAPE, LABEL> BinPoolsByShape<SHAPE, LABEL> group(
            Stream<LABEL> entities,
            ToIntFunction<LABEL> countExtractor,
            Function<LABEL, Stream<SHAPE>> shapeExtractor) {

        BinPool<LABEL> allBins = new BinPool<LABEL>();

        Multimap<SHAPE, Bin<LABEL>> bins = entities
                .flatMap(entity -> {
                    int count = countExtractor.applyAsInt(entity);
                    if (count <= 0) {
                        return Stream.empty();
                    } else {
                        Bin<LABEL> entityPool = allBins.add(entity, count);
                        return shapeExtractor.apply(entity)
                                .map(s -> Pair.of(s, entityPool));
                    }
                }).collect(Multimaps.<Pair<SHAPE, Bin<LABEL>>, SHAPE, Bin<LABEL>, Multimap<SHAPE, Bin<LABEL>>>toMultimap(
                        pair -> pair.first,
                        pair -> pair.second,
                        MultimapBuilder.hashKeys().arrayListValues()::build
                ));

        Map<SHAPE, BinPool<LABEL>> groupedBins = bins.keySet().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        cell -> {
                            BinPool<LABEL> pool = new BinPool<LABEL>();
                            bins.get(cell).forEach(pool::addBin);
                            return pool;
                        }
                ));

        return new BinPoolsByShape<>(allBins, groupedBins);
    }


}
