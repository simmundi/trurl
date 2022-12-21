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
