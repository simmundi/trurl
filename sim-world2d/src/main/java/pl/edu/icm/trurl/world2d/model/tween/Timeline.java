package pl.edu.icm.trurl.world2d.model.tween;

import pl.edu.icm.trurl.ecs.dao.annotation.CollectionType;
import pl.edu.icm.trurl.ecs.dao.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.ArrayList;
import java.util.List;

@WithDao
public class Timeline {
    @MappedCollection(collectionType = CollectionType.ARRAY_LIST)
    private List<Tween> tweens = new ArrayList<>();

    public List<Tween> getTweens() {
        return tweens;
    }

    public void addTween(Tween tween) {
        tweens.add(tween);
    }

    public Tween createAndAdd() {
        Tween tween = new Tween();
        addTween(tween);
        return tween;
    }
}
