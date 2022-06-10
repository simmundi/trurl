package pl.edu.icm.trurl.exampledata.pizza;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Random;

@WithMapper
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
}
