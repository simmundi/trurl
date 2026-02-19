package pl.edu.icm.trurl.io.tiled;

import pl.edu.icm.trurl.io.tiled.model.TileMetadata;
import pl.edu.icm.trurl.io.tiled.model.TilesetMetadata;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.*;
import java.util.function.Consumer;

public class TiledSingleMap {

    private final List<TilesetWithFirstGid> tilesets = new ArrayList<>();
    private final Map<String, String> objectsToEntities = new HashMap<>();
    private final List<Consumer<Map<String, String>>> entityReferenceWriters = new ArrayList<>();
    private final TiledCartographer tiledCartographer;

    public TiledSingleMap(TiledCartographer tiledCartographer) {
        this.tiledCartographer = tiledCartographer;
    }

    public void addTileset(int firstGid, String source) {
        tilesets.add(new TilesetWithFirstGid(tiledCartographer.get(source), firstGid));
    }

    public void addTile(int x, int y, int gid) {
        TileMetadata tile = getTile(gid);
        if (tile == null) {
            return;
        }
        Map<String, String> data = new HashMap<>();
        if (tile.getType() != null) {
            data.put("type", tile.getType());
        }
        data.put("x", Float.toString(x));
        data.put("y", Float.toString(y));
        data.put("width", Float.toString(tile.getTileWidth()));
        data.put("height", Float.toString(tile.getTileHeight()));
        data.put("centerX", Float.toString(x + tile.getTileWidth() / 2));
        data.put("centerY", Float.toString(y + tile.getTileHeight() / 2));
        data.put("representation", Integer.toString(tile.getRepresentation()));
        data.putAll(tile.getProperties());
        saveToStore(data, Collections.emptySet());
    }

    private int saveToStore(Map<String, String> data, Set<String> entityAttributes) {
        Store store = tiledCartographer.getStore();
        int entityId = store.getCounter().next();
        for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
            String key = stringStringEntry.getKey();
            String value = stringStringEntry.getValue();
            Attribute attribute = store.get(key);
            if (attribute != null) {
                if (!entityAttributes.contains(key)) {
                    attribute.setString(entityId, value);
                } else {
                    if (!"0".equals(value)) {
                        entityReferenceWriters.add(mappings -> {
                            attribute.setString(entityId, mappings.get(value));
                        });
                    }
                }
            }

        }
        return entityId;
    }

    public void addObject(int x, int y, String id, int gid, int width, int height, Shape shape, Map<String, String> properties, Set<String> objectProperties) {
        TileMetadata tile = getTile(gid);
        Map<String, String> data = new HashMap<>();
        if (tile != null) {
            data.putAll(tile.getProperties());
            data.remove("name"); // name is not inherited, because it is used as an id of the tile
            for (String objectProperty : objectProperties) {
                data.remove(objectProperty);
            }
        }
        data.putAll(properties);
        if (tile != null) {
            data.put("representation", Integer.toString(tile.getRepresentation()));
            if (tile.getType() != null) {
                data.put("type", tile.getType());
            }
        }
        data.putIfAbsent("x", Float.toString(x));
        data.putIfAbsent("y", Float.toString(y));
        data.putIfAbsent("width", Float.toString(width));
        data.putIfAbsent("height", Float.toString(height));
        data.putIfAbsent("shape", shape.toString());
        data.putIfAbsent("centerX", Float.toString(x + width / 2));
        data.putIfAbsent("centerY", Float.toString(y + height / 2));
        int entityId = saveToStore(data, objectProperties);
        objectsToEntities.put(id, Integer.toString(entityId));
    }

    public void connectEntities() {
        for (Consumer<Map<String, String>> entityReferenceWriter : entityReferenceWriters) {
            entityReferenceWriter.accept(objectsToEntities);
        }
    }

    public enum Shape {
        TILE, RECTANGLE, ELLIPSE, POLYGON
    }

    private TileMetadata getTile(int gid) {
        for (TilesetWithFirstGid tileset : tilesets) {
            if (tileset.firstGid <= gid && gid < tileset.firstGid + tileset.tileset.getTileCount()) {
                return tileset.tileset.getTileByLocalId(gid - tileset.firstGid);
            }
        }
        return null;
    }

    private class TilesetWithFirstGid {
        public final TilesetMetadata tileset;
        public final int firstGid;

        public TilesetWithFirstGid(TilesetMetadata tileset, int firstGid) {
            this.tileset = tileset;
            this.firstGid = firstGid;
        }
    }
}
