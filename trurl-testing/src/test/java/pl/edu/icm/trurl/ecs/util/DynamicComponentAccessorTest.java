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

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Stats;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicComponentAccessorTest {

    @Test
    void classToIndex() {
        // given
        DynamicComponentAccessor ci = new DynamicComponentAccessor(Arrays.asList(Looks.class, Stats.class));

        // execute
        int looksIdx = ci.classToIndex(Looks.class);
        int statsIdx = ci.classToIndex(Stats.class);

        // assert
        assertThat(looksIdx).isEqualTo(0);
        assertThat(statsIdx).isEqualTo(1);
    }

    @Test
    void indexToClass() {
        // given
        DynamicComponentAccessor ci = new DynamicComponentAccessor(Arrays.asList(Looks.class, Stats.class));

        // execute
        Class<?> statsClass = ci.indexToClass(1);
        Class<?> looksClass = ci.indexToClass(0);

        // assert
        assertThat(statsClass).isEqualTo(Stats.class);
        assertThat(looksClass).isEqualTo(Looks.class);
    }

    @Test
    void componentCount() {
        // given
        DynamicComponentAccessor ci = new DynamicComponentAccessor(Arrays.asList(Looks.class, Stats.class));

        // execute
        int count = ci.componentCount();

        // assert
        assertThat(count).isEqualTo(2);
    }
}
