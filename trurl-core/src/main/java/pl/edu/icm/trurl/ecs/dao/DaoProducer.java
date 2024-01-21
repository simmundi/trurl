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
import net.snowyhollows.bento.BentoFactory;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.ecs.dao.annotation.GwtIncompatible;

public class DaoProducer {

    private final Bento bento;

    @WithFactory
    public DaoProducer(Bento bento) {
        this.bento = bento;
    }

    public DaoProducer() {
        this.bento = Bento.createRoot();
    }

    @GwtIncompatible
    public <T> Dao<T> createDao(Class<T> clazz) {
        return createDao(clazz, "");
    }

    @GwtIncompatible
    public <T> Dao<T> createDao(Class<T> clazz, String daoPrefix) {
        return createDao(createDaoFactory(clazz), daoPrefix);
    }

    public <T> Dao<T> createDao(BentoFactory<? extends Dao<T>> factory) {
        return createDao(factory, "");
    }

    public <T> Dao<T> createDao(BentoFactory<? extends Dao<T>> factory, String daoPrefix) {
        Bento child = bento.create();
        child.register("daoPrefix", daoPrefix);
        return child.get(factory);
    }

    @GwtIncompatible
    public <T> BentoFactory<Dao<T>> createDaoFactory(Class<T> clazz) {
        String qname = clazz.getName();
        String packageName = qname.lastIndexOf('.') == -1 ? "" : qname.substring(0, qname.lastIndexOf('.'));
        String daoOfName = packageName + ".DaoOf" + clazz.getSimpleName();
        try {
            return (BentoFactory<Dao<T>>) Class.forName(daoOfName + "Factory")
                    .getField("IT")
                    .get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Class " + clazz + " does not have a valid dao (did you forget the @WithDao annotation? is trurl-generator configured as an annotation processor?)", e);
        }
    }


}
