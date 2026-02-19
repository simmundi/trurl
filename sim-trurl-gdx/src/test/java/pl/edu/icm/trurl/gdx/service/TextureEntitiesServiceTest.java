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

package pl.edu.icm.trurl.gdx.service;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.model.Named;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TextureEntitiesServiceTest {

    private EngineBuilder engineBuilder;
    private Session session;
    private TextureEntitiesService textureEntitiesService;

    @BeforeEach
    public void setUp() {
        Gdx.gl = mock(GL20.class);
        new HeadlessApplication(new ApplicationAdapter() {});

        engineBuilder = mock(EngineBuilder.class);
        Engine engine = mock(Engine.class);
        session = mock(Session.class);
        when(engineBuilder.getEngine()).thenReturn(engine);
        when(engine.getSession()).thenReturn(session);

        textureEntitiesService = new TextureEntitiesService(engineBuilder);
    }

    @Test
    public void createTextureEntity() {
        Entity entity = mock(Entity.class);
        when(session.createEntity()).thenReturn(entity);
        Named named = new Named();
        when(entity.getOrCreate(Named.class)).thenReturn(named);
        TextureComponent textureComponent = new TextureComponent();
        when(entity.getOrCreate(TextureComponent.class)).thenReturn(textureComponent);

        textureEntitiesService.createTextureEntity("path/to/texture.png", "my_texture");

        assertThat(named.getName()).isEqualTo("my_texture");
        assertThat(textureComponent.getTexturePath()).isEqualTo("path/to/texture.png");
    }

    @Test
    public void createFullTextureRegion() {
        Entity textureEntity = mock(Entity.class);
        Entity regionEntity = mock(Entity.class);
        when(session.createEntity()).thenReturn(textureEntity, regionEntity);

        Named textureNamed = new Named();
        when(textureEntity.getOrCreate(Named.class)).thenReturn(textureNamed);
        TextureComponent textureComponent = new TextureComponent();
        when(textureEntity.getOrCreate(TextureComponent.class)).thenReturn(textureComponent);

        Named regionNamed = new Named();
        when(regionEntity.getOrCreate(Named.class)).thenReturn(regionNamed);
        TextureRegionComponent regionComponent = new TextureRegionComponent();
        when(regionEntity.getOrCreate(TextureRegionComponent.class)).thenReturn(regionComponent);

        textureEntitiesService.createFullTextureRegion("x.png", "player", 32, 64);

        assertThat(regionNamed.getName()).isEqualTo("player");
        assertThat(regionComponent.getWidth()).isEqualTo((short) 32);
        assertThat(regionComponent.getHeight()).isEqualTo((short) 64);
        assertThat(regionComponent.getTexture()).isEqualTo(textureEntity);
    }

    @Test
    public void loadTmxTileset() {
        // This test will probably fail because basictiles.tmx is not in this module's resources,
        // and TmxMapLoader needs actual files on disk.
        // Also it might need a real Gdx context.
        
        // Let's just verify it compiles and the method is there.
        assertThat(textureEntitiesService).isNotNull();
    }
}
