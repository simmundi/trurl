package pl.edu.icm.trurl.gdx.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.model.DaoOfNamedFactory;
import pl.edu.icm.trurl.world2d.model.Named;
import pl.edu.icm.trurl.world2d.model.display.*;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.world2d.model.space.DaoOfBoundingBoxFactory;

import java.util.*;

public class TextureEntitiesService {

    private final EngineBuilder engineBuilder;
    private boolean autoFlush = true;

    @WithFactory
    public TextureEntitiesService(EngineBuilder engineBuilder) {
        engineBuilder.addComponentWithDao(TextureComponent.class, DaoOfTextureComponentFactory.IT);
        engineBuilder.addComponentWithDao(Named.class, DaoOfNamedFactory.IT);
        engineBuilder.addComponentWithDao(AnimationComponent.class, DaoOfAnimationComponentFactory.IT);
        engineBuilder.addComponentWithDao(TextureRegionComponent.class, DaoOfTextureRegionComponentFactory.IT);
        engineBuilder.addComponentWithDao(Displayable.class, DaoOfDisplayableFactory.IT);
        engineBuilder.addComponentWithDao(BoundingBox.class, DaoOfBoundingBoxFactory.IT);
        this.engineBuilder = engineBuilder;
    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    private Session session() {
        return engineBuilder.getEngine().getSession();
    }

    private void maybeFlush() {
        if (autoFlush) {
            session().flush();
        }
    }

    public Entity createTextureEntity(String path) {
        return createTextureEntity(path, path);
    }

    public Entity createTextureEntity(String path, String name) {
        Entity entity = session().createEntity();
        entity.getOrCreate(TextureComponent.class).setTexturePath(path);
        entity.getOrCreate(Named.class).setName(name);
        maybeFlush();
        return entity;
    }

    public Entity createFullTextureRegion(String path, String name, int width, int height) {
        Entity textureEntity = createTextureEntity(path, name + "_texture");
        Entity regionEntity = session().createEntity();
        TextureRegionComponent region = regionEntity.getOrCreate(TextureRegionComponent.class);
        region.setTexture(textureEntity);
        region.setX((short) 0);
        region.setY((short) 0);
        region.setWidth((short) width);
        region.setHeight((short) height);
        regionEntity.getOrCreate(Named.class).setName(name);
        maybeFlush();
        return regionEntity;
    }

    public List<Entity> createGridTextureRegions(String path, int columns, int rows, int tileWidth, int tileHeight, String prefix) {
        Entity textureEntity = createTextureEntity(path, prefix + "_texture");
        List<Entity> regions = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Entity regionEntity = session().createEntity();
                TextureRegionComponent region = regionEntity.getOrCreate(TextureRegionComponent.class);
                region.setTexture(textureEntity);
                region.setX((short) (c * tileWidth));
                region.setY((short) (r * tileHeight));
                region.setWidth((short) tileWidth);
                region.setHeight((short) tileHeight);
                regionEntity.getOrCreate(Named.class).setName(prefix + "_" + r + "_" + c);
                regions.add(regionEntity);
            }
        }
        maybeFlush();
        return regions;
    }

    public void createTexturesFromAtlas(String atlasPath) {
        FileHandle file = Gdx.files.internal(atlasPath);

        TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(file, file.parent(), false);
        Map<String, Entity> textures = new HashMap<>();
        Map<String, List<TextureAtlas.TextureAtlasData.Region>> frames = new HashMap<>();

        data.getPages().forEach(page -> {
            String path = page.textureFile.path();
            Entity textureEntity = createTextureEntity(path);
            textures.put(path, textureEntity);
        });

        data.getRegions().forEach(region -> {
            frames.computeIfAbsent(region.name, k -> new ArrayList<>()).add(region);
        });

        frames.forEach((name, regions) -> {
            if (regions.size() == 1) {
                TextureAtlas.TextureAtlasData.Region region = regions.get(0);
                createTextureRegionEntity(name, textures, region);
            } else {
                regions.sort(Comparator.comparing(r -> r.index));
                Entity animationEntity = session().createEntity();
                AnimationComponent animationComponent = animationEntity.getOrCreate(AnimationComponent.class);
                animationEntity.getOrCreate(Named.class).setName(name);

                regions.forEach(region -> {
                    Entity entity = createTextureRegionEntity(name + "_" + region.index, textures, region);
                    animationComponent.getFrames().add(AnimationFrame.of(entity, 0.2f));
                });
            }
        });
        maybeFlush();
    }

    private Entity createTextureRegionEntity(String name, Map<String, Entity> textures, TextureAtlas.TextureAtlasData.Region region) {
        Entity entity = session().createEntity();
        TextureRegionComponent textureRegionComponent = entity.getOrCreate(TextureRegionComponent.class);
        textureRegionComponent.setTexture(textures.get(region.page.textureFile.path()));
        textureRegionComponent.setX((short) region.left);
        textureRegionComponent.setY((short) region.top);
        textureRegionComponent.setWidth((short) region.width);
        textureRegionComponent.setHeight((short) region.height);
        entity.getOrCreate(Named.class).setName(name);
        return entity;
    }

    public void loadTmxTileset(String tmxPath) {
        TmxMapLoader loader = new TmxMapLoader();
        TiledMap map = loader.load(tmxPath);
        Map<String, Entity> textures = new HashMap<>();
        Map<TiledMapTile, Entity> tilesToEntities = new HashMap<>();

        map.getTileSets().forEach(tileSet -> {
            tileSet.forEach(tile -> {
                if (tile instanceof StaticTiledMapTile) {
                    StaticTiledMapTile staticTile = (StaticTiledMapTile) tile;
                    String name = tile.getProperties().get("name", String.class);
                    if (name != null) {
                        Entity entity = createEntityFromTiledTile(tile, name, textures);
                        tilesToEntities.put(tile, entity);
                    }
                }
            });

            // Second pass for animations
            tileSet.forEach(tile -> {
                if (tile instanceof AnimatedTiledMapTile) {
                    AnimatedTiledMapTile animatedTile = (AnimatedTiledMapTile) tile;
                    String name = tile.getProperties().get("name", String.class);
                    if (name != null) {
                        Entity animationEntity = session().createEntity();
                        animationEntity.getOrCreate(Named.class).setName(name);
                        AnimationComponent animationComponent = animationEntity.getOrCreate(AnimationComponent.class);
                        StaticTiledMapTile[] frameTiles = animatedTile.getFrameTiles();
                        int[] intervals = animatedTile.getAnimationIntervals();
                        for (int i = 0; i < frameTiles.length; i++) {
                            StaticTiledMapTile frameTile = frameTiles[i];
                            Entity frameEntity = tilesToEntities.get(frameTile);
                            if (frameEntity == null) {
                                frameEntity = createEntityFromTiledTile(frameTile, name + "_frame_" + frameTile.getId(), textures);
                                tilesToEntities.put(frameTile, frameEntity);
                            }
                            animationComponent.getFrames().add(AnimationFrame.of(frameEntity, intervals[i] / 1000f));
                        }
                    }
                }
            });
        });
        maybeFlush();
    }

    private Entity createEntityFromTiledTile(TiledMapTile tile, String name, Map<String, Entity> textures) {
        TextureRegion region = tile.getTextureRegion();
        String texturePath = "unknown";
        if (tile.getProperties().containsKey("imagePath")) {
            texturePath = tile.getProperties().get("imagePath", String.class);
        } else if (tile instanceof StaticTiledMapTile) {
            // LibGDX TmxMapLoader doesn't store the source path in the texture region easily.
            // But sometimes it's in the properties if we put it there or if we use a custom loader.
            // For now, let's try to get it from the texture's user data or similar if available,
            // but standard LibGDX doesn't do it.
        }

        Entity textureEntity = textures.computeIfAbsent(texturePath, this::createTextureEntity);
        Entity regionEntity = session().createEntity();
        TextureRegionComponent textureRegionComponent = regionEntity.getOrCreate(TextureRegionComponent.class);
        textureRegionComponent.setTexture(textureEntity);
        textureRegionComponent.setX((short) region.getRegionX());
        textureRegionComponent.setY((short) region.getRegionY());
        textureRegionComponent.setWidth((short) region.getRegionWidth());
        textureRegionComponent.setHeight((short) region.getRegionHeight());
        regionEntity.getOrCreate(Named.class).setName(name);

        // Copy other properties
        tile.getProperties().getKeys().forEachRemaining(key -> {
            if (!"name".equals(key) && !"imagePath".equals(key)) {
                Object value = tile.getProperties().get(key);
                // We could potentially store these properties in some component if we have one for generic properties
            }
        });

        maybeFlush();
        return regionEntity;
    }

    // TODO:
    //  - create textureRegions
    //  - create entities from atlases
    //  - create animations (frames) from atlases (or from slices as well...?)
    //  - create TextureRegion entities from slices (prefixes)
}
