package pl.edu.icm.trurl.world2d.model.tween;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.Category;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilderListener;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TweenTarget implements Category, EngineBuilderListener {

    public final static TweenTarget BOUNDING_BOX_CENTER = new TweenTarget("BOUNDING_BOX_CENTER", 0, "centerX:centerY");
    public final static TweenTarget ROTATION = new TweenTarget("ROTATION", 1, "graphicsTransform.rotation");
    public final static TweenTarget ALPHA = new TweenTarget("ALPHA", 2, "graphicsTransform.alpha");

    private final String name;
    private final byte ordinal;
    private final String paths;
    private FloatAttribute attributeA;
    private FloatAttribute attributeB;
    private FloatAttribute attributeC;

    @WithFactory
    public TweenTarget(String name, int ordinal, String paths) {
        this.name = name;
        this.ordinal = (byte) ordinal;
        this.paths = paths;
    }

    private Attribute attributeFromStore(Store store, String path) {
        int firstSlash = path.indexOf('/');
        if (firstSlash == -1) {
            return store.get(path);
        } else {
            String firstPart = path.substring(0, firstSlash);
            String secondPart = path.substring(firstSlash + 1);
            return attributeFromStore(store.getSubstore(firstPart), secondPart);
        }
    }

    public void get(ThreeFloats threeFloats, int row) {
        threeFloats.a = attributeA.getFloat(row);
        threeFloats.b = attributeB == null ? Float.NaN : attributeB.getFloat(row);
        threeFloats.c = attributeC == null ? Float.NaN : attributeC.getFloat(row);
    }

    public void set(ThreeFloats threeFloats, int row) {
        attributeA.setFloat(row, threeFloats.a);
        if (attributeB != null) {
            attributeB.setFloat(row, threeFloats.b);
        }
        if(attributeC != null) {
            attributeC.setFloat(row, threeFloats.c);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte ordinal() {
        return ordinal;
    }

    @Override
    public void onEngineCreated(Engine engine) {
        List<String> attributePaths = Arrays.stream(paths.split(":"))
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toList());

        if (attributePaths.size() > 0) {
            attributeA = (FloatAttribute) attributeFromStore(engine.getRootStore(), attributePaths.get(0));
        }
        if (attributePaths.size() > 1) {
            attributeB = (FloatAttribute) attributeFromStore(engine.getRootStore(), attributePaths.get(1));
        }
        if (attributePaths.size() > 2) {
            attributeC = (FloatAttribute) attributeFromStore(engine.getRootStore(), attributePaths.get(2));
        }
    }
}
