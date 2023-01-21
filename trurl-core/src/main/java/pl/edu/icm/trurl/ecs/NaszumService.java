/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

import java.util.*;

public abstract class NaszumService<T extends Naszum> {
    private final Bento bento;
    private final List<T> instancesList;
    private final Map<String, T> instancesMap = new HashMap<>();

    public NaszumService(Bento bento, String keyName, BentoFactory<T> tBentoFactory) {
        this.bento = bento;
        if(keyName == null){
            keyName = this.getClass().getCanonicalName();
        }
        List<T> instancesList = new ArrayList<>();
        this.instancesList = Collections.unmodifiableList(instancesList);
        String[] instances = bento.getString(keyName).split(",");
        for (int i = 0; i < instances.length; i++) {
            Bento newbento = bento.createWithPrefix(keyName + "." + instances[i] + ".");
            newbento.register("name", instances[i]);
            newbento.register("ordinal", i);
            T obj = newbento.get(tBentoFactory);
            instancesList.add(obj);
            instancesMap.put(instances[i], obj);
        }

    }
public NaszumService(Bento bento, BentoFactory<T> bentoFactory){
        this(bento, null, bentoFactory);
}
    public Map<String, T> getInstances() {
        return instancesMap;
    }

    public T getByName(String name) {
        return instancesMap.get(name);
    }

    public T getByOrdinal(int o) {
        return instancesList.get(o);
    }


}
