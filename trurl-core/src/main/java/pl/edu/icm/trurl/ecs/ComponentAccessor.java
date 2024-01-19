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

package pl.edu.icm.trurl.ecs;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;

/**
 * In multiple places in the API the data of different components is indexed by an integer
 * number, for speed. A single instance of component accessor used by the engine is the
 * single source of the truth to translate between class objects and their index, and vice-versa.
 *
 */
@ImplementationSwitch(configKey = "trurl.engine.component-accessor", cases = {
        @ImplementationSwitch.When(name = "dynamic", implementation = DynamicComponentAccessor.class, useByDefault = true)
})
public interface ComponentAccessor {
    int classToIndex(Class<?> componentClass);
    Class<?> indexToClass(int index);
    int componentCount();
}
