package pl.edu.icm.trurl.gdx.service;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.model.*;
import pl.edu.icm.trurl.world2d.model.display.Displayable;
import pl.edu.icm.trurl.world2d.model.display.GraphicsTransform;
import pl.edu.icm.trurl.world2d.model.display.TextureRegionComponent;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.world2d.model.space.Velocity;
import pl.edu.icm.trurl.world2d.service.AnimationResolver;
import pl.edu.icm.trurl.world2d.service.GlobalTimer;
import pl.edu.icm.trurl.world2d.service.NameService;

import java.util.concurrent.atomic.AtomicReference;

public class SimEntitiesService {
    private final NameService nameService;
    private final GlobalTimer globalTimer;
    private final AnimationResolver animationResolver;
    private final EngineBuilder engineBuilder;
    private boolean autoFlush = true;

    @WithFactory
    public SimEntitiesService(NameService nameService, GlobalTimer globalTimer, AnimationResolver animationResolver, EngineBuilder engineBuilder) {
        this.nameService = nameService;
        this.globalTimer = globalTimer;
        this.animationResolver = animationResolver;
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

    public EntityManipulator usingNewEntity() {
        return new EntityManipulator(session().createEntity());
    }

    public EntityManipulator using(Entity entity) {
        return new EntityManipulator(entity);
    }

    public final class EntityManipulator {
        private final Entity entity;

        private EntityManipulator(Entity entity) {
            this.entity = entity;
        }

        public EntityManipulator name(String name) {
            entity.getOrCreate(Named.class).setName(name);
            return this;
        }

        public EntityManipulator moveTo(float x, float y) {
            BoundingBox bb = entity.getOrCreate(BoundingBox.class);
            bb.setCenterX(x);
            bb.setCenterY(y);
            return this;
        }

        public EntityManipulator moveBy(float dx, float dy) {
            BoundingBox bb = entity.getOrCreate(BoundingBox.class);
            bb.setCenterX(bb.getCenterY() + dx);
            bb.setCenterY(bb.getCenterY() + dy);
            return this;
        }

        public EntityManipulator setVelocity(float dx, float dy) {
            entity.getOrCreate(Velocity.class).setDx(dx);
            entity.getOrCreate(Velocity.class).setDy(dy);
            return this;
        }

        public EntityManipulator setBounds(float width, float height) {
            BoundingBox bb = entity.getOrCreate(BoundingBox.class);
            bb.setWidth(width);
            bb.setHeight(height);
            return this;
        }

        public EntityManipulator addComponent(Object component) {
            entity.add(component);
            return this;
        }

        public EntityManipulator displayAs(String representationName) {
            Entity representation = entity.getSession().getEntity(nameService.getId(representationName));
            return displayAs(representation);
        }

        public EntityManipulator displayAs(Entity representation) {
            Displayable displayable = entity.getOrCreate(Displayable.class);
            displayable.setRepresentation(representation);
            return this;
        }

        public EntityManipulator copyBoundsAndPositionFrom(String name) {
            Entity entity = this.entity.getSession().getEntity(nameService.getId(name));
            return copyBoundsAndPositionFrom(entity);
        }

        public EntityManipulator copyBoundsAndPositionFrom(Entity entity) {
            BoundingBox bb = entity.get(BoundingBox.class);
            if (bb != null) {
                BoundingBox bb2 = this.entity.getOrCreate(BoundingBox.class);
                bb2.setCenterX(bb.getCenterX());
                bb2.setCenterY(bb.getCenterY());
                bb2.setWidth(bb.getWidth());
                bb2.setHeight(bb.getHeight());
            }
            return this;
        }

        public EntityManipulator copyDisplayFrom(String name) {
            Entity entity = this.entity.getSession().getEntity(nameService.getId(name));
            return copyDisplayFrom(entity);
        }

        public EntityManipulator copyDisplayFrom(Entity entity) {
            Displayable displayable = entity.get(Displayable.class);
            if (displayable != null) {
                Displayable displayable2 = this.entity.getOrCreate(Displayable.class);
                displayable2.setRepresentation(displayable.getRepresentation());
            }
            return this;
        }

        public EntityManipulator resetAnimation() {
            float passed = (float) globalTimer.getTotalTimePassed();
            getOrCreateGraphicsTransform().setAnimationOffset(passed);
            return this;
        }

        public EntityManipulator copyTransformFrom(String name) {
            Entity entity = this.entity.getSession().getEntity(nameService.getId(name));
            return copyTransformFrom(entity);
        }

        public EntityManipulator setScale(int i) {
            GraphicsTransform graphicsTransform = getOrCreateGraphicsTransform();
            graphicsTransform.setScale(i);
            return this;
        }

        public EntityManipulator setSizeFromDisplay() {
            Displayable displayable = entity.get(Displayable.class);
            if (displayable != null) {
                Entity representation = animationResolver.resolveRepresentationToAnimationFrame(displayable.getRepresentation(), 0);
                TextureRegionComponent textureRegionComponent = representation.get(TextureRegionComponent.class);
                setBounds(textureRegionComponent.getWidth(), textureRegionComponent.getHeight());
            }
            return this;
        }

        public EntityManipulator copyTransformFrom(Entity entity) {
            GraphicsTransform graphicsTransform = entity.get(Displayable.class).getGraphicsTransform();
            if (graphicsTransform != null) {
                GraphicsTransform graphicsTransform2 = this.entity.getOrCreate(GraphicsTransform.class);
                graphicsTransform2.setAlpha(graphicsTransform.getAlpha());
                graphicsTransform2.setScale(graphicsTransform.getScale());
                graphicsTransform2.setRotation(graphicsTransform.getRotation());
                graphicsTransform2.setHorizontalFlip(graphicsTransform.isHorizontalFlip());
            }
            return this;
        }

        public EntityManipulator setRotation(float angle) {
            GraphicsTransform graphicsTransform = getOrCreateGraphicsTransform();
            graphicsTransform.setRotation(angle);
            return this;
        }

        public EntityManipulator rotateClockwise(float da) {
            GraphicsTransform graphicsTransform = getOrCreateGraphicsTransform();
            graphicsTransform.setRotation(graphicsTransform.getRotation() - da);
            return this;
        }

        public Entity get() {
            maybeFlush();
            return entity;
        }

        private GraphicsTransform getOrCreateGraphicsTransform() {
            Displayable displayable = entity.getOrCreate(Displayable.class);
            GraphicsTransform graphicsTransform = displayable.getGraphicsTransform();
            if (graphicsTransform == null) {
                graphicsTransform = new GraphicsTransform();
                displayable.setGraphicsTransform(graphicsTransform);
            }
            return graphicsTransform;
        }
    }
}
