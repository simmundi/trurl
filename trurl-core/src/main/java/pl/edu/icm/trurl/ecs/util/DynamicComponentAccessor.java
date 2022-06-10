package pl.edu.icm.trurl.ecs.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import pl.edu.icm.trurl.ecs.ComponentAccessor;

import java.util.Arrays;

public class DynamicComponentAccessor implements ComponentAccessor {
    private final Class[] componentClasses;
    private final Object2IntMap<Class> componentMap;

    public DynamicComponentAccessor(Class... componentClasses) {
        this.componentClasses = Arrays.copyOf(componentClasses, componentClasses.length);
        componentMap = new Object2IntOpenHashMap<>(componentClasses.length);
        for (int i = 0; i < componentClasses.length; i++) {
            componentMap.put(componentClasses[i], i);
        }
    }

    @Override
    public int classToIndex(Class<?> componentClass) {
        return componentMap.getInt(componentClass);
    }

    @Override
    public Class<?> indexToClass(int index) {
        return componentClasses[index];
    }

    @Override
    public int componentCount() {
        return componentClasses.length;
    }
}
