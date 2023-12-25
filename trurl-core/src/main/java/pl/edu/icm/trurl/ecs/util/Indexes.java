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

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.ecs.index.Chunk;
import pl.edu.icm.trurl.ecs.index.Index;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Indexes {
    private final int defaultChunkSize;
    private final EngineConfiguration engineConfiguration;
    private final Engine engine;

    @WithFactory
    public Indexes(EngineConfiguration engineConfiguration, @ByName(value = "trurl.engine.default-chunk-size", fallbackValue = "25000") int defaultChunkSize) {
        this.engineConfiguration = engineConfiguration;
        this.defaultChunkSize = defaultChunkSize;
        this.engine = null;
    }

    public Indexes(Engine engine, int defaultChunkSize) {
        this.engineConfiguration = null;
        this.engine = engine;
        this.defaultChunkSize = defaultChunkSize;
    }

    public Index allEntities() {
        return allEntities(defaultChunkSize);
    }

    public Index allEntities(int chunkSize) {
        return new RangeIndex(() -> 0, () -> getEngine().getCount(), chunkSize);
    }

    public Index allWithComponents(Class<?>... components) {
        return filtered(allEntities(defaultChunkSize), hasComponents(components));
    }

    public IntPredicate hasComponents(Class<?>... components) {
        Dao[] daos = new Dao[components.length];

        for (int i = 0; i < components.length; i++) {
            daos[i] = getEngine().getDaoManager().classToDao(components[i]);
        }

        return (id) -> {
            for (Dao dao : daos) {
                if (!dao.isPresent(id)) {
                    return false;
                }
            }
            return true;
        };

    }

    public Index filtered(Index index, IntPredicate predicate) {


        return new Index () {
            @Override
            public Stream<Chunk> chunks() {
                return index.chunks()
                        .map(chunk -> new Chunk(chunk.getChunkInfo(), chunk.ids().
                                filter(predicate)));
            }

            @Override
            public int estimatedChunkSize() {
                return index.estimatedChunkSize() / 2;
            }
        };
    }

    public <M> Index filtered(Index index, Class<M> mappedType, Predicate<M> predicate) {
        final Dao<M> mDao = getEngine().getDaoManager().classToDao(mappedType);
        return new Index() {
            @Override
            public Stream<Chunk> chunks() {
                return index.chunks()
                        .map(chunk -> new Chunk(
                                chunk.getChunkInfo(),
                                chunk.ids()
                                        .filter(id -> mDao.isPresent(id) && predicate.test(mDao.createAndLoad(id)))));
            }

            @Override
            public int estimatedChunkSize() {
                return index.estimatedChunkSize();
            }
        };
    }


    /**
     * Filters index with given predicates, allowing to choose whether accept ID of entity without a component on
     * which the predicate is given or not.
     * @param index        Index to be filtered.
     * @param mClass          Class on which predicate will be provided.
     * @param mPredicate      The predicate.
     * @param mAcceptIfAbsent If true - entity will pass the predicate even if mClass is not one of its components
     * @param kClass
     * @param kPredicate
     * @param kAcceptIfAbsent
     * @param <M>
     * @param <K>
     * @return Filtered Index.
     */
    public <M, K> Index filtered(Index index,
                                 Class<M> mClass, Predicate<M> mPredicate, boolean mAcceptIfAbsent,
                                 Class<K> kClass, Predicate<K> kPredicate, boolean kAcceptIfAbsent) {
        final Dao<M> mDao = getEngine().getDaoManager().classToDao(mClass);
        final Dao<K> kDao = getEngine().getDaoManager().classToDao(kClass);

        return new Index() {
            @Override
            public Stream<Chunk> chunks() {
                return index.chunks().map(chunk -> new Chunk(chunk.getChunkInfo(), chunk.ids().filter(i ->
                        (mAcceptIfAbsent ? !mDao.isPresent(i) || mPredicate.test(mDao.createAndLoad(i)) :
                                mDao.isPresent(i) && mPredicate.test(mDao.createAndLoad(i))) &&
                                (kAcceptIfAbsent ? !kDao.isPresent(i) || kPredicate.test(kDao.createAndLoad(i)) :
                                        kDao.isPresent(i) && kPredicate.test(kDao.createAndLoad(i)))

                )));
            }

            @Override
            public int estimatedChunkSize() {
                return index.estimatedChunkSize();
            }
        };
    }

    /**
     * Filters index with given predicates, allowing to choose whether accept ID of entity without a component on
     * which the predicate is given or not.
     * @param index Index to be filtered.
     * @param mClass Class on which predicate will be provided.
     * @param mPredicate The predicate.
     * @param mAcceptIfAbsent If true - entity will pass the predicate even if mClass is not one of its components
     * @param kClass
     * @param kPredicate
     * @param kAcceptIfAbsent
     * @param lClass
     * @param lPredicate
     * @param lAcceptIfAbsent
     * @param wClass
     * @param wPredicate
     * @param wAcceptIfAbsent
     * @param <M>
     * @param <K>
     * @param <L>
     * @param <W>
     * @return Filtered Index.
     */
    public <M, K, L, W> Index filtered(Index index,
                                       Class<M> mClass, Predicate<M> mPredicate, boolean mAcceptIfAbsent,
                                       Class<K> kClass, Predicate<K> kPredicate, boolean kAcceptIfAbsent,
                                       Class<L> lClass, Predicate<L> lPredicate, boolean lAcceptIfAbsent,
                                       Class<W> wClass, Predicate<W> wPredicate, boolean wAcceptIfAbsent) {
        final Dao<M> mDao = getEngine().getDaoManager().classToDao(mClass);
        final Dao<K> kDao = getEngine().getDaoManager().classToDao(kClass);
        final Dao<L> lDao = getEngine().getDaoManager().classToDao(lClass);
        final Dao<W> wDao = getEngine().getDaoManager().classToDao(wClass);
        return new Index() {
            @Override
            public Stream<Chunk> chunks() {
                return index.chunks().map(chunk -> new Chunk(chunk.getChunkInfo(), chunk.ids().filter(i ->
                        (mAcceptIfAbsent ? !mDao.isPresent(i) || mPredicate.test(mDao.createAndLoad(i)) :
                                mDao.isPresent(i) && mPredicate.test(mDao.createAndLoad(i))) &&
                                (kAcceptIfAbsent ? !kDao.isPresent(i) || kPredicate.test(kDao.createAndLoad(i)) :
                                        kDao.isPresent(i) && kPredicate.test(kDao.createAndLoad(i))) &&
                                (lAcceptIfAbsent ? !lDao.isPresent(i) || lPredicate.test(lDao.createAndLoad(i)) :
                                        lDao.isPresent(i) && lPredicate.test(lDao.createAndLoad(i))) &&
                                (wAcceptIfAbsent ? !wDao.isPresent(i) || wPredicate.test(wDao.createAndLoad(i)) :
                                        wDao.isPresent(i) && wPredicate.test(wDao.createAndLoad(i)))

                )));
            }

            @Override
            public int estimatedChunkSize() {
                return index.estimatedChunkSize();
            }
        };
    }

    private Engine getEngine() {
        return engine == null ? engineConfiguration.getEngine() : engine;
    }
}
