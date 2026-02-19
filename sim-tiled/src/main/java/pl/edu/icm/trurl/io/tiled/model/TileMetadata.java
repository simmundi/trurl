package pl.edu.icm.trurl.io.tiled.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMetadata implements Serializable {
    private final TilesetMetadata tilesetMetadata;
    private final int localId;
    private String type;
    private Map<String, String> properties;
    private List<AnimationFrameMetadata> frames = new ArrayList<>();
    private Integer representation;

    public Integer getRepresentation() {
        return representation;
    }

    public void setRepresentation(Integer representation) {
        this.representation = representation;
    }

    TileMetadata(TilesetMetadata tilesetMetadata, int localId) {
        this.tilesetMetadata = tilesetMetadata;
        this.localId = localId;
    }

    public TilesetMetadata getTilesetMetadata() {
        return tilesetMetadata;
    }

    public String getImagePath() {
        return tilesetMetadata.getImagePath();
    }

    public void putProperty(String name, String value) {
        this.getProperties().put(name, value);
    }

    public void addAnimationFrame(int localId, float frameDuration) {
        this.getFrames().add(new AnimationFrameMetadata(localId, frameDuration));
    }

    public List<AnimationFrameMetadata> getAnimationFrames() {
        return frames;
    }

    public int getTileWidth() {
        return tilesetMetadata.getTileWidth();
    }

    public int getTileHeight() {
        return tilesetMetadata.getTileHeight();
    }

    public int getLocalId() {
        return localId;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private List<AnimationFrameMetadata> getFrames() {
        if (frames == null) {
            frames = new ArrayList<>();
        }
        return frames;
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }
}
