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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import pl.edu.icm.trurl.gdx.GdxTileTextureLoader;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.level.EntityPrototype;
import pl.edu.icm.trurl.world2d.level.LevelSource;
import pl.edu.icm.trurl.world2d.model.Named;
import pl.edu.icm.trurl.world2d.model.display.AnimationComponent;
import pl.edu.icm.trurl.world2d.model.display.AnimationFrame;
import pl.edu.icm.trurl.world2d.model.display.TextureComponent;
import pl.edu.icm.trurl.world2d.model.display.TextureRegionComponent;
 
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LevelSource that loads entities from a LibGDX TiledMap.
 */
public class GdxTmxLevelSource implements LevelSource {
    private final TiledMap tiledMap;
    private final GdxTileTextureLoader textureLoader;
    private final Map<Integer, Integer> tileToRepresentation = new HashMap<>();

    public GdxTmxLevelSource(TiledMap tiledMap) {
        this(tiledMap, null);
    }

    public GdxTmxLevelSource(TiledMap tiledMap, GdxTileTextureLoader textureLoader) {
        this.tiledMap = tiledMap;
        this.textureLoader = textureLoader;
    }

    @Override
    public void prepare(Session session) {
        Map<Texture, Entity> textureToEntity = new HashMap<>();

        // First pass: create base representation (TextureRegion) for ALL tiles
        for (TiledMapTileSet tileSet : tiledMap.getTileSets()) {
            tileSet.forEach(tile -> {
                Entity rep = createTileRepresentation(session, tile, textureToEntity);
                tileToRepresentation.put(tile.getId(), rep.getId());
            });
        }

        // Second pass: add AnimationComponent to animated tiles
        for (TiledMapTileSet tileSet : tiledMap.getTileSets()) {
            tileSet.forEach(tile -> {
                if (tile instanceof AnimatedTiledMapTile) {
                    addAnimatedTileDetails(session, (AnimatedTiledMapTile) tile);
                }
            });
        }
    }

    private Entity createTileRepresentation(Session session, TiledMapTile tile, Map<Texture, Entity> textureToEntity) {
        if (tileToRepresentation.containsKey(tile.getId())) {
            return session.getEntity(tileToRepresentation.get(tile.getId()));
        }

        TextureRegion region = tile.getTextureRegion();
        Texture texture = region.getTexture();

        Entity textureEntity = textureToEntity.computeIfAbsent(texture, t -> {
            String texturePath = tile.getProperties().get("imagePath", String.class);
            if (texturePath == null) {
                texturePath = "texture_" + System.identityHashCode(t);
            }

            if (textureLoader != null) {
                textureLoader.registerTexture(texturePath, t);
            }

            Entity entity = session.createEntity();
            entity.getOrCreate(TextureComponent.class).setTexturePath(texturePath);
            entity.getOrCreate(Named.class).setName(texturePath);
            return entity;
        });

        Entity regionEntity = session.createEntity();
        TextureRegionComponent textureRegionComponent = regionEntity.getOrCreate(TextureRegionComponent.class);
        textureRegionComponent.setTexture(textureEntity);
        textureRegionComponent.setX((short) region.getRegionX());
        textureRegionComponent.setY((short) region.getRegionY());
        textureRegionComponent.setWidth((short) region.getRegionWidth());
        textureRegionComponent.setHeight((short) region.getRegionHeight());

        if (textureLoader != null) {
            textureLoader.registerRegion(regionEntity, region);
        }

        String name = tile.getProperties().get("name", String.class);
        if (name != null) {
            regionEntity.getOrCreate(Named.class).setName(name);
        }

        tileToRepresentation.put(tile.getId(), regionEntity.getId());
        return regionEntity;
    }

    private void addAnimatedTileDetails(Session session, AnimatedTiledMapTile tile) {
        Integer representationId = tileToRepresentation.get(tile.getId());
        if (representationId == null) {
            return;
        }

        Entity animationEntity = session.getEntity(representationId);
        AnimationComponent animationComponent = animationEntity.getOrCreate(AnimationComponent.class);

        StaticTiledMapTile[] frameTiles = tile.getFrameTiles();
        int[] intervals = tile.getAnimationIntervals();
        for (int i = 0; i < frameTiles.length; i++) {
            StaticTiledMapTile frameTile = frameTiles[i];
            Integer frameRepresentationId = tileToRepresentation.get(frameTile.getId());
            if (frameRepresentationId == null) {
                continue;
            }
            Entity frameEntity = session.getEntity(frameRepresentationId);
            animationComponent.getFrames().add(AnimationFrame.of(frameEntity, intervals[i] / 1000f));
        }
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
                                tileLayer.getTileHeight(),
                                tileToRepresentation.getOrDefault(cell.getTile().getId(), Entity.NULL_ID)));
                        }
                    }
                }
            } else {
                for (MapObject object : layer.getObjects()) {
                    int representationId = Entity.NULL_ID;
                    if (object instanceof TiledMapTileMapObject) {
                        TiledMapTile tile = ((TiledMapTileMapObject) object).getTile();
                        representationId = tileToRepresentation.getOrDefault(tile.getId(), Entity.NULL_ID);
                    }
                    consumer.accept(new GdxObjectPrototype(object, worldHeight, representationId));
                }
            }
        }
    }
 
    private static class GdxTilePrototype implements EntityPrototype {
        private final TiledMapTile tile;
        private final float x, y, w, h;
        private final int representation;
        private Map<String, String> properties;
 
        public GdxTilePrototype(TiledMapTile tile, float x, float y, float w, float h, int representation) {
            this.tile = tile;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.representation = representation;
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
        @Override public int representation() { return representation; }
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
        private final int representation;
        private Map<String, String> properties;
 
        public GdxObjectPrototype(MapObject object, float worldHeight, int representation) {
            this.object = object;
            this.worldHeight = worldHeight;
            this.representation = representation;
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
            return representation; 
        }
        @Override public String getProperty(String key) { 
            if ("name".equals(key)) return object.getName();
            Object value = object.getProperties().get(key);
            if (value == null && "class".equals(key)) {
                // Support both "class" and "type" since Tiled 1.9 renamed it
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
