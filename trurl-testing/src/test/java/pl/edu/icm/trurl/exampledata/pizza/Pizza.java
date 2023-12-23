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

package pl.edu.icm.trurl.exampledata.pizza;

import pl.edu.icm.trurl.ecs.dao.annotation.CollectionType;
import pl.edu.icm.trurl.ecs.dao.annotation.MappedCollection;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WithDao
public class Pizza {
    @MappedCollection(margin = 2)
    List<Olive> olives = new ArrayList<>();
    @MappedCollection(collectionType = CollectionType.ARRAY_LIST)
    List<Topping> toppings = new ArrayList<>();

    public List<Olive> getOlives() {
        return olives;
    }

    public List<Topping> getToppings() {
        return toppings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pizza pizza = (Pizza) o;
        return Objects.equals(olives, pizza.olives) && Objects.equals(toppings, pizza.toppings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(olives, toppings);
    }
}
