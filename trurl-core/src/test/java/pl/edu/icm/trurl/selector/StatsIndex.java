package pl.edu.icm.trurl.selector;

import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.index.ComponentIndex;
import pl.edu.icm.trurl.exampledata.Stats;

/**
 * For tests (minimal empty implementation)
 */
public class StatsIndex extends ComponentIndex<Stats> {

    public StatsIndex(EngineConfiguration engineConfiguration) {
        super(engineConfiguration, Stats.class);
    }

    @Override
    public void savingComponent(int id, Stats newValue) {

    }

}
