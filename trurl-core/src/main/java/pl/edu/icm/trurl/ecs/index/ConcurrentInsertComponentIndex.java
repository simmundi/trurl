package pl.edu.icm.trurl.ecs.index;

import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.ArraySelector;
import pl.edu.icm.trurl.util.ConcurrentInsertIntArray;

import java.util.stream.Stream;

public abstract class ConcurrentInsertComponentIndex<T> extends ComponentIndex<T> implements Selector {

    private final ConcurrentInsertIntArray ids;
    private final int chunkSize;
    private final int initialSize;

    public ConcurrentInsertComponentIndex(EngineConfiguration engineConfiguration, Class<T> clazz, int chunkSize, int initialSize) {
        this(engineConfiguration, clazz, chunkSize, initialSize, ConcurrentInsertIntArray.defaultMaxSize);
    }

    public ConcurrentInsertComponentIndex(EngineConfiguration engineConfiguration, Class<T> clazz, int chunkSize, int initialSize, int maximumTableSize) {
        super(engineConfiguration, clazz);
        this.chunkSize = chunkSize;
        this.initialSize = initialSize;
        ids = new ConcurrentInsertIntArray(maximumTableSize);
    }

    public boolean test(T component, int id) {
        return component != null;
    }

    public final boolean contains(int id) {
        return ids.contains(id);
    }

    @Override
    public final void savingComponent(int id, T newValue) {
        //we don't care if id has already been in index
        if (test(newValue, id)) {
            ids.add(id);
        }
    }

    @Override
    public Stream<Chunk> chunks() {
        return selector().chunks();
    }

    private Selector selector() {
        ArraySelector arraySelector = new ArraySelector(initialSize, chunkSize);
        ids.stream().forEach(arraySelector::add);
        return arraySelector;
    }

}
