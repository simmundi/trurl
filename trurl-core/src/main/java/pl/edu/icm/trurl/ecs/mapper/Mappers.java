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

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Mappers {

    private final Bento bento;

    @WithFactory
    public Mappers(Bento bento) {
        this.bento = bento;
    }

    public Mappers() {
        this.bento = Bento.createRoot();
    }

    public <T> Mapper<T> create(Class<T> clazz) {
        return create(clazz, "");
    }


    public <T> Mapper<T> create(Class<T> clazz, String mapperPrefix) {
        try {
            Bento child = bento.create();
            child.register("mapperPrefix", mapperPrefix);
            return child.get(
                    Class.forName(clazz.getPackage().getName() + ".MapperOf" + clazz.getSimpleName() + "Factory")
                            .getField("IT")
                            .get(null));
        } catch (ReflectiveOperationException cause) {
            throw new IllegalArgumentException("Class " + clazz + " does not have a valid mapper (did you forget the @WithMapper annotation? is trurl-generator configured as an annotation processor?)", cause);
        }
    }


    static public List<Attribute> gatherAttributes(Collection<Mapper<?>> mappers) {
        return mappers.stream()
                .flatMap(mapper -> Stream.concat(Stream.of(mapper), mapper.getChildMappers().stream()))
                .flatMap(mapper -> (Stream<Attribute>) mapper.attributes().stream())
                .collect(Collectors.toList());
    }

    public <T> Mapper<T> createAndAttach(Class<T> clazz, Store store) {
        Mapper<T> tMapper = create(clazz);
        tMapper.configureStore(store);
        tMapper.attachStore(store);
        return tMapper;
    }

    static public <T> Stream<T> stream(Mapper<T> mapper) {
        return IntStream.range(0, mapper.getCount())
                .filter(row -> mapper.isPresent(row))
                .mapToObj(row -> {
                    T t = mapper.create();
                    mapper.load(null, t, row);
                    return t;
                });
    }
}
