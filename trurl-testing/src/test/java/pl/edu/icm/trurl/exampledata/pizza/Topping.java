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

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.Objects;
import java.util.Random;

@WithDao
public class Topping {
    private Ingredient ingredient;
    private float amount;

    public Topping() {
    }

    public Topping(Ingredient ingredient, float amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public static Topping of (Ingredient ingredient, float amount) {
        return new Topping(ingredient, amount);
    }
    
    public static Topping random() {
        Ingredient[] ingredients = Ingredient.values();
        Random random = new Random();
        return of(ingredients[random.nextInt(ingredients.length)], random.nextFloat() * 3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topping topping = (Topping) o;
        return Float.compare(amount, topping.amount) == 0 && ingredient == topping.ingredient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount);
    }
}
