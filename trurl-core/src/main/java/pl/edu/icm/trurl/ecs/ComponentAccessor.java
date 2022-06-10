package pl.edu.icm.trurl.ecs;

public interface ComponentAccessor {
    int classToIndex(Class<?> componentClass);
    Class<?> indexToClass(int index);
    int componentCount();
}
