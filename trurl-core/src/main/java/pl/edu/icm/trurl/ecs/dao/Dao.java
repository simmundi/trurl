/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.ecs.dao;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreConfig;
import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Daos persist and fetch component objects from a store.
 *
 * Each Dao needs to be attached to a single store, which contains attributes required
 * by its component. For example a Dao&lt;Person&gt; might require a store with string
 * attributes called "name" and "lastName". If a store lacks the expected attributes, an
 * exception is thrown.
 *
 * Daos can also configure a store to make it fit for attaching, by adding all the required
 * attributes, joins etc. This can be used both before attaching the Dao to the store, or
 * just as means to learn about the schema used by the Dao.
 *
 * @param <T> The component class.
 */
public interface Dao<T> {
    /**
     * Returns the name of the component, mostly usefuly for debug or exporting (e.g. for use
     * as the name of the data file)
     * @return
     */
    default String name() {
        return this.create().getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     * Configures the passed store to contain all the required attributes. It also creates all the
     * necessary joins and references, and configures recursively any substores created.
     *
     * Calling this method does not change the state of the Dao, so it can be called to extract the
     * schema of the Dao.
     * @param metadata
     */
    void configureStore(StoreConfig metadata);

    /**
     * Attaches the Dao to the given store. It is assumed that the store and its substores are compliant
     * with the requirements of this Dao.
     * @param store
     */
    void attachStore(Store store);

    /**
     * A sugar method for configuring and attaching a store in a single call.
     * @param store
     */
    default void configureAndAttach(Store store) {
        configureStore(store);
        attachStore(store);
    }

    /**
     * Verifies if an instane of the component is present in the given index.
     * @param index - the row to verify. Iff the store is the root store, the index is the
     *            entity identifier.
     * @return
     */
    boolean isPresent(int index);

    /**
     * Erases all the data in the row. TODO: describe the responsibilities of clearing the substores.
     * @param index
     */
    void erase(int index);

    /**
     * Creates a single, blank instance of the component, not connected to any row. The instance
     * should usually use the default constructor.
     * @return Component
     */
    T create();

    /**
     * It fills the component (and recursibely - all its child components) with the data fetched from
     * the store.
     * @param session - Session which is used to create entities present in the graph and establish the ownerId
     * @param component - component to fill in
     * @param index
     */
    void load(Session session, T component, int index);

    /**
     * A shortcut for creating and loading the entity in one call
     * @param row
     * @return
     */
    default T createAndLoad(Session session, int row) {
        T created = create();
        load(session, created, row);
        return created;
    }

    /**
     * A shortcut for creating and loading the entity in one call, with a null Session.
     * This method will fail in the parallel mode or when any Entity needs to be created
     * while fillng in the component data.
     * @param row
     * @return
     */
    default T createAndLoad(int row) {
        return createAndLoad(null, row);
    }

    /**
     * This method will recursively save all the data of the given component to the given index.
     * If the Dao was attached to the root store, then the index is the entity identifier.
     * @param component
     * @param index
     */
    void save(Session owner, T component, int index);

    /**
     * A shortcut for passing in a null session. This will fail in the parallel mode or if
     * there is an entity instance present in the component.
     * @param component
     * @param index
     */
    default void save(T component, int index) {
        save(null, component, index);
    }

    /**
     * Returns a list of attributes used by this Dao.
     * @return
     */
    List<Attribute> getAttributes();

    /**
     * Returns a list of all the child-daos used directly by this Dao. The child-daos are used
     * for persisting and fetching the direct component-typed attributes, and may have their
     * own child-daos.
     * @return list of direct Daos used by this Dao.
     */
    default List<Dao> getChildDaos() {
        return Collections.emptyList();
    }

    /**
     * Replaces all the Entity instances reachable from this component with stubs,
     * i.e. empty entities containing only their ID.
     * @param component
     */
    void stubEntities(T component);

    /**
     * This method shouldn't be used directly by the client code and for now it is only
     * used to set / unset the "parallel mode" internal flag.
     * It will eventually be replaced with something more robust.
     * @param lifecycleEvent
     */
    @Deprecated
    void fireEvent(LifecycleEvent lifecycleEvent);
}
