package pl.edu.icm.trurl.ecs.mapper;

import pl.edu.icm.trurl.store.Store;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Mappers {

    static public <T> Mapper<T> create(Class<T> clazz) {
        try {
            return (Mapper<T>)
                    Class.forName(clazz.getCanonicalName() + "Mapper")
                            .getDeclaredConstructor()
                            .newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException cause) {
            throw new IllegalArgumentException("Class " + clazz + " does not have a valid mapper (did you forget the @WithMapper annotation? is trurl-generator configured as an annotation processor?)", cause);
        }
    }

    static public <T> Mapper<T> createAndAttach(Class<T> clazz, Store store) {
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