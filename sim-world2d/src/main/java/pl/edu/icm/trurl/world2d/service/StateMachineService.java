package pl.edu.icm.trurl.world2d.service;

import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.world2d.model.state.DaoOfStateMachineFactory;
import pl.edu.icm.trurl.world2d.model.state.StateMachine;
import pl.edu.icm.trurl.world2d.model.state.StateMachineDao;

import java.util.Arrays;

public abstract class StateMachineService<StateT extends Enum<StateT>, EventT extends Enum<EventT>> implements EntityProcessor {

    private final GlobalTimer globalTimer;
    private final float[] durations;
    private final short[] transitions;

    private final StateT initialState;
    private final EventT defaultEvent;
    private final StateT[] states;
    private final EventT[] events;
    private final boolean[] requiresWaiting;

    private StateMachineDao stateMachineDao;
    private ComponentToken<StateMachine> stateToken;

    public StateMachineService(GlobalTimer globalTimer, EngineBuilder engineBuilder, Class<StateT> stateClass, Class<EventT> eventClass, StateT initialState, EventT defaultEvent) {
        this.globalTimer = globalTimer;
        states = stateClass.getEnumConstants();
        events = eventClass.getEnumConstants();
        durations = new float[states.length];
        requiresWaiting = new boolean[events.length];
        transitions = new short[stateClass.getEnumConstants().length * eventClass.getEnumConstants().length];

        Arrays.fill(transitions, Short.MIN_VALUE);
        this.initialState = initialState;
        this.defaultEvent = defaultEvent;
        engineBuilder.addListener(this::init);
        // TODO: Dao should have a "getComponentClass" method
        // TODO: Components should be called Structs (?)
        engineBuilder.addComponentWithDao(StateMachine.class, DaoOfStateMachineFactory.IT);

        configure(new StateMachineConfigurer());
    }

    private float currentTime;

    public void init(Engine engine) {
        stateMachineDao = (StateMachineDao) engine.getDaoManager().classToDao(StateMachine.class);
        // TODO: these tokens should also be in the dao, shouldn't they?
        stateToken = engine.getDaoManager().classToToken(StateMachine.class);
    }

    @Override
    public void run(Session session, int id) {
        rawRun(session, id, null);
    }

    @Override
    public final void rawRun(Session session, int idx, Entity entity) {
        short pending = stateMachineDao.getPendingEvent(idx);
        if (pending == Short.MIN_VALUE) return;

        float duration = durations[stateMachineDao.getCurrentState(idx)];
        if (currentTime > duration + stateMachineDao.getSince(idx)) {
            Entity e = entity != null ? entity : session.getEntity(idx);
            StateT newState = states[pending];

            switchState(e, newState);
        }
    }

    @Override
    public EntityProcessor reify() {
        return EntityProcessor.from(new EntityProcessor.StatefulProcessor<Void>() {
            @Override
            public void run(Session session, int entityId, Entity entity, Void context, EntityProcessor.InternalLink next) {
                rawRun(session, entityId, entity);
                next.runInternal(session, entityId, entity);
            }

            @Override
            public void onBegin(Session session) {
                currentTime = (float) globalTimer.getTotalTimePassed();
            }
        }, () -> null);
    }

    protected class StateMachineConfigurer {

        public class BunchOfStates {

            public class BunchOfEvents {
                private final EventT[] events;

                @SafeVarargs
                public BunchOfEvents(EventT... events) {
                    this.events = events;
                }

                public BunchOfStates transitionTo(StateT targetState) {
                    for (StateT state : states) {
                        for (EventT event : events) {
                            from(state, event, targetState);
                        }
                    }
                    return BunchOfStates.this;
                }
            }

            private final StateT[] states;

            private BunchOfStates(StateT[] states) {
                this.states = states;
            }

            public BunchOfEvents on(EventT... events) {
                return new BunchOfEvents(events);
            }

            public BunchOfStates setDuration(float duration) {
                for (StateT state : states) {
                    StateMachineConfigurer.this.setDuration(state, duration);
                }
                return this;
            }

        }

        private StateMachineConfigurer() {
        }

        public StateMachineConfigurer setDuration(StateT state, float duration) {
            durations[state.ordinal()] = duration;
            return this;
        }

        public StateMachineConfigurer from(StateT originState, EventT event, StateT targetState) {
            if (originState != targetState) {
                short targetStateOrdinal = targetState == null ? Short.MIN_VALUE : (short) targetState.ordinal();
                transitions[originState.ordinal() * events.length + event.ordinal()] = targetStateOrdinal;
            }
            return this;
        }

        @SafeVarargs
        public final BunchOfStates from(StateT... states) {
            return new BunchOfStates(states);
        }

        public StateMachineConfigurer fromAny(EventT event, StateT targetState) {
            for (StateT state : states) {
                from(state, event, targetState);
            }
            return this;
        }

        public StateMachineConfigurer waitsForState(EventT... events) {
            for (EventT event : events) {
                requiresWaiting[event.ordinal()] = true;
            }
            return this;
        }

        public StateMachineConfigurer clearTransitionsFrom(StateT constrainedState) {
            for (EventT event : events) {
                from(constrainedState, event, null);
            }
            return this;
        }
    }

    protected abstract void configure(StateMachineConfigurer configurer);

    private short getTransition(int state, int event) {
        return transitions[state * events.length + event];
    }

    private void switchState(Entity entity, StateT newState) {
        StateMachine stateMachine = entity.get(stateToken);

        short current = stateMachine.getCurrentState();
        StateT previous = current >= 0 ? states[current] : null;

        if (previous != null) {
            exit(entity, previous);
        }
        stateMachine.setCurrentState((short) newState.ordinal());
        stateMachine.setSince(currentTime);
        short transition = getTransition(newState.ordinal(), defaultEvent.ordinal());
        if (transition >= 0) {
            stateMachine.setPendingEvent(transition);
        } else {
            stateMachine.setPendingEvent(Short.MIN_VALUE);
        }
        enter(entity, newState);
    }

    public final void resetState(Entity entity, StateT newState) {
        // TODO: fix add(token, object), it's badly broken!
        entity.add(new StateMachine());
        switchState(entity, newState);
    }

    public final void resetState(Entity entity) {
        resetState(entity, initialState);
    }

    public abstract void enter(Entity entity, StateT newState);

    public abstract void exit(Entity entity, StateT oldState);

    public final StateT sendEvent(Entity entity, EventT event) {
        StateMachine stateMachine = entity.get(StateMachine.class);
        if (stateMachine == null) return null;

        short newStateOrdinal = getTransition(stateMachine.getCurrentState(), event.ordinal());
        if (newStateOrdinal < 0) {
            return null;
        }
        StateT newState = states[newStateOrdinal];

        if (requiresWaiting[event.ordinal()]) {
            if (stateMachine.getPendingEvent() < 0) {
                stateMachine.setPendingEvent(getTransition(stateMachine.getCurrentState(), event.ordinal()));
            } else {
                return null;
            }
        } else {
            switchState(entity, newState);
        }

        return newState;
    }

    public StateT getState(Entity e) {
        short currentState = e.get(stateToken).getCurrentState();
        return currentState >= 0 ? states[currentState] : null;
    }

    public StateT getState(int id) {
        short currentState = stateMachineDao.getCurrentState(id);
        return currentState >= 0 ? states[currentState] : null;
    }
}
