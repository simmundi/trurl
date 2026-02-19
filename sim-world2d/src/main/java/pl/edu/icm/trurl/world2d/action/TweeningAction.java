package pl.edu.icm.trurl.world2d.action;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.EntityProcessor;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.world2d.model.tween.*;
import pl.edu.icm.trurl.world2d.service.GlobalTimer;

import java.util.Iterator;

public class TweeningAction implements EntityProcessor {

    private final GlobalTimer globalTimer;
    private TimelineDao timelineDao;
    private double timePassed;
    private final ThreeFloats empty = new ThreeFloats();

    @WithFactory
    public TweeningAction(EngineBuilder engineBuilder, GlobalTimer globalTimer) {
        this.globalTimer = globalTimer;
        // TODO
        engineBuilder.addComponentWithDao(Timeline.class, DaoOfTimelineFactory.IT);
        engineBuilder.addListener(engine -> {
            timelineDao = (TimelineDao) engine.getDaoManager().classToDao(Timeline.class);
        });
    }

    @Override
    public void run(Session session, int id) {
        rawRun(session, id, null);
    }

    @Override
    public void rawRun(Session session, int id, Entity entity) {
        if (!timelineDao.isPresent(id)) {
            return;
        }

        Entity e = entity != null ? entity : session.getEntity(id);
        Timeline timeline = e.get(Timeline.class);
        Iterator<Tween> tweenIterator = timeline.getTweens().iterator();
        while (tweenIterator.hasNext()) {
            Tween tween = tweenIterator.next();
            double elapsed = timePassed - tween.getStartTime();
            if (elapsed < 0) {
                continue;
            }
            int cycle = (int) (elapsed / (tween.getDuration() + tween.getLoopDelay()));
            int targetCycles = Math.abs(tween.getRepeat());

            if (tween.getRepeat() != Byte.MIN_VALUE && tween.getRepeat() != Byte.MAX_VALUE && cycle > targetCycles) {
                tweenIterator.remove();
                continue;
            }

            float timeInCurrentCycle = (float) elapsed - cycle * (tween.getDuration() + tween.getLoopDelay());
            float progress = Math.min(timeInCurrentCycle / tween.getDuration(), 1);

            Easing easing = tween.getEasing();
            TweenTarget tweenTarget = tween.getTweenTarget();

            ThreeFloats data = new ThreeFloats();

            if (Float.isNaN(tween.getQ1())) {
                tweenTarget.get(data, id);
                tween.setQ1(data.a);
                tween.setW1(data.b);
                tween.setE1(data.c);
            } else {
                data.a = tween.getQ1();
                data.b = tween.getW1();
                data.c = tween.getE1();
            }

            ThreeFloats target = new ThreeFloats();
            if (!tween.isTargetIsRelative()) {
                target.a = tween.getQ2();
                target.b = tween.getW2();
                target.c = tween.getE2();
            } else {
                target.a = tween.getQ2() + tween.getQ1();
                target.b = tween.getW2() + tween.getW1();
                target.c = tween.getE2() + tween.getE1();
            }

            if (tween.getRepeat() > 0 || cycle % 2 == 0) {
                easing.compute(data, target, empty, data, progress);
            } else {
                easing.compute(target, data, empty, data, progress);
            }
            tweenTarget.set(data, id);
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
                timePassed = globalTimer.getTotalTimePassed();
            }
        }, () -> null);
    }
}
