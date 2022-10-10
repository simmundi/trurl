package pl.edu.icm.trurl.ecs.selector;

public class ChunkInfo {

    private final int chunkId;
    private final int approximateSize;

    private final String label;

    private ChunkInfo(int chunkId, int approximateSize) {
        this.chunkId = chunkId;
        this.approximateSize = approximateSize;
        label = "";
    }

    public ChunkInfo(int chunkId, int approximateSize, String label) {
        this.chunkId = chunkId;
        this.approximateSize = approximateSize;
        this.label = label;
    }

    public static ChunkInfo of(int chunkId, int size) {
        return new ChunkInfo(chunkId, size);
    }

    public static ChunkInfo of(int chunkId, int size, String label) {
        return new ChunkInfo(chunkId, size, label);
    }

    public int getChunkId() {
        return chunkId;
    }

    public int getApproximateSize() {
        return approximateSize;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "ChunkInfo{" +
                "label=" + label +
                ", chunkId=" + chunkId +
                ", approxSize=" + approximateSize +
                '}';
    }
}
