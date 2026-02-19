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

package pl.edu.icm.trurl.world2d.level;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.model.display.Displayable;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;

/**
 * Orchestrates the creation of entities from a LevelSource.
 */
public class LevelExecutor {
    private final Engine engine;

    public LevelExecutor(Engine engine) {
        this.engine = engine;
    }

    public void execute(LevelSource source, EntityCreatorPredicate filter, LevelEntityCustomizer customizer) {
        Session session = engine.getSession();
        try {
            source.forEach(prototype -> {
                // Phase 1: Filter
                if (filter.test(prototype)) {
                    // Phase 2: Create
                    Entity entity = session.createEntity();
                    
                    // Phase 3: Componentization (Defaults)
                    BoundingBox box = entity.getOrCreate(BoundingBox.class);
                    box.setCenterX(prototype.x() + prototype.width() / 2);
                    box.setCenterY(prototype.y() + prototype.height() / 2);
                    box.setWidth(prototype.width());
                    box.setHeight(prototype.height());
                    
                    if (prototype.representation() != Entity.NULL_ID) {
                        Displayable displayable = entity.getOrCreate(Displayable.class);
                        displayable.setRepresentation(session.getEntity(prototype.representation()));
                    }
                    
                    // Phase 4: Customize
                    customizer.customize(entity, prototype);
                }
            });
            
            // Phase 5: Resolution (can be added later if needed)
        } finally {
            session.flush();
        }
    }
}
