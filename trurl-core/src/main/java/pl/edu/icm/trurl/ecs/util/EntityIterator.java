 package pl.edu.icm.trurl.ecs.util;

 import pl.edu.icm.trurl.ecs.Entity;
 import pl.edu.icm.trurl.ecs.EntitySystem;
 import pl.edu.icm.trurl.ecs.Session;
 import pl.edu.icm.trurl.ecs.SessionFactory;
 import pl.edu.icm.trurl.ecs.mapper.LifecycleEvent;
 import pl.edu.icm.trurl.ecs.selector.Chunk;
 import pl.edu.icm.trurl.ecs.selector.Selector;

 import java.util.function.BiConsumer;
 import java.util.function.Consumer;
 import java.util.function.Function;

public class EntityIterator {
    private Session.Mode mode = Session.Mode.NORMAL;
    private boolean parallel = false;

    private final Selector selector;

    private final Function<Chunk, Void> NULL_CONTEXT_FACTORY = (unused) -> null;

    private EntityIterator(Selector selector) {
        this.selector = selector;
    }

    public EntityIterator parallel() {
        this.parallel = true;
        return this;
    }

    public static EntityIterator select(Selector selector) {
        return new EntityIterator(selector);
    }

    public EntityIterator detachEntities() {
        mode = Session.Mode.DETACHED_ENTITIES;
        return this;
    }

    public EntityIterator dontPersist() {
        mode = Session.Mode.NO_PERSIST;
        return this;
    }

    public <Context> EntitySystem forEach(Function<Chunk, Context> contextProducer, BiConsumer<Context, Entity> consumer) {
        return forEach(contextProducer, ((context, session, idx) -> consumer.accept(context, session.getEntity(idx))));
    }

    public EntitySystem forEach(Consumer<Entity> consumer) {
        return forEach(NULL_CONTEXT_FACTORY, ((context, session, idx) -> consumer.accept(session.getEntity(idx))));
    }

    public <Q> EntitySystem forEach(Class<Q> q, Systems.OneComponentSystem<Q> oneComponentSystem) {
        return forEach(NULL_CONTEXT_FACTORY, (context, session, idx) -> {
            Entity entity = session.getEntity(idx);
            oneComponentSystem.execute(entity, entity.get(q));
        });
    }

    public <Q, W> EntitySystem forEach(Class<Q> q, Class<W> w, Systems.TwoComponentSystem<Q, W> twoComponentSystem) {
        return forEach(NULL_CONTEXT_FACTORY, (context, session, idx) -> {
            Entity entity = session.getEntity(idx);
            twoComponentSystem.execute(entity, entity.get(q), entity.get(w));
        });
    }


    public <Q, W, E> EntitySystem forEach(Class<Q> q, Class<W> w, Class<E> e, Systems.ThreeComponentSystem<Q, W, E> threeComponentSystem) {
        return forEach(NULL_CONTEXT_FACTORY, (context, session, idx) -> {
            Entity entity = session.getEntity(idx);
            threeComponentSystem.execute(entity, entity.get(q), entity.get(w), entity.get(e));
        });
    }

    public <Q, W, E, R> EntitySystem forEach(Class<Q> q, Class<W> w, Class<E> e, Class<R> r, Systems.FourComponentSystem<Q, W, E, R> fourComponentSystem) {
        return forEach(NULL_CONTEXT_FACTORY, (context, session, idx) -> {
            Entity entity = session.getEntity(idx);
            fourComponentSystem.execute(entity, entity.get(q), entity.get(w), entity.get(e), entity.get(r));
        });
    }

    public <Context> EntitySystem forEach(Function<Chunk, Context> contextFactory, Systems.IdxProcessor<Context> idxProcessor) {
        return initialSessionFactory -> {
            SessionFactory sessionFactory = initialSessionFactory.withModeAndCount(mode, selector.estimatedChunkSize() * 8);
            if (parallel) {
                initialSessionFactory.lifecycleEvent(LifecycleEvent.PRE_PARALLEL_ITERATION);
            }
            (parallel ? selector.chunks().parallel() : selector.chunks()).forEach(chunk -> {
                final Session session = sessionFactory.create(chunk.getChunkInfo().getChunkId() + 1);
                Context context = contextFactory.apply(chunk);
                chunk.ids().forEach(id ->
                        idxProcessor.process(context, session, id)
                );
                session.close();
            });
            if (parallel) {
                initialSessionFactory.lifecycleEvent(LifecycleEvent.POST_PARALLEL_ITERATION);
            }
        };
    }
}
