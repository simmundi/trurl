package pl.edu.icm.trurl.world2d.service;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.model.space.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartitionService {

    private final EngineBuilder engineBuilder;

    @WithFactory
    public PartitionService(EngineBuilder engineBuilder) {
        this.engineBuilder = engineBuilder;
        engineBuilder.addComponentWithDao(Partition.class, DaoOfPartitionFactory.IT);
        engineBuilder.addComponentWithDao(BoundingBox.class, DaoOfBoundingBoxFactory.IT);
    }

    public void createSpatialPartitions(float width, float height) {
        Engine engine = engineBuilder.getEngine();
        BoundingBoxDao boundingBoxDao = (BoundingBoxDao) engine.getDaoManager().classToDao(BoundingBox.class);

        Map<BoundingBox, List<Entity>> paritionedEntities = new HashMap<>();

        for (int i = 0; i < engine.getCount(); i++) {
            if (boundingBoxDao.isPresent(i)) {
                BoundingBox box = boundingBoxDao.createAndLoad(i);

                // align the box to grid of width x height
                alignToGrid(width, height, box);

                // find if box is in the paritionedEntities map, if not create a new entry;
                // add the new entity stub to the list of entities in the partition
                List<Entity> entities = paritionedEntities.computeIfAbsent(box, k -> new ArrayList<>());
                entities.add(Entity.stub(i));
            }
        }

        Session session = engine.getSession();
        for (BoundingBox boundingBox : paritionedEntities.keySet()) {
            Entity entity = session.createEntity();
            entity.add(boundingBox);
            Partition partition = entity.getOrCreate(Partition.class);
            partition.setType(Partition.Type.SPATIAL);
            partition.getEntities().addAll(paritionedEntities.get(boundingBox));
            session.flush();
            session.clear();
        }
    }

    private void alignToGrid(float width, float height, BoundingBox box) {
        // set the center and the size of the box to the smallest rectangle that contains the box
        float x = box.getCenterX();
        float y = box.getCenterY();
        float halfWidth = box.getWidth() / 2;
        float halfHeight = box.getHeight() / 2;

        // align the box to the grid of width x height
        x = (float) Math.floor(x / width) * width + width / 2;
        y = (float) Math.floor(y / height) * height + height / 2;

        // grow the box to the grid of width x height
        halfWidth = (float) Math.ceil(halfWidth / width) * width;
        halfHeight = (float) Math.ceil(halfHeight / height) * height;

        // set the new center and size of the box
        box.setCenterX(x);
        box.setCenterY(y);
        box.setWidth(halfWidth * 2);
        box.setHeight(halfHeight * 2);
    }


}
