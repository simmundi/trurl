package pl.edu.icm.trurl.io.tiled.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TilesetMetadata implements Serializable {
    private String imagePath;
    private int tileWidth;
    private int tileHeight;
    private int tileCount;
    private int columns;
    private List<TileMetadata> data = new ArrayList<>();

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
        while (tileCount < data.size()) {
            data.remove(data.size() - 1);
        }
        while (tileCount > data.size()) {
            data.add(new TileMetadata(this, data.size()));
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public TileMetadata getTileByLocalId(int id) {
        return data.get(id);
    }

    public int getTileCount() {
        return tileCount;
    }


    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }
}
