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
package pl.edu.icm.trurl.ecs.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.CounterWithSetup;
import pl.edu.icm.trurl.exampledata.CounterWithSetupMapper;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;
import pl.edu.icm.trurl.store.Store;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MapperSetupFeatureIT {
    Store store = new Store(new ArrayAttributeFactory(), 1);
    CounterWithSetupMapper counterMapper = new CounterWithSetupMapper("");

    @BeforeEach
    public void before() {
        counterMapper.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should save Counter and load it")
    public void normalize() {
        // given
        CounterWithSetup counter = counterMapper.create();
        counter.setValue(17f);
        counterMapper.save(counter, 0);

        // execute
        CounterWithSetup loaded = counterMapper.createAndLoad(0);

        // assert
        assertThat(store.attributes().collect(Collectors.toList())).hasSize(1);
        assertThat(loaded.getOriginalValue()).isEqualTo(17f);
        assertThat(loaded.getValue()).isEqualTo(17f);
    }
}
