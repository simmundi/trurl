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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.PersonMapper;
import pl.edu.icm.trurl.exampledata.pizza.PizzaMapper;

@ExtendWith(MockitoExtension.class)
public class GetChildMappersIT {
    PizzaMapper pizzaMapper = new PizzaMapper();
    PersonMapper personMapper = new PersonMapper();

    @Test
    @DisplayName("Should return child mappers")
    public void getChildMappers() {
        // assert & execute
        Assertions.assertThat(pizzaMapper.getChildMappers()).containsExactlyInAnyOrder(
                pizzaMapper.getOlivesMapper(), pizzaMapper.getToppingsMapper()
        );
    }

    @Test
    @DisplayName("Should return an empty list for childless mapper")
    public void getChildMappers__empty() {
        // assert & execute
        Assertions.assertThat(personMapper.getChildMappers()).isEmpty();
    }

}
