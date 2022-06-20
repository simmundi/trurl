package pl.edu.icm.trurl.ecs.mapper;

/**
 * <p>
 *     The intended example uses of listeners are:
 * </p>
 * <ul>
 *     <li>writing a business-specific persistent log of all the changes</li>
 *     <li>creating useful, business-specific indices</li>
 *     <li>syncing changes across instances or databases</li>
 * </ul>
 * <p>
 *     The "business specific" is important, because this is what differs
 *     the idea of listening to a mapper from the idea of listening to a store.
 * </p>
 *
 * @param <T> Type of component
 */
public interface MapperListener<T> {
    void savingComponent(int id, T newValue);
    default void lifecycleEvent(LifecycleEvent lifecycleEvent) {}
}
