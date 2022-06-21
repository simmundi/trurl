package pl.edu.icm.trurl.ecs.mapper.feature;

/**
 * This component has a "normalized" version (e.g. dashes removed from ISBN, immunizations
 * sorted by date etc.), and this normalized version needs to be persisted in the store.
 *
 * Method normalize will be called by the mapper as the very last step of persisting
 * the component. There should be no additional logic performed here, only the
 * normalization.
 */
public interface CanBeNormalized {
    void normalize();
}
