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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.stats.bin.Bin;
import pl.edu.icm.trurl.stats.bin.BinPool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BinPoolTest {

    private enum Color {
        VIOLET, YELLOW, PINK, GOLD
    }

    @Test
    @DisplayName("Should add bins to a pool and count correct total")
    public void add() {
        // given
        BinPool<Color> binPool = new BinPool<>();
        Bin<Color> violets = binPool.add(Color.VIOLET, 123);
        binPool.add(Color.YELLOW, 123);

        // execute
        int countBefore = binPool.getTotalCount();
        violets.pick();
        int countAfter = binPool.getTotalCount();

        // assert
        assertEquals(246, countBefore);
        assertEquals(245, countAfter);
    }

    @Test
    @DisplayName("Should create subpools from a pool")
    public void createSubPool() {
        // given
        BinPool<Color> binPool = new BinPool<>();
        Bin<Color> violets = binPool.add(Color.VIOLET, 10);
        Bin<Color> yellows = binPool.add(Color.YELLOW, 10);
        Bin<Color> golds = binPool.add(Color.GOLD, 10);
        Bin<Color> pinks = binPool.add(Color.PINK, 10);

        int totalCount = binPool.getTotalCount();
        BinPool<Color> sub1 = binPool.createSubPool(Color.VIOLET, Color.YELLOW, Color.GOLD);
        BinPool<Color> sub2 = binPool.createSubPool(Color.YELLOW, Color.GOLD, Color.PINK);

        assertThat(totalCount).isEqualTo(40);
        assertThat(sub1.getTotalCount()).isEqualTo(30);
        assertThat(sub2.getTotalCount()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should sample bins")
    public void sample() {
        // given
        BinPool<Color> binPool = new BinPool<>();
        binPool.add(Color.VIOLET, 3);
        binPool.add(Color.YELLOW, 3);


        // execute
        List<Color> results = new ArrayList<>();
        while (binPool.getTotalCount() > 0) {
            results.add(binPool.sample(0.5).pick());
        }

        // assert
        assertThat(results).containsExactly(
                Color.YELLOW, Color.VIOLET,
                Color.YELLOW, Color.VIOLET,
                Color.YELLOW, Color.VIOLET
        );
    }

    @Test
    public void reset() {
        // given
        BinPool<Color> binPool = new BinPool<>();
        binPool.add(Color.VIOLET, 5);
        binPool.add(Color.GOLD, 3);
        binPool.add(Color.PINK, 2);

        // execute
        binPool.sample(0).pick(10);
        int countBefore = binPool.getTotalCount();
        binPool.sample(0).pick();
        int countAfter = binPool.getTotalCount();

        // assert
        assertThat(countBefore).isEqualTo(0);
        assertThat(countAfter).isEqualTo(9);
    }

    @Test
    void streamBins() {
        // given
        BinPool<Color> binPool = new BinPool<>();
        binPool.add(Color.VIOLET, 5);
        binPool.add(Color.GOLD, 3);
        binPool.add(Color.PINK, 2);

        // execute
        List<Bin<Color>> bins = binPool.streamBins().collect(Collectors.toList());

        // assert
        Assertions.assertThat(bins)
                .extracting(Bin::getLabel, Bin::getCount)
                .containsExactly(
                        tuple(Color.VIOLET, 5),
                        tuple(Color.GOLD, 3),
                        tuple(Color.PINK, 2));
    }

    @Test
    void sampleNth() {
        // given
        BinPool<Color> binPool = new BinPool<>();
        binPool.add(Color.VIOLET, 5);
        binPool.add(Color.GOLD, 3);
        binPool.add(Color.PINK, 2);

        // execute
        List<Bin<Color>> bins = IntStream.range(0, 10)
                .mapToObj(n -> binPool.sampleNth(n))
                .collect(Collectors.toList());

        // assert
        Assertions.assertThat(bins)
                .extracting(Bin::getLabel)
                .containsExactly(
                        Color.VIOLET, Color.VIOLET, Color.VIOLET, Color.VIOLET, Color.VIOLET,
                        Color.GOLD, Color.GOLD, Color.GOLD,
                        Color.PINK, Color.PINK);
    }
}
