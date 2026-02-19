package pl.edu.icm.trurl.io.tiled.model;

import java.io.Serializable;

public class AnimationFrameMetadata implements Serializable {
    private final int localId;
    private final float duration;

    public AnimationFrameMetadata(int localId, float duration) {
        this.localId = localId;
        this.duration = duration;
    }

    public int getLocalId() {
        return localId;
    }

    public float getDuration() {
        return duration;
    }

}
