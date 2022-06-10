package pl.edu.icm.trurl.ecs.selector;

import java.util.stream.IntStream;

public class Chunk {
    private final ChunkInfo chunkInfo;
    private final IntStream ids;

    public Chunk(ChunkInfo chunkInfo, IntStream ids) {
        this.chunkInfo = chunkInfo;
        this.ids = ids;
    }

    public ChunkInfo getChunkInfo() {
        return chunkInfo;
    }

    public IntStream ids() {
        return ids;
    }
}
