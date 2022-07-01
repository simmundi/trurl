package pl.edu.icm.trurl.ecs.mapper.feature;

/**
 * Component requires internal setup after all the data is fetched from store,
 * and before anyone else sees it.
 *
 * This might be viewed as a counterpart to normalize.
 *
 */
public interface RequiresSetup {
    void setup();
}
