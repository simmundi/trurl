/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RangeIndexTest {

    @Test
    @DisplayName("should create correct number of chunks with correct last chunk count")
    void of() {
        //given
        //execute
        RangeIndex rangeSelector = RangeIndex.of(0, 123, 11);
        //assert
        assertThat(rangeSelector.chunks().count()).isEqualTo(12);
        assertThat(rangeSelector.chunks().reduce((first, second) -> second).orElseThrow(NoSuchElementException::new)
                .ids()
                .count())
                .isEqualTo(2);
    }

}