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

package pl.edu.icm.trurl.ecs.mapper;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.config.Configurer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.*;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.chess.RowsFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperSoftEnumsIT {



    @Test
    @DisplayName("Should correctly map a component with a SoftEnum")
    void test() throws IOException {
        // given
        Store store = new ArrayStore();
        Bento config = new Configurer().loadConfigResource("/contamination.properties").getConfig();
        HealthMapper healthMapper = config.get(HealthMapperFactory.IT);
        ContaminationTypes contaminationTypes = config.get(ContaminationTypesFactory.IT);

        ContaminationType WILD = contaminationTypes.getByOrdinal(0);
        ContaminationType ALPHA = contaminationTypes.getByOrdinal(1);
        ContaminationType DELTA = contaminationTypes.getByOrdinal(2);

        Health health1 = new Health(0.2f, WILD);
        Health health2 = new Health(0.4f, ALPHA);
        Health health3 = new Health(0.6f, DELTA);

        healthMapper.configureAndAttach(store);

        // execute
        healthMapper.save(health1, 0);
        healthMapper.save(health2, 1);
        healthMapper.save(health3, 2);

        // assert
        assertThat(healthMapper.getContaminationType(0)).isEqualTo(contaminationTypes.getByName("WILD"));
        assertThat(healthMapper.getContaminationType(1)).isEqualTo(contaminationTypes.getByName("ALPHA"));
        assertThat(healthMapper.getContaminationType(2)).isEqualTo(contaminationTypes.getByName("DELTA"));
        assertThat(healthMapper.getContaminationType(3)).isNull();

        assertThat(healthMapper.createAndLoad(0).getContaminationType()).isSameAs(WILD);
        assertThat(healthMapper.createAndLoad(1).getContaminationType()).isSameAs(ALPHA);
        assertThat(healthMapper.createAndLoad(2).getContaminationType()).isSameAs(DELTA);
    }
}
