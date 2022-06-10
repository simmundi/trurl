package pl.edu.icm.trurl.ecs.selector;

import java.util.stream.Stream;

public interface Selector {
    Stream<Chunk> chunks();
    default int estimatedChunkSize() {
        return 15_000;
    };
}
