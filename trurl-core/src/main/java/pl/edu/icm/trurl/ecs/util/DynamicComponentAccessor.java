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

package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.ComponentAccessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The default component accessor.
 */
public class DynamicComponentAccessor implements ComponentAccessor {
    private final Class[] componentClasses;
    private final Map<Class<?>, Integer> componentMap;

    @WithFactory
    public DynamicComponentAccessor(@ByName Collection componentClasses) {
        this.componentClasses = ((Collection<Class<?>>)componentClasses).toArray(new Class[]{});
        componentMap = new HashMap<>(this.componentClasses.length);
        for (int i = 0; i < this.componentClasses.length; i++) {
            componentMap.put(this.componentClasses[i], i);
        }
    }

    @Override
    public int classToIndex(Class<?> componentClass) {
        return componentMap.get(componentClass).intValue();
    }

    @Override
    public Class<?> indexToClass(int index) {
        return componentClasses[index];
    }

    @Override
    public int componentCount() {
        return componentClasses.length;
    }
}
