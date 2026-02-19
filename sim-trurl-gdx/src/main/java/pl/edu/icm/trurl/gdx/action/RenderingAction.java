package pl.edu.icm.trurl.gdx.action;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.EntityProcessor;
import pl.edu.icm.trurl.gdx.GdxTileTextureLoader;
import pl.edu.icm.trurl.world2d.model.display.*;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.world2d.model.space.BoundingBoxDao;
import pl.edu.icm.trurl.world2d.model.space.DaoOfBoundingBoxFactory;
import pl.edu.icm.trurl.world2d.service.AnimationResolver;

public class RenderingAction implements EntityProcessor {
    private final EngineBuilder engineBuilder;
    private final AnimationResolver animationResolver;
    private final GdxTileTextureLoader textureLoader;
    private final DisplayListener displayListener;
    private final Renderer renderer;

    BoundingBoxDao boundingBoxDao;
    DisplayableDao displayableDao;
    GraphicsTransformDao graphicsTransformDao;

    @WithFactory
    public RenderingAction(AnimationResolver animationResolver,
                           GdxTileTextureLoader textureLoader,
                           EngineBuilder engineBuilder,
                           DisplayListener displayListener,
                           Renderer renderer) {
        this.animationResolver = animationResolver;
        this.textureLoader = textureLoader;
        this.displayListener = displayListener;
        this.renderer = renderer;

        engineBuilder.addComponentWithDao(BoundingBox.class, DaoOfBoundingBoxFactory.IT);
        engineBuilder.addComponentWithDao(Displayable.class, DaoOfDisplayableFactory.IT);

        this.engineBuilder = engineBuilder;
        this.engineBuilder.addListener(this::initEngine);
    }

    private void initEngine(Engine engine) {
        displayableDao = (DisplayableDao) engine.getDaoManager().classToDao(Displayable.class);
        boundingBoxDao = (BoundingBoxDao) engine.getDaoManager().classToDao(BoundingBox.class);
        graphicsTransformDao = (GraphicsTransformDao) displayableDao.getGraphicsTransformDao();
    }

    @Override
    public void run(Session session, int id) {
        rawRun(session, id, null);
    }

    @Override
    public void rawRun(Session session, int idx, Entity entity) {
        if (!displayableDao.isPresent(idx) || Float.isNaN(boundingBoxDao.getCenterX(idx))) {
            return;
        }

        float centerX = boundingBoxDao.getCenterX(idx);
        float centerY = boundingBoxDao.getCenterY(idx);
        float width = boundingBoxDao.getWidth(idx);
        float height = boundingBoxDao.getHeight(idx);


        if (!renderer.isInFrustum(centerX, centerY, width / 2, height / 2)) {
            return;
        }

        boolean horizontalFlip = false;
        float rotation = 0;
        float alpha = 1;
        float scale = 1;
        float offset = 0;

        if (graphicsTransformDao.isPresent(idx)) {
            horizontalFlip = GraphicsTransform.isHorizontalFlip(graphicsTransformDao.getTransformMask(idx));
            rotation = graphicsTransformDao.getRotation(idx);
            alpha = graphicsTransformDao.getAlpha(idx);
            scale = graphicsTransformDao.getScale(idx);
            offset = graphicsTransformDao.getAnimationOffset(idx);
        }

        int representationId = displayableDao.getRepresentation(idx);
        Entity baseRepresentation = session.getEntity(representationId);

        Entity representationResolved = animationResolver.resolveRepresentationToAnimationFrame(baseRepresentation, offset);
        TextureRegion textureRegion = textureLoader.getRegion(representationResolved);
        float drawWidth = textureRegion.getRegionWidth();
        float drawHeight = textureRegion.getRegionHeight();

        if (scale < 0) {
            drawWidth = width * -scale;
            drawHeight = height * -scale;
            scale = -scale;
        }

        float x = centerX - drawHeight / 2;
        float y = centerY - drawWidth / 2;
        x = Math.round(x);
        y = Math.round(y);

        displayListener.onRepresentationDrawn(idx, centerX, centerY, drawWidth, drawHeight, rotation, scale, alpha);
        renderer.addTextureRegion(textureRegion, x, y, drawWidth, drawHeight, scale, rotation, alpha, horizontalFlip);
    }

    @Override
    public void onEnd(Session session) {
        renderer.render();
        renderer.clear();
    }

    @Override
    public EntityProcessor reify() {
        return this;
    }
}
