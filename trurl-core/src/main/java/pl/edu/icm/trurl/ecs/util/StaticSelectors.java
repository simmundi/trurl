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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.Selector;

import java.util.stream.Stream;

import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;

public class StaticSelectors {

    private EngineConfiguration engineConfiguration;

    @WithFactory
    public StaticSelectors(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public StaticSelectorConfigBuilder config() {
        return new StaticSelectorConfigBuilder();
    }

    public Selector select(StaticSelectorConfig staticSelectorConfig) {
        return new StaticSelector(
                staticSelectorConfig.initialSize,
                staticSelectorConfig.chunkSize,
                staticSelectorConfig.components);
    }

    private class StaticSelector implements Selector {
        private final IntArrayList ids;
        private final int chunkSize;

        private StaticSelector(int initialSize, int chunkSize, Class<?>... components) {
            this.chunkSize = chunkSize > 0 ? chunkSize : 1024;
            ids = new IntArrayList((int) (initialSize > 0 ? initialSize : engineConfiguration.getEngine().getCount() * 0.5));
            Mapper[] mappers = new Mapper[components.length];
            for (int i = 0; i < components.length; i++) {
                mappers[i] = engineConfiguration.getEngine().getMapperSet().classToMapper(components[i]);
            }

            next_id:
            for (int id = 0; id < engineConfiguration.getEngine().getCount(); id++) {
                for (Mapper mapper : mappers) {
                    if (!mapper.isPresent(id)) {
                        continue next_id;
                    }
                }
                ids.add(id);
            }
        }

        @Override
        public Stream<Chunk> chunks() {
            int size = ids.size();
            int units = size / chunkSize;
            int lastSize = size % chunkSize;

            return concat(
                    range(0, units).mapToObj(unit ->
                            new Chunk(ChunkInfo.of(unit, chunkSize), range(unit * chunkSize, unit * chunkSize + chunkSize).map(ids::getInt))),
                    lastSize > 0 ? of(new Chunk(ChunkInfo.of(units, lastSize), range(units * chunkSize, size).map(ids::getInt))) : empty());
        }
    }

}
