package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;


public class EntitiesSampler {
    private final EngineConfiguration engineConfiguration;
    private final AtomicInteger newIds = new AtomicInteger();
    private int[] oldToNewIdMapping;
    private List<Integer> newToOldIdMapping;
    private Map<String, List<Integer>> fixedAttributes;

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
                AtomicReference<String> fixedAttributeName = new AtomicReference<>("");
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

                } else if (fixedAttributes.keySet().stream()
                        .anyMatch(a -> {

                            if (attribute.name().contains(a)) {
                                fixedAttributeName.set(a);
                                return true;
                            }
                            return false;
                        })) {

                    Attribute oldAttribute = oldStore.get(attribute.name());
                    if (fixedAttributes.get(fixedAttributeName.get()).contains(newId)) {
                        attribute.setString(newId, oldStore.get(attribute.name()).getString(newId));
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

    private void findAllFixedAttributes(Store oldStore, Store newStore) {
        Set<String> rawAttributeNames = new HashSet<>();
        newStore.attributes().filter(a -> a.name().endsWith("_ids") || a.name().endsWith("_start")).forEach(attribute -> {
            rawAttributeNames.add(attribute.name());
        });
        oldStore.attributes().filter(a -> rawAttributeNames.stream().anyMatch(ra -> a.name()
                        .contains(ra.replace("_ids", "").replace("_start", ""))))
                .forEach(attribute -> {
                    fixedAttributes.put(attribute.name(), new ArrayList<Integer>());
                    String matched = rawAttributeNames.stream().filter(ra -> attribute.name()
                            .contains(ra.replace("_ids", "").replace("_start", ""))).findAny().get();
                    fixedAttributes.put(matched.replace("_ids", ""), new ArrayList<>());

                    for (int i = 0; i < oldStore.getCount(); i++) {
                        if (matched.endsWith("_ids")) {
                            ValueObjectListAttribute oldAttribute = oldStore.get(matched);
                            String rawMatched = matched.replace("_ids", "");
                            oldAttribute.loadIds(i, (row, id) -> fixedAttributes.get(rawMatched).add(id));
                        } else if (matched.endsWith("_start")) {
                            IntAttribute startAttribute = oldStore.get(matched);
                            int start = startAttribute.getInt(i);
                            if (start >= 0) {
                                String rawMatched = matched.replace("_ids", "");
                                IntAttribute lengthAttribute = oldStore.get(rawMatched + "_length");
                                int length = lengthAttribute.getInt(i);
                                IntStream ids = IntStream.range(start, start + length);
                                ids.forEach(id -> fixedAttributes.get(rawMatched).add(id));
                            }

                        }
                    }
                });
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
        } while (changed.get());
    }
}

