package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.Selector;

import java.util.function.IntPredicate;

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

    private Engine getEngine() {
        return engine == null ? engineConfiguration.getEngine() : engine;
    }
}
