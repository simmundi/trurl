package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class MapperSet {
    private final ComponentAccessor componentAccessor;
    private final Mapper[] mappers;

    public MapperSet(ComponentAccessor componentAccessor) {
        this.componentAccessor = componentAccessor;
        mappers = new Mapper[componentAccessor.componentCount()];
        for (int idx = 0; idx < componentCount(); idx++) {
            mappers[idx] = Mappers.create(componentAccessor.indexToClass(idx));
        }
    }

    public <T> Mapper<T> classToMapper(Class<T> componentClass) {
        return mappers[componentAccessor.classToIndex(componentClass)];
    }

    public <T> Mapper<T> indexToMapper(int componentIndex) {
        return mappers[componentIndex];
    }

    public int classToIndex(Class<?> componentClass) {
        return componentAccessor.classToIndex(componentClass);
    }

    public int componentCount() {
        return componentAccessor.componentCount();
    }

    public Stream<Mapper<Object>> streamMappers() {
        return IntStream.range(0, componentCount())
                .mapToObj(this::indexToMapper);
    }
}
