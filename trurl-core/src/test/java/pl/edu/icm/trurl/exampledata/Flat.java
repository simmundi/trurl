package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;

@WithMapper
public class Flat {
    private Entity owner;
    private List<Entity> tenants = new ArrayList<>();

    public Flat() {
    }

    public Entity getOwner() {
        return owner;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    public List<Entity> getTenants() {
        return tenants;
    }
}
