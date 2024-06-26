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

import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.Person;
import pl.edu.icm.trurl.exampledata.PersonDao;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;
import pl.edu.icm.trurl.store.Store;

@ExtendWith(MockitoExtension.class)
public class DaoNormalizationIT {
    Store store = new Store(new BasicAttributeFactory(), 1);
    PersonDao personDao = new PersonDao("");

    @BeforeEach
    public void before() {
        personDao.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should save Person instance with normalized name")
    public void normalize() {
        // given
        Person person = personDao.create();
        person.setName("  jan KoWaLskI   ");

        // execute
        personDao.save(person, 0);

        // assert
        Assertions.assertThat(personDao.getName(0)).isEqualTo("JAN KOWALSKI");
    }
}
