package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ValueObjectListArrayAttribute;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ValueObjectListAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;


public class EntitiesSampler {
    private final EngineConfiguration engineConfiguration;
    private final AtomicInteger newIds = new AtomicInteger();
    private int[] oldToNewIdMapping;
    private List<Integer> newToOldIdMapping;

    @WithFactory
    public EntitiesSampler(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public void copySelected(Selector selector, Store newStore) {
        Store oldStore = engineConfiguration.getEngine().getStore();
        oldToNewIdMapping = createOldToNewIdMapping(selector);
        newToOldIdMapping = createNewToOldIdMapping();
        addAllRelatedEntities(oldStore, newStore);

        newStore.attributes().forEach(attribute -> {
            for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                if (Arrays.stream(attribute.getClass().getInterfaces())
                        .anyMatch(i -> i == EntityAttribute.class)) {

                    EntityAttribute entityAttribute = oldStore.get(attribute.name());
                    int oldEntityId = entityAttribute.getId(newToOldIdMapping.get(newId));

                    EntityAttribute newEntityAttribute = (EntityAttribute) attribute;
                    newEntityAttribute.setId(newId, oldToNewIdMapping[oldEntityId]);

                } else if (Arrays.stream(attribute.getClass().getInterfaces())
                        .anyMatch(i -> i == EntityListAttribute.class)) {

                    EntityListAttribute entityListAttribute = oldStore.get(attribute.name());

                    List<Integer> oldEntityIds = new ArrayList<>();
                    entityListAttribute.loadIds(newToOldIdMapping.get(newId), oldEntityIds::add);
                    oldEntityIds.replaceAll(oldId -> oldToNewIdMapping[oldId]);

                    EntityListAttribute newEntityListAttribute = (EntityListAttribute) attribute;
                    newEntityListAttribute.saveIds(newId, oldEntityIds.size(), oldEntityIds::get);

                } else if (Arrays.stream(attribute.getClass().getInterfaces())
                        .anyMatch(i -> i == ValueObjectListAttribute.class)) {

                    ValueObjectListAttribute valueObjectListAttribute = oldStore.get(attribute.name());

                    List<Integer> oldEntityIds = new ArrayList<>();
                    valueObjectListAttribute.loadIds(newToOldIdMapping.get(newId), oldEntityIds::add);
                    oldEntityIds.replaceAll(oldId -> oldToNewIdMapping[oldId]);
                    StringBuilder newEntityIds = new StringBuilder();
                    for (int i = 0; i < oldEntityIds.size(); i++) {
                        if (i == 0) {
                            newEntityIds.append(oldEntityIds.get(i));
                        } else {
                            newEntityIds.append(",").append(oldEntityIds.get(i));
                        }
                    }
                    if (!newEntityIds.toString().equals("")) {
                        ValueObjectListAttribute newValueObjectListAttribute = (ValueObjectListAttribute) attribute;
                        newValueObjectListAttribute.setString(newId, newEntityIds.toString());
                    }

                } else {
                    attribute.setString(newId, oldStore.get(attribute.name()).getString(newToOldIdMapping.get(newId)));
                }
            }
        });

        newStore.addInt("old_id");
        IntAttribute oldIdsAttribute = newStore.get("old_id");
        for (int i = 0; i < newToOldIdMapping.size(); i++) {
            oldIdsAttribute.setInt(i, newToOldIdMapping.get(i));
        }

        newStore.fireUnderlyingDataChanged(0, newToOldIdMapping.size());
    }

    private int[] createOldToNewIdMapping(Selector selector) {
        Engine engine = engineConfiguration.getEngine();
        int[] oldToNewMapping = new int[engine.getCount()];
        Arrays.fill(oldToNewMapping, Integer.MIN_VALUE);
        engine.execute(select(selector).dontPersist().forEach(entity -> {
            oldToNewMapping[entity.getId()] = newIds.getAndIncrement();
        }));
        return oldToNewMapping;
    }

    private List<Integer> createNewToOldIdMapping() {
        List<Integer> newToOldMapping = new ArrayList<>();
        for (int oldId = 0; oldId < oldToNewIdMapping.length; oldId++) {
            int newId = oldToNewIdMapping[oldId];
            if (newId >= 0) {
                newToOldMapping.add(newId, oldId);
            }
        }
        return newToOldMapping;
    }

    private boolean addOldToNewIdMapping(int oldId) {
        if (oldToNewIdMapping[oldId] >= 0) {
            return false;
        } else {
            int newId = newIds.getAndIncrement();
            oldToNewIdMapping[oldId] = newId;
            newToOldIdMapping.add(newId, oldId);
            return true;
        }
    }

    private void addAllRelatedEntities(Store oldStore, Store newStore) {
        AtomicBoolean changed = new AtomicBoolean(false);

        do {
            changed.set(false);
            newStore.attributes().filter(a -> Arrays.stream(a.getClass().getInterfaces())
                            .anyMatch(i -> i == EntityAttribute.class))
                    .forEach(attribute -> {
                        for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                            EntityAttribute entityAttribute = oldStore.get(attribute.name());

                            int oldEntityId = entityAttribute.getId(newToOldIdMapping.get(newId));
                            changed.set(addOldToNewIdMapping(oldEntityId));
                        }
                    });
            newStore.attributes().filter(a -> Arrays.stream(a.getClass().getInterfaces())
                            .anyMatch(i -> i == EntityListAttribute.class))
                    .forEach(attribute -> {
                        for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                            EntityListAttribute entityListAttribute = oldStore.get(attribute.name());

                            List<Integer> oldEntityIds = new ArrayList<>();
                            entityListAttribute.loadIds(newToOldIdMapping.get(newId), oldEntityIds::add);

                            oldEntityIds.forEach(oldId -> {
                                changed.set(addOldToNewIdMapping(oldId));
                            });
                        }
                    });
            newStore.attributes().filter(a -> Arrays.stream(a.getClass().getInterfaces())
                            .anyMatch(i -> i == ValueObjectListAttribute.class))
                    .forEach(attribute -> {
                        for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                            ValueObjectListArrayAttribute valueObjectListAttribute = oldStore.get(attribute.name());

                            List<Integer> oldIds = new ArrayList<>();
                            valueObjectListAttribute.loadIds(newToOldIdMapping.get(newId), oldIds::add);

                            oldIds.forEach(oldId -> {
                                changed.set(addOldToNewIdMapping(oldId));
                            });
                        }
                    });
        } while (changed.get());
    }
}

