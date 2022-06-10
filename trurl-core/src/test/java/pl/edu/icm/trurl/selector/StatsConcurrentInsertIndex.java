package pl.edu.icm.trurl.selector;

import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.index.ConcurrentInsertComponentIndex;
import pl.edu.icm.trurl.exampledata.Stats;

/**
 * For tests (minimal empty implementation)
 */
public class StatsConcurrentInsertIndex extends ConcurrentInsertComponentIndex<Stats> {

    public StatsConcurrentInsertIndex(EngineConfiguration engineConfiguration) {
        super(engineConfiguration, Stats.class, 5, 10, 100);
    }
}
