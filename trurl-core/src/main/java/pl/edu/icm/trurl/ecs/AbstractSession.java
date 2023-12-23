package pl.edu.icm.trurl.ecs;

import pl.edu.icm.trurl.ecs.dao.ComponentOwner;

public abstract class AbstractSession implements ComponentOwner {

    public abstract AnyEntity getEntity(long id);

    public abstract AnyEntity createEntity(Object... components);
}
