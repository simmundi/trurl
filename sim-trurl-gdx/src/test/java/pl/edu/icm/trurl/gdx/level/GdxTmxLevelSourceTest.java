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
import pl.edu.icm.trurl.world2d.level.LevelExecutor;
import pl.edu.icm.trurl.world2d.model.Named;
import pl.edu.icm.trurl.world2d.model.display.Displayable;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;

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
        engineBuilder.addComponentClasses(BoundingBox.class, Displayable.class, Named.class);
        Engine engine = engineBuilder.getEngine();
        LevelExecutor executor = new LevelExecutor(engine);

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
        assertThat(engine.getCount()).isEqualTo(8);
    }

    @Test
    public void testLoadBasictiles_VerifiesRepresentations() {
        // given
        Bento bento = Bento.createRoot();
        EngineBuilder engineBuilder = bento.get(EngineBuilderFactory.IT);
        engineBuilder.addComponentClasses(BoundingBox.class, Displayable.class);
        Engine engine = engineBuilder.getEngine();
        LevelExecutor executor = new LevelExecutor(engine);

        TiledMap map = new TmxMapLoader(new ClasspathFileHandleResolver()).load("basictiles.tmx");
        GdxTmxLevelSource source = new GdxTmxLevelSource(map);

        List<Integer> representations = new ArrayList<>();

        // execute
        executor.execute(source,
                p -> {
                    // Filter for a specific layer or area to keep it focused
                    // Layer "another layer" has GIDs like 79, 1, 92, 100, 28, 71, 9, 80, 49, 50, 51
                    return p.sourceId() == null && p.representation() != 11; // 11 is the background tile GID
                },
                (e, p) -> {
                    representations.add(p.representation());
                }
        );

        // assert
        // These GIDs are from "another layer" in basictiles.tmx
        assertThat(representations).contains(79, 1, 92, 100, 28, 71, 80);
        // And the 3x2 block of 9s and 49, 50, 51
        assertThat(representations).contains(9, 49, 50, 51);
    }
}
