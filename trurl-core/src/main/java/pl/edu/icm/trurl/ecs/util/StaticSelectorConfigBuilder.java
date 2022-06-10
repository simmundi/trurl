package pl.edu.icm.trurl.ecs.util;

public class StaticSelectorConfigBuilder {
    private Class[] components;
    private int initialSize;
    private int chunkSize;



    public StaticSelectorConfigBuilder withChunkSize(int unitSize) {
        this.chunkSize = unitSize;
        return this;
    }

    public StaticSelectorConfigBuilder withMandatoryComponents(Class... components) {
        this.components = components;
        return this;
    }

    public StaticSelectorConfigBuilder withInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    public StaticSelectorConfig build() {
        return new StaticSelectorConfig(components, initialSize, chunkSize);
    }
}
