package pl.edu.icm.trurl.gdx.action;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.gdx.managed.ManagedOrthographicCamera;
import pl.edu.icm.trurl.gdx.managed.ManagedShapeRenderer;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.world2d.model.space.BoundingBoxDao;

public class DebugGeometryAction implements EntityProcessor {

    private final ShapeRenderer shapeRenderer;
    private final Camera camera;
    private BoundingBoxDao boundingBoxDao;

    @WithFactory
    public DebugGeometryAction(EngineBuilder engineBuilder, ManagedShapeRenderer shapeRenderer, ManagedOrthographicCamera camera) {
        this.shapeRenderer = shapeRenderer;
        this.camera = camera;
        engineBuilder.addListener(this::initEngine);
    }

    private void initEngine(Engine engine) {
        boundingBoxDao = (BoundingBoxDao) engine.getDaoManager().classToDao(BoundingBox.class);
    }

    @Override
    public void run(Session session, int id) {
        rawRun(session, id, null);
    }

    @Override
    public void rawRun(Session session, int idx, Entity entity) {
        if (!boundingBoxDao.isPresent(idx)) {
            return;
        }

        float x = boundingBoxDao.getCenterX(idx);
        float y = boundingBoxDao.getCenterY(idx);
        float w = boundingBoxDao.getWidth(idx);
        float h = boundingBoxDao.getHeight(idx);

        shapeRenderer.rect(x - w / 2, y - h / 2, w, h);
    }

    @Override
    public EntityProcessor reify() {
        return EntityProcessor.from(
                (session, id, entity, ctx, next) -> {
                    this.rawRun(session, id, entity);
                    next.runInternal(session, id, entity);
                },
                () -> {
                    return null;
                }
        ).reify(); // Wrap to allow onBegin/onEnd
    }

    @Override
    public void onBegin(Session session) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    @Override
    public void onEnd(Session session) {
        shapeRenderer.end();
    }
}
