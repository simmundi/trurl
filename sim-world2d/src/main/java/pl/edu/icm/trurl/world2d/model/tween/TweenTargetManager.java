package pl.edu.icm.trurl.world2d.model.tween;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.CategoryManager;
import pl.edu.icm.trurl.ecs.EngineBuilder;

import java.util.Arrays;
import java.util.List;

public class TweenTargetManager extends CategoryManager<TweenTarget> {

    @WithFactory
    public TweenTargetManager(Bento bento, EngineBuilder engineBuilder) {
        super(bento, "world2d.model.tween.target", TweenTargetFactory.IT);
        engineBuilder.addListener(engine -> {
            for (TweenTarget value : values()) {
                value.onEngineCreated(engine);
            }
        });
    }

    @Override
    protected List<TweenTarget> getBuiltIns() {
        return Arrays.asList(TweenTarget.BOUNDING_BOX_CENTER, TweenTarget.ROTATION, TweenTarget.ALPHA);
    }

    @Override
    public TweenTarget[] emptyArray() {
        return new TweenTarget[0];
    }
}
