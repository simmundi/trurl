package pl.edu.icm.trurl.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface EntityProcessor extends LifecycleProcessor {

    void run(Session session, int entityId);

    default void run(Entity entity) {
        if (entity != null) {
            rawRun(entity.getSession(), entity.getId(), entity);
        }
    }

    void rawRun(Session session, int entityId, Entity entity);

    @Override
    default void onBegin(Session session) {
    }

    @Override
    default void onEnd(Session session) {
    }

    default EntityProcessor reify() {
        return this;
    }

    static EntityProcessor from(Consumer<Entity> consumer) {
        return new TopLevelChain(new BasicInternalLink((session, entityId, entity, next) -> {
            Entity e = entity != null ? entity : (session == null ? null : session.getEntity(entityId));
            consumer.accept(e);
            next.runInternal(session, entityId, e);
        }));
    }

    static EntityProcessor from(SessionEntityConsumer consumer) {
        return new TopLevelChain(new BasicInternalLink((session, entityId, entity, next) -> {
            consumer.run(session, entityId);
            next.runInternal(session, entityId, entity);
        }));
    }

    static EntityProcessor from(ProcessorWithNext processorWithNext) {
        return new TopLevelChain(new BasicInternalLink(processorWithNext::run));
    }

    static EntityProcessor from(ProcessorWithEntityAndNext processorWithNext) {
        return new TopLevelChain(new BasicInternalLink((session, entityId, entity, next) -> {
            Entity e = entity != null ? entity : (session == null ? null : session.getEntity(entityId));
            processorWithNext.run(e, (s, id, ent) -> next.runInternal(s, id, ent));
        }));
    }

    static EntityProcessor chain(EntityProcessor... processors) {
        List<InternalLink> flattened = new ArrayList<>();
        for (EntityProcessor processor : processors) {
            if (processor instanceof TopLevelChain) {
                InternalLink current = ((TopLevelChain) processor).first;
                while (current != FinalLink.INSTANCE) {
                    flattened.add(current);
                    current = current.nextLink;
                }
            } else if (processor != null) {
                // Should not happen if all are created via factories, but for safety:
                flattened.add(new BasicInternalLink((session, entityId, entity, next) -> {
                    processor.rawRun(session, entityId, entity);
                    next.runInternal(session, entityId, entity);
                }, processor));
            }
        }

        if (flattened.isEmpty()) {
            return new TopLevelChain(FinalLink.INSTANCE);
        }

        InternalLink last = FinalLink.INSTANCE;
        for (int i = flattened.size() - 1; i >= 0; i--) {
            InternalLink current = flattened.get(i);
            last = current.withNext(last);
        }

        return new TopLevelChain(last);
    }

    static EntityProcessor branch(Predicate predicate, EntityProcessor ifTrue, EntityProcessor ifFalse) {
        InternalLink trueLink = ifTrue instanceof TopLevelChain ? ((TopLevelChain) ifTrue).first :
                new BasicInternalLink((s, id, e, n) -> {
                    ifTrue.rawRun(s, id, e);
                    n.runInternal(s, id, e);
                });
        InternalLink falseLink = ifFalse instanceof TopLevelChain ? ((TopLevelChain) ifFalse).first :
                new BasicInternalLink((s, id, e, n) -> {
                    ifFalse.rawRun(s, id, e);
                    n.runInternal(s, id, e);
                });

        return new TopLevelChain(new BranchingInternalLink(predicate, trueLink, falseLink));
    }

    @FunctionalInterface
    interface Predicate {
        boolean test(Session session, int entityId);
    }

    @FunctionalInterface
    interface SessionEntityConsumer {
        void run(Session session, int entityId);
    }

    @FunctionalInterface
    interface ProcessorWithNext {
        void run(Session session, int entityId, Entity entity, InternalLink next);
    }

    @FunctionalInterface
    interface ProcessorWithEntityAndNext {
        void run(Entity entity, RawEntityProcessor next);
    }

    @FunctionalInterface
    interface RawEntityProcessor {
        void run(Session session, int entityId, Entity entity);
    }

    static <C> EntityProcessor from(StatefulProcessor<C> logic, java.util.function.Supplier<C> supplier) {
        return new TopLevelChain(new StatefulInternalLink<>(logic, supplier));
    }

    @FunctionalInterface
    interface StatefulProcessor<C> extends LifecycleProcessor {
        void run(Session session, int entityId, Entity entity, C context, InternalLink next);

        @Override
        default void onBegin(Session session) {
        }

        @Override
        default void onEnd(Session session) {
        }

        default void onBegin(Session session, C context) {
        }

        default void onEnd(Session session, C context) {
        }
    }

    abstract class InternalLink {
        protected final InternalLink nextLink;

        protected InternalLink(InternalLink nextLink) {
            this.nextLink = nextLink;
        }

        public abstract void runInternal(Session session, int entityId, Entity entity);

        public abstract void onBeginInternal(Session session);

        public abstract void onEndInternal(Session session);

        public abstract InternalLink withNext(InternalLink next);

        public InternalLink reify() {
            return this;
        }
    }

    class FinalLink extends InternalLink {
        static final FinalLink INSTANCE = new FinalLink();

        private FinalLink() {
            super(null);
        }

        @Override
        public void runInternal(Session session, int entityId, Entity entity) {
            // End of chain
        }

        @Override
        public void onBeginInternal(Session session) {
        }

        @Override
        public void onEndInternal(Session session) {
        }

        @Override
        public InternalLink withNext(InternalLink next) {
            return next;
        }

        @Override
        public InternalLink reify() {
            return this;
        }
    }

    class BasicInternalLink extends InternalLink {
        private final ProcessorWithNext logic;
        private final LifecycleProcessor lifecycle;

        public BasicInternalLink(ProcessorWithNext logic) {
            this(logic, null, FinalLink.INSTANCE);
        }

        public BasicInternalLink(ProcessorWithNext logic, LifecycleProcessor lifecycle) {
            this(logic, lifecycle, FinalLink.INSTANCE);
        }

        private BasicInternalLink(ProcessorWithNext logic, LifecycleProcessor lifecycle, InternalLink nextLink) {
            super(nextLink);
            this.logic = logic;
            this.lifecycle = lifecycle;
        }

        @Override
        public void runInternal(Session session, int entityId, Entity entity) {
            logic.run(session, entityId, entity, nextLink);
        }

        @Override
        public void onBeginInternal(Session session) {
            if (lifecycle != null) {
                lifecycle.onBegin(session);
            }
            nextLink.onBeginInternal(session);
        }

        @Override
        public void onEndInternal(Session session) {
            if (lifecycle != null) {
                lifecycle.onEnd(session);
            }
            nextLink.onEndInternal(session);
        }

        @Override
        public InternalLink withNext(InternalLink next) {
            return new BasicInternalLink(this.logic, this.lifecycle, next);
        }

        @Override
        public InternalLink reify() {
            InternalLink reifiedNext = nextLink.reify();
            return reifiedNext == nextLink ? this : withNext(reifiedNext);
        }
    }

    class BranchingInternalLink extends InternalLink {
        private final Predicate predicate;
        private final InternalLink ifTrue;
        private final InternalLink ifFalse;

        public BranchingInternalLink(Predicate predicate, InternalLink ifTrue, InternalLink ifFalse) {
            this(predicate, ifTrue, ifFalse, FinalLink.INSTANCE);
        }

        private BranchingInternalLink(Predicate predicate, InternalLink ifTrue, InternalLink ifFalse, InternalLink nextLink) {
            super(nextLink);
            this.predicate = predicate;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        @Override
        public void runInternal(Session session, int entityId, Entity entity) {
            if (predicate.test(session, entityId)) {
                ifTrue.runInternal(session, entityId, entity);
            } else {
                ifFalse.runInternal(session, entityId, entity);
            }
            nextLink.runInternal(session, entityId, entity);
        }

        @Override
        public void onBeginInternal(Session session) {
            ifTrue.onBeginInternal(session);
            ifFalse.onBeginInternal(session);
            nextLink.onBeginInternal(session);
        }

        @Override
        public void onEndInternal(Session session) {
            ifTrue.onEndInternal(session);
            ifFalse.onEndInternal(session);
            nextLink.onEndInternal(session);
        }

        @Override
        public InternalLink withNext(InternalLink next) {
            return new BranchingInternalLink(predicate, ifTrue, ifFalse, next);
        }

        @Override
        public InternalLink reify() {
            InternalLink reifiedTrue = ifTrue.reify();
            InternalLink reifiedFalse = ifFalse.reify();
            InternalLink reifiedNext = nextLink.reify();

            if (reifiedTrue == ifTrue && reifiedFalse == ifFalse && reifiedNext == nextLink) {
                return this;
            }
            return new BranchingInternalLink(predicate, reifiedTrue, reifiedFalse, reifiedNext);
        }
    }

    class StatefulInternalLink<C> extends InternalLink {
        private final StatefulProcessor<C> logic;
        private final java.util.function.Supplier<C> supplier;
        private final C context;

        public StatefulInternalLink(StatefulProcessor<C> logic, java.util.function.Supplier<C> supplier) {
            this(logic, supplier, null, FinalLink.INSTANCE);
        }

        private StatefulInternalLink(StatefulProcessor<C> logic, java.util.function.Supplier<C> supplier, C context, InternalLink nextLink) {
            super(nextLink);
            this.logic = logic;
            this.supplier = supplier;
            this.context = context;
        }

        @Override
        public void runInternal(Session session, int entityId, Entity entity) {
            logic.run(session, entityId, entity, context, nextLink);
        }

        @Override
        public void onBeginInternal(Session session) {
            logic.onBegin(session);
            logic.onBegin(session, context);
            nextLink.onBeginInternal(session);
        }

        @Override
        public void onEndInternal(Session session) {
            logic.onEnd(session);
            logic.onEnd(session, context);
            nextLink.onEndInternal(session);
        }

        @Override
        public InternalLink withNext(InternalLink next) {
            return new StatefulInternalLink<>(logic, supplier, context, next);
        }

        @Override
        public InternalLink reify() {
            return new StatefulInternalLink<>(logic, supplier, supplier.get(), nextLink.reify());
        }
    }

    class TopLevelChain implements EntityProcessor {
        final InternalLink first;

        TopLevelChain(InternalLink first) {
            this.first = first;
        }

        @Override
        public void run(Session session, int entityId) {
            first.runInternal(session, entityId, null);
        }

        @Override
        public void rawRun(Session session, int entityId, Entity entity) {
            first.runInternal(session, entityId, entity);
        }

        @Override
        public void onBegin(Session session) {
            first.onBeginInternal(session);
        }

        @Override
        public void onEnd(Session session) {
            first.onEndInternal(session);
        }

        @Override
        public EntityProcessor reify() {
            InternalLink reifiedFirst = first.reify();
            return reifiedFirst == first ? this : new TopLevelChain(reifiedFirst);
        }
    }
}
