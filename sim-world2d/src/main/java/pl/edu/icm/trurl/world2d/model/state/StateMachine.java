package pl.edu.icm.trurl.world2d.model.state;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao(namespace = "statemachine")
public class StateMachine {
    private short currentState = Short.MIN_VALUE;
    private short pendingEvent = Short.MIN_VALUE;
    private float since = 0;

    public short getCurrentState() {
        return currentState;
    }

    public void setCurrentState(short currentState) {
        this.currentState = currentState;
    }

    public short getPendingEvent() {
        return pendingEvent;
    }

    public void setPendingEvent(short pendingEvent) {
        this.pendingEvent = pendingEvent;
    }

    public float getSince() {
        return since;
    }

    public void setSince(float since) {
        this.since = since;
    }
}
