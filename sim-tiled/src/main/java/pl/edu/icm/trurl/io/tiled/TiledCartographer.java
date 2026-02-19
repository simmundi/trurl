package pl.edu.icm.trurl.io.tiled;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.io.tiled.model.TilesetMetadata;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;
import pl.edu.icm.trurl.world2d.model.Named;


public class TiledCartographer {

    private final TsxService tsxService;
    private final EngineBuilder engineBuilder;

    @WithFactory
    public TiledCartographer(TsxService tsxService, EngineBuilder engineBuilder) {
        this.tsxService = tsxService;
        this.engineBuilder = engineBuilder;
        this.engineBuilder.addComponentClasses(BoundingBox.class);
        this.engineBuilder.addComponentClasses(Named.class);
    }

    public TiledSingleMap createMapBuilder() {
        return new TiledSingleMap(this);
    }

    public TilesetMetadata get(String path) {
        return tsxService.getTileset(path);
    }

    public Store getStore() {
        return engineBuilder.getEngine().getRootStore();
    }
}
