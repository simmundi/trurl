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

package pl.edu.icm.trurl.ecs.dao.feature;

/**
 * <p>This component can resolve conflicts with parallel writes of the same component, by
 * merging itself with the conflicting version. There is an assumption here that the
 * resolve method is symmetrical, i.e. calling a.resolve(b) gives identical results to b.resolve(a).
 *
 * <p>Components with conflict resolution need to store the identifier of their owners, which can
 * be used to introduce determinism, like always keeping the version of the component with the
 * higher
 *
 * <p>Upon a conflict with another copy of the row, it can resolve the
 * conflict by inspecting the other copy and applying necessary changes
 * to itself.
 *
 * <p>The methods of this interface will be called by the auto-generated Dao</p>
 *
 * @param <T> MUST be the same class as the component
 */
public interface ComponentWithConflictResolution<T> {
    T resolve(T other);

    int getOwnerId();

    void setOwnerId(int ownerId);
}
