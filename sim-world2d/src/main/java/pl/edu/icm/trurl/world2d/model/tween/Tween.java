package pl.edu.icm.trurl.world2d.model.tween;

import pl.edu.icm.trurl.ecs.dao.annotation.CategoryManagedBy;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

/**
 * Tween represents a change of one, two or three floats (denoted as q, w, e) from some initial state
 * (q1, w1, e1) to some target state (q2, w2, e2) over a period of time (duration). The change starts
 * at absolute time denoted as startTime (as provided by the GlobalTimer), can be automatically repeated
 * up to 128 times (repeat), with some delay between repetitions (loopDelay) and with the possibility to
 * have the repeats run in ping-pong mode (where each second loop goes backwards).
 *
 * The floats are mapped to target attributes by TweenTarget, the easing function is provided by Easing.
 */
@WithDao
public class Tween {
    // initial q
    private float q1;
    // initial w
    private float w1;
    // target q
    private float e1;

    private float q2;
    // target w
    private float w2;
    private float e2;
    @CategoryManagedBy(TweenTargetManager.class)
    private TweenTarget tweenTarget;
    @CategoryManagedBy(EasingManager.class)
    private Easing easing;

    // time (global timer) when the tween should start
    private float startTime;
    // duration of a single move between 1 and 2 statea (end time would be startTime + duration)
    private float duration;
    // number of repeats, with some special values:
    // - negative values mean yoyo
    // - Byte.MAX_VALUE means infinite
    // - Byte.MIN_VALUE means infinite with yoyo
    private byte repeat;
    private boolean targetIsRelative;
    private float loopDelay;

    public Tween() {}

    public float getQ1() {
        return q1;
    }

    /**
     * Sets initial value of q.
     * If set to Float.NaN, the value will be read from the target when the tweening starts.
     * @param q1
     */
    public void setQ1(float q1) {
        this.q1 = q1;
    }

    public float getW1() {
        return w1;
    }

    /**
     * Sets initial value of w
     * If set to Float.NaN, the value will be read from the target when the tweening starts.
     * @param w1
     */
    public void setW1(float w1) {
        this.w1 = w1;
    }

    public float getE1() {
        return e1;
    }

    /**
     * Sets initial value of e
     * If set to Float.NaN, the value will be read from the target when the animation starts.
     * @param e1
     */
    public void setE1(float e1) {
        this.e1 = e1;
    }

    public float getQ2() {
        return q2;
    }

    /**
     * Sets target value of q. If targetIsRelative is true, the value will be added to the initial value of q.
     * @param q2
     */
    public void setQ2(float q2) {
        this.q2 = q2;
    }

    public float getW2() {
        return w2;
    }


    /**
     * Sets target value of w. If targetIsRelative is true, the value will be added to the initial value of w.
     * @param w2
     */
    public void setW2(float w2) {
        this.w2 = w2;
    }

    public float getE2() {
        return e2;
    }

    /**
     * Sets target value of e. If targetIsRelative is true, the value will be added to the initial value of e.
     * @param e2
     */
    public void setE2(float e2) {
        this.e2 = e2;
    }

    public float getStartTime() {
        return startTime;
    }

    /**
     * Sets the time (in absolute terms, as used by the global timer) when the animation should start.
     * Because the value is absolute, there is no need to have settings for the initial delay.
     * @param startTime
     */
    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getDuration() {
        return duration;
    }

    /**
     * Sets a duration of a single transition from state 1 to 2. The length of the animation is
     * affected by the duration and also the number of repeats and the loop delay.
     * @param duration
     */
    public void setDuration(float duration) {
        this.duration = duration;
    }

    public byte getRepeat() {
        return repeat;
    }

    /**
     * Sets the number of repeats of the animation. The counter is 0 based, so 0 means a single run of the animation.
     * Negative values of repeat mean that animation will be run backwards (ping-pong mode) on every second repeat.
     * Value of Byte.MAX_VALUE means infinite number of repeats, while Byte.MIN_VALUE means infinite number of repeats
     * in ping-pong mode.
     * @param repeat
     */
    public void setRepeat(byte repeat) {
        this.repeat = repeat;
    }

    public float getLoopDelay() {
        return loopDelay;
    }

    /**
     * Sets delay after each execution of a repeat
     * @param loopDelay
     */
    public void setLoopDelay(float loopDelay) {
        this.loopDelay = loopDelay;
    }

    public TweenTarget getTweenTarget() {
        return tweenTarget;
    }

    /**
     * Sets the target of the animation. Some predefined targets are available in TweenTarget Category (e.g., {@code TweenTarget.BOUNDING_BOX_CENTER} ).
     * Due to the fact that the targets are serialized as integers, adding custom targets requires understanding
     * how categories (aka instance controlled classes) work in Bento and is currently not recommended.
     * @param tweenTarget
     */
    public void setTweenTarget(TweenTarget tweenTarget) {
        this.tweenTarget = tweenTarget;
    }

    public Easing getEasing() {
        return easing;
    }

    /**
     * Sets the easing used by the animation. Some predefined easing curves are available in the Easing Category (e.g. {@code Easing.CIRC_IN }.
     * The same caveat as for TweenTarget applies here.
     * @param easing
     */
    public void setEasing(Easing easing) {
        this.easing = easing;
    }

    public boolean isTargetIsRelative() {
        return targetIsRelative;
    }

    /**
     * If this is set, the target is interpreted in relation to the initial values of q, w, e (whether they are set from the target or given explicitly)
     * @param targetIsRelative
     */
    public void setTargetIsRelative(boolean targetIsRelative) {
        this.targetIsRelative = targetIsRelative;
    }
}
