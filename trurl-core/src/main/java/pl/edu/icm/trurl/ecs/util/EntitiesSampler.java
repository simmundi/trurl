package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pl.edu.icm.trurl.ecs.util.EntityIterator.select;

@WithFactory
public class EntitiesSampler {
    private final EngineConfiguration engineConfiguration;


    public EntitiesSampler(EngineConfiguration engineConfiguration) {
        this.engineConfiguration = engineConfiguration;
    }

    public void copySelected(Selector selector, Store newStore) {
        List<Integer> oldIds = createIdMapping(selector);
        Store oldStore = engineConfiguration.getEngine().getStore();
        for (Attribute attribute : newStore.attributes().collect(Collectors.toList())) {
            if (attribute.name().equals("old_id")) {
                for (int i = 0; i < oldIds.size(); i++) {
                    attribute.setString(i, oldIds.get(i).toString());
                }
            } else {
                for (int i = 0; i < oldIds.size(); i++) {
                    attribute.setString(i, oldStore.get(attribute.name()).getString(oldIds.get(i)));
                }
            }

        }
    }

    private List<Integer> createIdMapping(Selector selector) {
        List<Integer> oldIds = new ArrayList<>();
        engineConfiguration.getEngine().execute(select(selector).dontPersist().forEach(entity -> {
            oldIds.add(entity.getId());
        }));
        return oldIds;
    }
}

