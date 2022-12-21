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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import pl.edu.icm.trurl.ecs.ComponentAccessor;

import java.util.Arrays;

public class DynamicComponentAccessor implements ComponentAccessor {
    private final Class[] componentClasses;
    private final Object2IntMap<Class> componentMap;

    public DynamicComponentAccessor(Class... componentClasses) {
        this.componentClasses = Arrays.copyOf(componentClasses, componentClasses.length);
        componentMap = new Object2IntOpenHashMap<>(componentClasses.length);
        for (int i = 0; i < componentClasses.length; i++) {
            componentMap.put(componentClasses[i], i);
        }
    }

    @Override
    public int classToIndex(Class<?> componentClass) {
        return componentMap.getInt(componentClass);
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
