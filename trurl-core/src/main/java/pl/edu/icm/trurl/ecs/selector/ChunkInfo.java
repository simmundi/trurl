package pl.edu.icm.trurl.ecs.selector;

public class ChunkInfo {

    private final int chunkId;
    private final int approximateSize;

    private ChunkInfo(int chunkId, int approximateSize) {
        this.chunkId = chunkId;
        this.approximateSize = approximateSize;
    }

    public static ChunkInfo of(int chunkId, int size) {
        return new ChunkInfo(chunkId, size);
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getApproximateSize() {
        return approximateSize;
    }

    @Override
    public String toString() {
        return "ChunkInfo{" +
                "chunkId=" + chunkId +
                ", approxSize=" + approximateSize +
                '}';
    }
}
