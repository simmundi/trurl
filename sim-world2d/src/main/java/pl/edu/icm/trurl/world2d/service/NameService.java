package pl.edu.icm.trurl.world2d.service;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.world2d.model.DaoOfNamedFactory;
import pl.edu.icm.trurl.world2d.model.Named;
import pl.edu.icm.trurl.world2d.model.NamedDao;

import java.util.concurrent.ConcurrentHashMap;

public class NameService {
    private NamedDao namedDao;
    private ConcurrentHashMap<String, Integer> nameds = new ConcurrentHashMap<>();

    private EngineBuilder engineBuilder;

    @WithFactory
    public NameService(EngineBuilder engineBuilder) {
        this.engineBuilder = engineBuilder;
        this.engineBuilder.addComponentWithDao(Named.class, DaoOfNamedFactory.IT);
        this.engineBuilder.addListener(engine -> {
            namedDao = (NamedDao) engineBuilder.getEngine().getDaoManager().classToDao(Named.class);
        });
    }

    public boolean hasName(Entity entity, String name) {
        return hasName(entity.getId(), name);
    }

    public boolean hasName(int id, String name) {
        return name.equals(namedDao.getName(id));
    }

    public String getName(Entity entity) {
        return namedDao.getName(entity.getId());
    }

    public String getName(int id) {
        return namedDao.getName(id);
    }

    public int getId(String name, int defaultValue) {
        if (!nameds.containsKey(name)) {
            for (int i = 0; i < engineBuilder.getEngine().getCount(); i++) {
                if (namedDao.isPresent(i)) {
                    String newName = namedDao.getName(i);
                    if (!nameds.containsKey(name)) {
                        nameds.put(newName, i);
                    }
                }
            }
        }

        Integer result = nameds.get(name);
        return result == null ? defaultValue : result;
    }

    public int getId(String name) {
        int result = getId(name, -Entity.NULL_ID);
        if (result == -Entity.NULL_ID) {
            throw new IllegalArgumentException("No entity with name " + name);
        }
        return result;
    }
}
