package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

@WithFactory
public class EntitiesSampler {
    private final Engine engine;


    public EntitiesSampler(Engine engine, Selectors selectors) {
        this.engine = engine;
    }

    public void copySelected(Selector selector, Store newStore) {
        Map<Integer, Integer> idMap = createIdMapping(selector, newStore);
        Store oldStore = engine.getStore();
        for(Attribute attribute : newStore.attributes().collect(Collectors.toList())) {
            for(int i=0; i < idMap.size(); i++) {
                attribute.setString(i, oldStore.get(attribute.name()).getString(idMap.get(i)));
            }
        }
    }

    private Map<Integer, Integer> createIdMapping(Selector selector, Store store) {
        Map<Integer, Integer> map = new HashMap<>();
        AtomicInteger i = new AtomicInteger(0);
        engine.execute(select(selector).dontPersist().forEach(entity -> {
            map.put(i.getAndIncrement(), entity.getId());
        }));
        return map;
    }
}

