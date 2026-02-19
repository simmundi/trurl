/*
 * Copyright (c) 2026 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import java.util.function.IntConsumer;

public class AttributeSource implements Source {
    private final Store substore;
    private final IntAttribute backreference;

    public AttributeSource(Store substore, IntAttribute backreference) {
        this.substore = substore;
        this.backreference = backreference;
    }

    @Override
    public void forEach(IntConsumer consumer) {
        int count = substore.getCounter().getCount();
        for (int i = 0; i < count; i++) {
            // TODO: remove this check
            if (!substore.isEmpty(i)) {
                int rootId = backreference.getInt(i);
                if (rootId != IntAttribute.NULL) {
                    consumer.accept(rootId);
                }
            }
        }
    }
}
