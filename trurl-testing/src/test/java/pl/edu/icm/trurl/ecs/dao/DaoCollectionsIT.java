/*
 * Copyright (c) 2022-2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.pizza.*;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DaoCollectionsIT {
    Dao<Pizza> dao;
    Store store = new Store(new ArrayAttributeFactory(), 1000);

    @BeforeEach
    void before() {
        dao = new Daos().create(Pizza.class);
        dao.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should configure the collection with margin of two olives")
    void save__margin() {
        // given
        Pizza pizzaA = twoOlivesOneTopping();
        Pizza pizzaB = twoOlivesOneTopping();
        Pizza pizzaC = twoOlivesOneTopping();

        // execute
        dao.save(pizzaA, 45);
        dao.save(pizzaB, 100);
        dao.save(pizzaC, 127);

        pizzaA.getOlives().add(Olive.of(OliveColor.BLACK, 1));
        dao.save(pizzaA, 45);

        pizzaB.getOlives().add(Olive.of(OliveColor.GREEN, 1));
        pizzaB.getOlives().add(Olive.of(OliveColor.BLACK, 2));
        dao.save(pizzaB, 100);

        pizzaC.getOlives().add(Olive.of(OliveColor.GREEN, 1));
        pizzaC.getOlives().add(Olive.of(OliveColor.BLACK, 2));
        pizzaC.getOlives().add(Olive.of(OliveColor.BLACK, 5));

        // assert
        assertThat(dao.createAndLoad(45).getOlives()).hasSize(3);
        assertThat(dao.createAndLoad(100).getOlives()).hasSize(4);
    }

    @Test
    @DisplayName("Should configure the collection with minimal size of 10 toppings")
    void save__minimal() {
        // given
        Pizza pizzaA = twoOlivesOneTopping();
        Pizza pizzaB = twoOlivesOneTopping();
        Pizza pizzaC = twoOlivesOneTopping();

        // execute
        dao.save(pizzaA, 45);
        dao.save(pizzaB, 100);
        dao.save(pizzaC, 100);

        while (pizzaA.getToppings().size() < 10) {
            pizzaA.getToppings().add(Topping.of(Ingredient.HAM, 1));
        }
        while (pizzaB.getToppings().size() < 15) {
            pizzaB.getToppings().add(Topping.of(Ingredient.CHEESE, 1));
        }
        while (pizzaC.getToppings().size() < 20) {
            pizzaC.getToppings().add(Topping.of(Ingredient.ANCHOVIS, 0.001f));
        }
        dao.save(pizzaA, 45);
        dao.save(pizzaB, 100);
        dao.save(pizzaC, 255);

        Pizza retrievedA = dao.createAndLoad(45);
        Pizza retrievedB = dao.createAndLoad(100);
        Pizza retrievedC = dao.createAndLoad(255);

        assertThat(retrievedA.getToppings()).hasSize(10);
        assertThat(retrievedB.getToppings()).hasSize(15);
        assertThat(retrievedC.getToppings()).hasSize(20);
        assertThat(retrievedC.getOlives()).hasSize(2);
    }

    private Pizza twoOlivesOneTopping() {
        Pizza pizza = new Pizza();
        pizza.getOlives().add(Olive.random());
        pizza.getOlives().add(Olive.random());
        pizza.getToppings().add(Topping.random());
        return pizza;
    }
}
