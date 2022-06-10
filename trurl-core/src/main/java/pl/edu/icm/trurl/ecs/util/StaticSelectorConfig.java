package pl.edu.icm.trurl.ecs.util;

public class StaticSelectorConfig {
    final Class[] components;
    final int initialSize;
    final int chunkSize;

    StaticSelectorConfig(Class[] components, int initialSize, int chunkSize) {
        this.components = components;
        this.initialSize = initialSize;
        this.chunkSize = chunkSize;
    }
}
