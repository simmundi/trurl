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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.Coordinates;
import pl.edu.icm.trurl.exampledata.MapperOfThingFactory;
import pl.edu.icm.trurl.exampledata.Thing;
import pl.edu.icm.trurl.exampledata.ThingMapper;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedDenseIT {
    public static final int SIZE = 10_000;
    Store store = new Store(new ArrayAttributeFactory(), 10_000);
    ThingMapper thingMapper;

    @BeforeEach
    void beforeEach() {
        Bento config = new Configurer().setParam("mapperPrefix", "").getConfig();
        thingMapper = config.get(MapperOfThingFactory.IT);
        thingMapper.configureAndAttach(store);
    }

    @Test
    void test1() {
        // execute
        IntStream.range(0, 10_000).forEach(i -> {
            Thing thing = new Thing();
            thing.setIndex(i);
            if (i % 50 == 0) {
                thing.setCoordinates(new Coordinates(i, -i));
            }
            thingMapper.save(thing, store.getCounter().next());
        });

        // assert
        assertThat(store.getCounter().getCount()).isEqualTo(SIZE);
        assertThat(store.getSubstore("coordinates").getCounter().getCount()).isEqualTo(SIZE / 50);

        IntStream.range(0, SIZE).forEach(i -> {
            Thing thing = thingMapper.createAndLoad(i);
            if (i % 50 == 0) {
                assertThat(thing.getCoordinates().getX()).isEqualTo(i);
                assertThat(thing.getCoordinates().getY()).isEqualTo(-i);
            } else {
                assertThat(thing.getCoordinates()).isNull();
            }
        });
    }
}
