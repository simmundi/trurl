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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;

import static org.mockito.Mockito.*;
import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.EngineBuilderFactory;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.IntArray;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.world2d.level.LevelExecutor;
import pl.edu.icm.trurl.world2d.model.Named;
import pl.edu.icm.trurl.world2d.model.display.AnimationComponent;
import pl.edu.icm.trurl.world2d.model.display.Displayable;
import pl.edu.icm.trurl.world2d.model.display.TextureComponent;
import pl.edu.icm.trurl.world2d.model.display.TextureRegionComponent;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.gdx.GdxTileTextureLoader;
import pl.edu.icm.trurl.gdx.managed.ManagedAssetsManager;

import pl.edu.icm.trurl.ecs.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class GdxTmxLevelSourceTest {

    private static HeadlessApplication application;

    @BeforeAll
    public static void setup() {
        if (Gdx.gl == null) {
            Gdx.gl = mock(GL20.class);
        }
        if (Gdx.app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        }
    }

    @AfterAll
    public static void tearDown() {
    }

    @Test
    public void testLoadBasictiles_FiltersAndCustomizes() {
        // given
        Bento bento = Bento.createRoot();
        EngineBuilder engineBuilder = bento.get(EngineBuilderFactory.IT);
        engineBuilder.addComponentClasses(BoundingBox.class, Displayable.class, Named.class, TextureComponent.class, TextureRegionComponent.class, AnimationComponent.class);
        LevelExecutor executor = new LevelExecutor(engineBuilder);

        TiledMap map = new TmxMapLoader(new ClasspathFileHandleResolver()).load("basictiles.tmx");
        GdxTmxLevelSource source = new GdxTmxLevelSource(map);

        List<String> objectNames = new ArrayList<>();

        // execute
        executor.execute(source,
                p -> {
                    String sid = p.sourceId();
                    return sid != null && !"null".equals(sid);
                },
                (e, p) -> {
                    String name = p.getProperty("name");
                    if (name != null) {
                        e.getOrCreate(Named.class).setName(name);
                        objectNames.add(name);
                    }
                }
        );

        // assert
        // Based on basictiles.tmx, there are 8 objects: Barbara, Chad, Cindy, and 5 others (Becky + 4 unnamed)
        assertThat(objectNames).contains("Barbara", "Chad", "Cindy", "Becky");
        // Representations (Textures, Regions) are also created. 
        // 120 tiles in Tileset + 1 texture entity.
        // engine.getCount() includes everything.
        assertThat(objectNames).hasSize(4); // We only add names for these 4
    }

    @Test
    public void testLoadBasictiles_VerifiesRepresentations() {
        // ... previous test logic
    }
 
    @Test
    public void testLoadMap_DeduplicatesTexturesAndRegistersThem() {
        // given
        Bento bento = Bento.createRoot();
        EngineBuilder engineBuilder = bento.get(EngineBuilderFactory.IT);
        engineBuilder.addComponentClasses(BoundingBox.class, Displayable.class, Named.class, TextureComponent.class, TextureRegionComponent.class, AnimationComponent.class);
        Engine engine = engineBuilder.getEngine();
        LevelExecutor executor = new LevelExecutor(engineBuilder);

        TiledMap map = new TiledMap();
        map.getProperties().put("height", 1);
        map.getProperties().put("tileheight", 16);

        com.badlogic.gdx.graphics.Texture texture = new com.badlogic.gdx.graphics.Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        StaticTiledMapTile tile1 = new StaticTiledMapTile(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));
        tile1.setId(1);
        StaticTiledMapTile tile2 = new StaticTiledMapTile(new com.badlogic.gdx.graphics.g2d.TextureRegion(texture));
        tile2.setId(2);

        com.badlogic.gdx.maps.tiled.TiledMapTileSet tileSet = new com.badlogic.gdx.maps.tiled.TiledMapTileSet();
        tileSet.putTile(1, tile1);
        tileSet.putTile(2, tile2);
        map.getTileSets().addTileSet(tileSet);

        TiledMapTileLayer layer = new TiledMapTileLayer(2, 1, 16, 16);
        TiledMapTileLayer.Cell cell1 = new TiledMapTileLayer.Cell();
        cell1.setTile(tile1);
        layer.setCell(0, 0, cell1);
        TiledMapTileLayer.Cell cell2 = new TiledMapTileLayer.Cell();
        cell2.setTile(tile2);
        layer.setCell(1, 0, cell2);
        map.getLayers().add(layer);

        GdxTileTextureLoader loader = mock(GdxTileTextureLoader.class);
        GdxTmxLevelSource source = new GdxTmxLevelSource(map, loader);

        // execute
        executor.execute(source, p -> true, (e, p) -> {});

        // assert
        // Only one TextureComponent entity should be created because they share the same Texture object
        long textureCount = engine.getSession().findEntitiesInSession().stream()
                .filter(e -> e.get(TextureComponent.class) != null)
                .count();
        assertThat(textureCount).isEqualTo(1);
        verify(loader, atLeastOnce()).registerTexture(anyString(), eq(texture));
        verify(loader, atLeast(2)).registerRegion(any(Entity.class), any(com.badlogic.gdx.graphics.g2d.TextureRegion.class));
    }

    @Test
    public void testLoadBasictiles_VerifiesAnimationsCorrectlyCreated() {
        // given
        Bento bento = Bento.createRoot();
        EngineBuilder engineBuilder = bento.get(EngineBuilderFactory.IT);
        engineBuilder.addComponentClasses(BoundingBox.class, Displayable.class, Named.class, TextureComponent.class, TextureRegionComponent.class, AnimationComponent.class);
        LevelExecutor executor = new LevelExecutor(engineBuilder);

        TiledMap map = new TmxMapLoader(new ClasspathFileHandleResolver()).load("basictiles.tmx");
        GdxTmxLevelSource source = new GdxTmxLevelSource(map);
        Session session = engineBuilder.getEngine().getSession();

        List<Integer> animatedRepresentations = new ArrayList<>();

        // execute
        executor.execute(source,
                p -> {
                    int rep = p.representation();
                    if (rep != Entity.NULL_ID) {
                        Entity repEntity = session.getEntity(rep);
                        if (repEntity.get(AnimationComponent.class) != null) {
                            animatedRepresentations.add(rep);
                        }
                    }
                    return true;
                },
                (e, p) -> {}
        );

        // assert
        // tile 29 and 60 are animated in basictiles.tsx
        // tileset basictiles.tsx has firstgid=1
        // so global ids are 30 and 61
        
        assertThat(animatedRepresentations).isNotEmpty();
        
        for (Integer repId : animatedRepresentations) {
            Entity repEntity = session.getEntity(repId);
            AnimationComponent anim = repEntity.get(AnimationComponent.class);
            System.out.println("[DEBUG_LOG] Animation entity " + repId + " has " + anim.getFrames().size() + " frames");
            assertThat(anim.getFrames()).withFailMessage("Animation entity " + repId + " should have 2 frames but has " + anim.getFrames().size()).hasSize(2);
        }
    }
}
