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

package pl.edu.icm.trurl.bin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class BinPoolsByShapeTest {

    @Test
    @Disabled("For some reason, stopped working")
    void group() {
        // execute

        // a pool of letter "a"
        BinPoolsByShape<Integer, String> binPoolsByShape = BinPoolsByShape.group(
                Stream.of("ala", "ola", "bela", "rurka", "watażka", "ważka", "penicylina"),
                string -> (int)string.chars().filter(ch -> ch == 'a').count(),
                string -> Stream.of(string.length()));

        // assert
        int totalCountOfLetterA = binPoolsByShape.getAllBins().getTotalCount();
        BinPool<String> aIn5LetterWords = binPoolsByShape.getGroupedBins().get(5);

        assertThat(totalCountOfLetterA).isEqualTo(11);
        assertThat(aIn5LetterWords.getTotalCount()).isEqualTo(3);
        assertThat(binPoolsByShape.getGroupedBins().keySet()).contains(3,4,5,7,10);

    }

}
