package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.Selector;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class Selectors {
    private final int DEFAULT_CHUNK_SIZE = 25_000;
    private final EngineConfiguration engineConfiguration;
    private final Engine engine;

    @WithFactory
    public Selectors(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
        this.engine = null;
    }

    public Selectors(Engine engine) {
        this.engineConfiguration = null;
        this.engine = engine;
    }

    public Selector allEntities() {
        return allEntities(DEFAULT_CHUNK_SIZE);
    }

    public Selector allEntities(int chunkSize) {
        return new RangeSelector(() -> 0, () -> getEngine().getCount(), chunkSize);
    }

    public Selector allWithComponents(Class<?>... components) {
        return filtered(allEntities(DEFAULT_CHUNK_SIZE), hasComponents(components));
    }

    public IntPredicate hasComponents(Class<?>... components) {
        Mapper[] mappers = new Mapper[components.length];

        for (int i = 0; i < components.length; i++) {
            mappers[i] = getEngine().getMapperSet().classToMapper(components[i]);
        }

        return (id) -> {
            for (Mapper mapper : mappers) {
                if (!mapper.isPresent(id)) {
                    return false;
                }
            }
            return true;
        };

    }

    public Selector filtered(Selector selector, IntPredicate predicate) {
        return () -> selector.chunks()
                .map(chunk -> new Chunk(chunk.getChunkInfo(), chunk.ids().
                        filter(predicate)));
    }

    public <M> Selector filtered(Selector selector, Class<M> mappedType, Predicate<M> predicate) {
        final Mapper<M> mMapper = getEngine().getMapperSet().classToMapper(mappedType);
        return () -> selector.chunks()
                .map(chunk -> new Chunk(
                        chunk.getChunkInfo(),
                        chunk.ids()
                                .filter(id -> mMapper.isPresent(id) && predicate.test(mMapper.createAndLoad(id)))));
    }


    /**
     * Filters selector with given predicates, allowing to choose whether accept ID of entity without a component on
     * which the predicate is given or not.
     * @param selector        Selector to be filtered.
     * @param mClass          Class on which predicate will be provided.
     * @param mPredicate      The predicate.
     * @param mAcceptIfAbsent If true - entity will pass the predicate even if mClass is not one of its components
     * @param kClass
     * @param kPredicate
     * @param kAcceptIfAbsent
     * @param <M>
     * @param <K>
     * @return Filtered Selector.
     */
    public <M, K> Selector filtered(Selector selector,
                                    Class<M> mClass, Predicate<M> mPredicate, boolean mAcceptIfAbsent,
                                    Class<K> kClass, Predicate<K> kPredicate, boolean kAcceptIfAbsent) {
        final Mapper<M> mMapper = getEngine().getMapperSet().classToMapper(mClass);
        final Mapper<K> kMapper = getEngine().getMapperSet().classToMapper(kClass);

        return () -> selector.chunks().map(chunk -> new Chunk(chunk.getChunkInfo(), chunk.ids().filter(i ->
                (mAcceptIfAbsent ? !mMapper.isPresent(i) || mPredicate.test(mMapper.createAndLoad(i)) :
                        mMapper.isPresent(i) && mPredicate.test(mMapper.createAndLoad(i))) &&
                (kAcceptIfAbsent ? !kMapper.isPresent(i) || kPredicate.test(kMapper.createAndLoad(i)) :
                        kMapper.isPresent(i) && kPredicate.test(kMapper.createAndLoad(i)))

        )));
    }

    /**
     * Filters selector with given predicates, allowing to choose whether accept ID of entity without a component on
     * which the predicate is given or not.
     * @param selector Selector to be filtered.
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
     * @return Filtered Selector.
     */
    public <M, K, L, W> Selector filtered(Selector selector,
                                          Class<M> mClass, Predicate<M> mPredicate, boolean mAcceptIfAbsent,
                                          Class<K> kClass, Predicate<K> kPredicate, boolean kAcceptIfAbsent,
                                          Class<L> lClass, Predicate<L> lPredicate, boolean lAcceptIfAbsent,
                                          Class<W> wClass, Predicate<W> wPredicate, boolean wAcceptIfAbsent) {
        final Mapper<M> mMapper = getEngine().getMapperSet().classToMapper(mClass);
        final Mapper<K> kMapper = getEngine().getMapperSet().classToMapper(kClass);
        final Mapper<L> lMapper = getEngine().getMapperSet().classToMapper(lClass);
        final Mapper<W> wMapper = getEngine().getMapperSet().classToMapper(wClass);
        return () -> selector.chunks().map(chunk -> new Chunk(chunk.getChunkInfo(), chunk.ids().filter(i ->
                (mAcceptIfAbsent ? !mMapper.isPresent(i) || mPredicate.test(mMapper.createAndLoad(i)) :
                        mMapper.isPresent(i) && mPredicate.test(mMapper.createAndLoad(i))) &&
                (kAcceptIfAbsent ? !kMapper.isPresent(i) || kPredicate.test(kMapper.createAndLoad(i)) :
                        kMapper.isPresent(i) && kPredicate.test(kMapper.createAndLoad(i))) &&
                (lAcceptIfAbsent ? !lMapper.isPresent(i) || lPredicate.test(lMapper.createAndLoad(i)) :
                        lMapper.isPresent(i) && lPredicate.test(lMapper.createAndLoad(i))) &&
                (wAcceptIfAbsent ? !wMapper.isPresent(i) || wPredicate.test(wMapper.createAndLoad(i)) :
                        wMapper.isPresent(i) && wPredicate.test(wMapper.createAndLoad(i)))

        )));
    }

    private Engine getEngine() {
        return engine == null ? engineConfiguration.getEngine() : engine;
    }
}
