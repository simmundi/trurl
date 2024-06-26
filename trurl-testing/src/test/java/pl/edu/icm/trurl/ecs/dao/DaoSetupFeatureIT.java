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
package pl.edu.icm.trurl.ecs.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.CounterWithSetup;
import pl.edu.icm.trurl.exampledata.CounterWithSetupDao;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.Store;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DaoSetupFeatureIT {
    Store store = new Store(new BasicAttributeFactory(), 1);
    CounterWithSetupDao counterDao = new CounterWithSetupDao("");

    @BeforeEach
    public void before() {
        counterDao.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should save Counter and load it")
    public void normalize() {
        // given
        CounterWithSetup counter = counterDao.create();
        counter.setValue(17f);
        counterDao.save(counter, 0);

        // execute
        CounterWithSetup loaded = counterDao.createAndLoad(0);

        // assert
        assertThat(store.getAllAttributes()).hasSize(1);
        assertThat(loaded.getOriginalValue()).isEqualTo(17f);
        assertThat(loaded.getValue()).isEqualTo(17f);
    }
}
