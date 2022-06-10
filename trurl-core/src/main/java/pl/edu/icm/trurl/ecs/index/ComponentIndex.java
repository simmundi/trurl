package pl.edu.icm.trurl.ecs.index;

import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.EngineCreationListener;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListener;

public abstract class ComponentIndex<T> implements MapperListener<T>, EngineCreationListener {

    private final Class<T> classToken;
    private Mapper<T> mapper;

    public ComponentIndex(EngineConfiguration engineConfiguration, Class<T> clazz) {
        this.classToken = clazz;
        engineConfiguration.addEngineCreationListeners(this);
    }

    @Override
    public final void onEngineCreated(Engine engine) {
        mapper = engine.getMapperSet().classToMapper(classToken);
        mapper.getMapperListeners().addSavingListener(this);
        afterOnEngineCreated(engine, mapper);
    }

    public void afterOnEngineCreated(Engine engine, Mapper<T> mapper) {

    }
}
