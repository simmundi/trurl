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

package pl.edu.icm.trurl.store;

import pl.edu.icm.trurl.store.attribute.Attribute;

import java.util.stream.Stream;

/**
 * Represents a columnar store.
 * <p>
 * The interface gives access to specific attributes (instances of Attribute, which can be
 * downcast to interfaces like ShortAttribute, StringAttribute etc.) and acts as a hub
 * for adding listeners and firing general events.
 * <p>
 * The events are not fired automatically by any part of the store; they are meant
 * for situations where a couple of consumers of the same store wish to let
 * one another know about changes to the data - like when a loader loads data
 * into the store.
 * <p>
 * Within Trurl, the stores are meant to store entities / components.
 * <p>
 * The flow is:
 *
 * <ul>
 *     <li>
 *         code creates a store instance,
 *     </li>
 *     <li>
 *         the instance is passed to Mappers, which use the StoreMetadata
 *     interface to configure types and names of the store's columns
 *     </li>
 *     <li>
 *         the mappers are then attached to the store (i.e. get all the
 *     necessary attributes) and become its listeners
 *     </li>
 *     <li>
 *         the store configuration is used to load raw data from a file
 *     </li>
 *     <li>
 *         the loader fires underlying data changed event; the mappers
 *     handle it and use it to find out about the length of the data and
 *     to fire any required events to their relevant listeners.
 *     </li>
 * </ul>
 */
public interface Store extends StoreConfigurer, StoreInspector, StoreObservable {

}
