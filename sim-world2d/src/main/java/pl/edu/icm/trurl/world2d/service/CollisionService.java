package pl.edu.icm.trurl.world2d.service;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.store.IntSink;
import pl.edu.icm.trurl.store.IntSource;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.world2d.model.space.BoundingBoxDao;

public class CollisionService {

    private BoundingBoxDao boundingBoxDao;
    private Engine engine;

    @WithFactory
    public CollisionService(EngineBuilder engineBuilder) {
        engineBuilder.addListener(e -> {
            boundingBoxDao = (BoundingBoxDao) e.getDaoManager().classToDao(BoundingBox.class);
            engine = e;
        });
    }

    public void find(BoundingBox collisionBox, IntSink sink) {
        int count = engine.getCount();
        find(collisionBox, sink, (idx -> idx), count);
    }

    public void find(BoundingBox collisionBox, IntSink sink, IntSource source, int count) {

        int idx = 0;

        for (int x = 0; x < count; x++) {
            int row = source.getInt(x);
            if (!boundingBoxDao.isPresent(row)) continue;

            float halfWidth = boundingBoxDao.getWidth(row) / 2;
            float halfHeight = boundingBoxDao.getHeight(row) / 2;
            float centerX = boundingBoxDao.getCenterX(row);
            float centerY = boundingBoxDao.getCenterY(row);

            if (collisionBox.overlapsMinMax(centerX - halfWidth, centerX + halfWidth, centerY - halfHeight, centerY + halfHeight)) {
                sink.setInt(idx++, row);
            }
        }
    }
}
