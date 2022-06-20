package pl.edu.icm.trurl.ecs.mapper;

import java.util.concurrent.CopyOnWriteArrayList;

public final class MapperListeners<T> {
    private CopyOnWriteArrayList<MapperListener<T>> listeners = new CopyOnWriteArrayList();

    public void fireSavingComponent(T component, int row) {
        if (!listeners.isEmpty()) {
            for (MapperListener<T> listener : listeners) {
                listener.savingComponent(row, component);
            }
        }
    }

    public void addSavingListener(MapperListener<T> mapperListener) {
        listeners.add(mapperListener);
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }
}
