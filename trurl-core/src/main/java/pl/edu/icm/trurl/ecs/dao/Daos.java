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

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;

public class Daos {

    private final Bento bento;

    @WithFactory
    public Daos(Bento bento) {
        this.bento = bento;
    }

    public Daos() {
        this.bento = Bento.createRoot();
    }

    public <T> Dao<T> create(Class<T> clazz) {
        return create(clazz, "");
    }


    public <T> Dao<T> create(Class<T> clazz, String daoPrefix) {
        try {
            Bento child = bento.create();
            child.register("daoPrefix", daoPrefix);
            return child.get(
                    Class.forName(clazz.getPackage().getName() + ".DaoOf" + clazz.getSimpleName() + "Factory")
                            .getField("IT")
                            .get(null));
        } catch (ReflectiveOperationException cause) {
            throw new IllegalArgumentException("Class " + clazz + " does not have a valid dao (did you forget the @WithDao annotation? is trurl-generator configured as an annotation processor?)", cause);
        }
    }
}
