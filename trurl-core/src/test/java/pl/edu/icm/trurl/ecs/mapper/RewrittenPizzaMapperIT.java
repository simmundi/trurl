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
/*
package pl.edu.icm.trurl.ecs.mapper;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.pizza.*;
import pl.edu.icm.trurl.store.Store;


public class RewrittenPizzaMapperIT {
    Bento bento = Bento.createRoot();
    Store store = new Store(1000);
    Mappers mappers = bento.get(MappersFactory.IT);

    @Test
    public void testA() {
        RewrittenPizzaMapper mapper = new RewrittenPizzaMapper(mappers);

        mapper.configureAndAttach(store);

        Pizza pizza = new Pizza();
        pizza.getToppings().add(Topping.of(Ingredient.CORN, 0.4f));
        pizza.getToppings().add(Topping.of(Ingredient.HAM, 0.3f));
        pizza.getToppings().add(Topping.of(Ingredient.BACON, 0.2f));
        pizza.getToppings().add(Topping.of(Ingredient.ANCHOVIS, 5f));

        pizza.getOlives().add(Olive.of(OliveColor.BLACK, 4));
        pizza.getOlives().add(Olive.of(OliveColor.BLACK, 3));
        pizza.getOlives().add(Olive.of(OliveColor.BLACK, 6));

        int first = store.getCounter().next();
        mapper.save(pizza, first);
        mapper.save(pizza, store.getCounter().next());
        mapper.save(pizza, store.getCounter().next());
        mapper.save(pizza, store.getCounter().next());
        mapper.save(pizza, store.getCounter().next());

        pizza.getOlives().remove(0);
        pizza.getOlives().remove(0);
        pizza.getToppings().remove(0);
        pizza.getToppings().remove(0);

        mapper.save(pizza, first);

        Pizza pizza2 = mapper.createAndLoad(first);
        System.out.println("uuk");
    }
}


 */