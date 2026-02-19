/*
 * Copyright (c) 2026 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.gdx.level;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import pl.edu.icm.trurl.world2d.level.EntityPrototype;
import pl.edu.icm.trurl.world2d.level.LevelSource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LevelSource that loads entities from a LibGDX TiledMap.
 */
public class GdxTmxLevelSource implements LevelSource {
    private final TiledMap tiledMap;

    public GdxTmxLevelSource(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
    }

    @Override
    public void forEach(Consumer<EntityPrototype> consumer) {
        int mapHeight = tiledMap.getProperties().get("height", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
        float worldHeight = mapHeight * tileHeight;

        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                for (int x = 0; x < tileLayer.getWidth(); x++) {
                    for (int y = 0; y < tileLayer.getHeight(); y++) {
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                        if (cell != null && cell.getTile() != null) {
                            consumer.accept(new GdxTilePrototype(cell.getTile(), 
                                x * tileLayer.getTileWidth(), 
                                y * tileLayer.getTileHeight(), 
                                tileLayer.getTileWidth(), 
                                tileLayer.getTileHeight()));
                        }
                    }
                }
            } else {
                for (MapObject object : layer.getObjects()) {
                    consumer.accept(new GdxObjectPrototype(object, worldHeight));
                }
            }
        }
    }

    private static class GdxTilePrototype implements EntityPrototype {
        private final TiledMapTile tile;
        private final float x, y, w, h;
        private Map<String, String> properties;

        public GdxTilePrototype(TiledMapTile tile, float x, float y, float w, float h) {
            this.tile = tile;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override public float x() { return x; }
        @Override public float y() { return y; }
        @Override public float width() { return w; }
        @Override public float height() { return h; }
        @Override public String type() { 
            Object value = tile.getProperties().get("type");
            return value != null ? String.valueOf(value) : null;
        }
        @Override public String sourceId() { return null; }
        @Override public int representation() { return tile.getId(); }
        @Override public String getProperty(String key) { 
            Object value = tile.getProperties().get(key);
            return value != null ? String.valueOf(value) : null;
        }
        @Override public Map<String, String> allProperties() {
            if (properties == null) {
                properties = new HashMap<>();
                tile.getProperties().getKeys().forEachRemaining(k -> properties.put(k, String.valueOf(tile.getProperties().get(k))));
            }
            return properties;
        }
    }

    private static class GdxObjectPrototype implements EntityPrototype {
        private final MapObject object;
        private final float worldHeight;
        private Map<String, String> properties;

        public GdxObjectPrototype(MapObject object, float worldHeight) {
            this.object = object;
            this.worldHeight = worldHeight;
        }

        @Override public float x() { return object.getProperties().get("x", Float.class); }
        @Override public float y() { 
            // LibGDX objects use Y-up by default, but Tiled TMX can be different.
            // However, LibGDX loader usually normalizes this.
            return object.getProperties().get("y", Float.class); 
        }
        @Override public float width() { return object.getProperties().get("width", 0f, Float.class); }
        @Override public float height() { return object.getProperties().get("height", 0f, Float.class); }
        @Override public String type() { 
            Object value = object.getProperties().get("type");
            return value != null ? String.valueOf(value) : null;
        }
        @Override public String sourceId() { 
            return String.valueOf(object.getProperties().get("id"));
        }
        @Override public int representation() { 
            if (object instanceof TiledMapTileMapObject) {
                return ((TiledMapTileMapObject) object).getTile().getId();
            }
            return -1; 
        }
        @Override public String getProperty(String key) { 
            if ("name".equals(key)) return object.getName();
            Object value = object.getProperties().get(key);
            if (value == null && "class".equals(key)) {
                // Support both "class" and "type" since Tiled 1.9 renamed it, and LibGDX might map it differently
                value = object.getProperties().get("type");
            }
            return value != null ? String.valueOf(value) : null;
        }
        @Override public Map<String, String> allProperties() {
            if (properties == null) {
                properties = new HashMap<>();
                object.getProperties().getKeys().forEachRemaining(k -> properties.put(k, String.valueOf(object.getProperties().get(k))));
                if (object.getName() != null) {
                    properties.put("name", object.getName());
                }
            }
            return properties;
        }
    }
}
