/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

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
import java.util.stream.IntStream;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;


public class EntitiesSubset {
    private final EngineConfiguration engineConfiguration;
    private final AtomicInteger newIds = new AtomicInteger();
    private int[] oldToNewIdMapping;
    private Map<Integer, Integer> newToOldIdMapping;
    private final Map<String, Map<Integer, Integer>> objectAttributesOldToNewIdMapping = new HashMap<>();
    private final Map<String, List<Integer>> objectAttributesNewToOldIdMapping = new HashMap<>();
    private int objectAttributeMaxLength;

    @WithFactory
    public EntitiesSubset(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public void copySelected(Selector selector, Store newStore) {
        Store oldStore = engineConfiguration.getEngine().getStore();
        oldToNewIdMapping = createOldToNewIdMapping(selector);
        newToOldIdMapping = createNewToOldIdMapping();
        addAllRelatedEntities(oldStore, newStore);
        findAllObjectAttributes(oldStore, newStore);
        newStore.attributes().forEach(attribute -> {
            attribute.ensureCapacity(Math.max(newToOldIdMapping.size(), objectAttributeMaxLength));
            for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                String attributeName = attribute.name();
                if (attribute instanceof EntityAttribute) {

                    EntityAttribute entityAttribute = oldStore.get(attributeName);
                    int oldEntityId = entityAttribute.getId(newToOldIdMapping.get(newId));

                    if (oldEntityId >= 0) {
                        EntityAttribute newEntityAttribute = (EntityAttribute) attribute;
                        newEntityAttribute.setId(newId, oldToNewIdMapping[oldEntityId]);
                    }

                } else if (attribute instanceof EntityListAttribute) {

                    EntityListAttribute entityListAttribute = oldStore.get(attributeName);

                    List<Integer> oldEntityIds = new ArrayList<>();
                    entityListAttribute.loadIds(newToOldIdMapping.get(newId), oldEntityIds::add);
                    oldEntityIds.replaceAll(oldId -> oldToNewIdMapping[oldId]);

                    EntityListAttribute newEntityListAttribute = (EntityListAttribute) attribute;
                    newEntityListAttribute.saveIds(newId, oldEntityIds.size(), oldEntityIds::get);

                } else if (objectAttributesNewToOldIdMapping.containsKey(attributeName)) {

                    if (newId < objectAttributesNewToOldIdMapping.get(attributeName).size()) {
                        attribute.setString(newId, oldStore.get(attributeName)
                                .getString(objectAttributesNewToOldIdMapping.get(attributeName).get(newId)));
                    }

                } else if (attributeName.endsWith("_start")) { //todo: wrap object range attribute similarly to entity
                    Attribute objectAttribute = oldStore.get(attributeName);
                    int oldObjectId = Integer.parseInt(objectAttribute
                            .getString(newToOldIdMapping.get(newId)));

                    if (oldObjectId >= 0) {
                        String rawAttributeNameWithPrefix = attributeName.replace("_start", "");
                        String[] rawAttributeNameArray = rawAttributeNameWithPrefix.split("\\.");
                        String rawAttributeName = rawAttributeNameArray[rawAttributeNameArray.length - 1];
                        String matchedAttributeName = objectAttributesNewToOldIdMapping.keySet().stream().filter(is ->
                                        is.contains(rawAttributeName)).findAny()
                                .orElseThrow(() -> new IllegalStateException("Could not find matching attribute for: " + attributeName));
                        attribute.setString(newId,
                                String.valueOf(objectAttributesOldToNewIdMapping.get(matchedAttributeName).get(oldObjectId)));
                    }
                } else if (attribute instanceof ValueObjectListAttribute) {
                    if (!oldStore.get(attributeName).isEmpty(newToOldIdMapping.get(newId))) {
                        ValueObjectListAttribute valueObjectListAttribute = (ValueObjectListAttribute) attribute;
                        String rawAttributeNameWithPrefix = attributeName.replace("_ids", "");
                        String[] rawAttributeNameArray = rawAttributeNameWithPrefix.split("\\.");
                        String rawAttributeName = rawAttributeNameArray[rawAttributeNameArray.length - 1];
                        String matchedAttributeName = objectAttributesNewToOldIdMapping.keySet().stream().filter(oa ->
                                        oa.contains(rawAttributeName)).findAny()
                                .orElseThrow(() -> new IllegalStateException("Could not find matching attribute for: " + attributeName));
                        ValueObjectListAttribute oldValueObjectListAttribute = oldStore.get(attributeName);
                        List<Integer> objectIds = new ArrayList<>();
                        oldValueObjectListAttribute.loadIds(newToOldIdMapping.get(newId), objectIds::add);
                        objectIds.replaceAll(id -> id = objectAttributesOldToNewIdMapping.get(matchedAttributeName).get(id));
                        valueObjectListAttribute.saveIds(newId, objectIds.size(), objectIds::get);
                    }

                } else {
                    attribute.setString(newId, oldStore.get(attributeName)
                            .getString(newToOldIdMapping.get(newId)));

                }
            }
        });

        newStore.attributes().filter(a -> objectAttributesNewToOldIdMapping.containsKey(a.name())).forEach(attribute -> {
            for (int newId = newToOldIdMapping.size(); newId < objectAttributeMaxLength; newId++) {
                String attributeName = attribute.name();
                if (newId < objectAttributesNewToOldIdMapping.get(attributeName).size()) {
                    attribute.setString(newId, oldStore.get(attributeName)
                            .getString(objectAttributesNewToOldIdMapping.get(attributeName).get(newId)));

                }
            }
        });

        newStore.addInt("old_id");
        IntAttribute oldIdsAttribute = newStore.get("old_id");
        oldIdsAttribute.ensureCapacity(newToOldIdMapping.size());
        for (int i = 0; i < newToOldIdMapping.size(); i++) {
            oldIdsAttribute.setInt(i, newToOldIdMapping.get(i));
        }

        newStore.fireUnderlyingDataChanged(0, Math.max(newToOldIdMapping.size(), objectAttributeMaxLength));
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

    private Map<Integer, Integer> createNewToOldIdMapping() {
        Map<Integer, Integer> newToOldMapping = new HashMap<>();
        for (int oldId = 0; oldId < oldToNewIdMapping.length; oldId++) {
            int newId = oldToNewIdMapping[oldId];
            if (newId >= 0) {
                newToOldMapping.put(newId, oldId);
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
            newToOldIdMapping.put(newId, oldId);
            return true;
        }
    }

    private void findAllObjectAttributes(Store oldStore, Store newStore) {
        Map<String, AttributeType> attributeTypeMap = new HashMap<>();
        Map<String, String> rawAttributePrefixMap = new HashMap<>();
        newStore.attributes().filter(a -> a.name().endsWith("_ids") || a.name().endsWith("_start")).forEach(attribute -> {
            String rawAttributeNameWithPrefix = attribute.name()
                    .replace("_ids", "").replace("_start", "");
            String[] rawAttributeNameArray = rawAttributeNameWithPrefix.split("\\.");
            String rawAttributeName = rawAttributeNameArray[rawAttributeNameArray.length - 1];
            rawAttributePrefixMap.put(rawAttributeName, rawAttributeNameWithPrefix);

            if (attribute.name().endsWith("_ids")) {
                attributeTypeMap.put(rawAttributeName, AttributeType.ARRAY_LIST);
            } else {
                attributeTypeMap.put(rawAttributeName, AttributeType.RANGE);
            }
        });
        oldStore.attributes().filter(a -> !a.name().endsWith("_ids")
                        && !a.name().endsWith("_start")
                        && !a.name().endsWith("_length")
                        && attributeTypeMap.keySet().stream().anyMatch(is -> a.name().contains(is)))
                .forEach(attribute -> {
                    String attributeName = attribute.name();
                    objectAttributesNewToOldIdMapping.put(attributeName, new ArrayList<>());
                    objectAttributesOldToNewIdMapping.put(attributeName, new HashMap<>());
                    String matched = attributeTypeMap.keySet().stream()
                            .filter(attributeName::contains).findAny()
                            .orElseThrow(() -> new IllegalStateException("Could not find matching attribute for: " + attributeName));
                    String matchedWithPrefix = rawAttributePrefixMap.get(matched);
                    for (int oldId : newToOldIdMapping.values()) {
                        if (attributeTypeMap.get(matched) == AttributeType.ARRAY_LIST) {
                            ValueObjectListAttribute oldAttribute = oldStore.get(matchedWithPrefix + "_ids");
                            oldAttribute.loadIds(oldId, (row, id) -> {
                                objectAttributesNewToOldIdMapping.get(attributeName).add(id);
                                int newId = objectAttributesNewToOldIdMapping.get(attributeName).size() - 1;
                                objectAttributesOldToNewIdMapping.get(attributeName).put(id, newId);
                            });
                        } else if (attributeTypeMap.get(matched) == AttributeType.RANGE) {
                            IntAttribute startAttribute = oldStore.get(matchedWithPrefix + "_start");
                            int start = startAttribute.getInt(oldId);
                            if (start >= 0) {
                                Attribute lengthAttribute = oldStore.get(matchedWithPrefix + "_length");
                                int length = Integer.parseInt(lengthAttribute.getString(oldId));
                                IntStream ids = IntStream.range(start, start + length);
                                ids.forEach(id -> {
                                    objectAttributesNewToOldIdMapping.get(attributeName).add(id);
                                    int newId = objectAttributesNewToOldIdMapping.get(attributeName).size() - 1;
                                    objectAttributesOldToNewIdMapping.get(attributeName).put(id, newId);
                                    objectAttributeMaxLength = Math.max(objectAttributeMaxLength, newId + 1);
                                });
                            }

                        }
                    }
                });
    }

    private void addAllRelatedEntities(Store oldStore, Store newStore) {
        AtomicBoolean changed = new AtomicBoolean(false);

        do {
            changed.set(false);
            newStore.attributes().filter(a -> a instanceof EntityAttribute)
                    .forEach(attribute -> {
                        for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                            EntityAttribute entityAttribute = oldStore.get(attribute.name());

                            int oldEntityId = entityAttribute.getId(newToOldIdMapping.get(newId));
                            if (oldEntityId >= 0) {
                                changed.set(addOldToNewIdMapping(oldEntityId) || changed.get());
                            }
                        }
                    });
            newStore.attributes().filter(a -> a instanceof EntityListAttribute)
                    .forEach(attribute -> {
                        for (int newId = 0; newId < newToOldIdMapping.size(); newId++) {
                            EntityListAttribute entityListAttribute = oldStore.get(attribute.name());

                            List<Integer> oldEntityIds = new ArrayList<>();
                            entityListAttribute.loadIds(newToOldIdMapping.get(newId), oldEntityIds::add);

                            oldEntityIds.forEach(oldId -> {
                                if (oldId >= 0) {
                                    changed.set(addOldToNewIdMapping(oldId) || changed.get());
                                }
                            });
                        }
                    });
        } while (changed.get());
    }

    private enum AttributeType {
        ARRAY_LIST,
        RANGE
    }
}

