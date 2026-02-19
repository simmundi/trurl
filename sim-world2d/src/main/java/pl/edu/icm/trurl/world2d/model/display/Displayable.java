package pl.edu.icm.trurl.world2d.model.display;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.dao.annotation.Mapped;
import pl.edu.icm.trurl.ecs.dao.annotation.Type;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class Displayable {
    private Entity representation;
    @Mapped(type = Type.SPARSE)
    private GraphicsTransform graphicsTransform;

    public Entity getRepresentation() {
        return representation;
    }

    public void setRepresentation(Entity representation) {
        this.representation = representation;
    }

    public GraphicsTransform getGraphicsTransform() {
        return graphicsTransform;
    }

    public void setGraphicsTransform(GraphicsTransform graphicsTransform) {
        this.graphicsTransform = graphicsTransform;
    }
}
