package com.example.recipes.Item;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.recipes.Database.TypeConverter.IngredientTypeConverter;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Interface.Item;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 */
@Entity(
        tableName = "dish",
        indices = {@Index("id")}
)
public class Dish implements Item {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name = "";
    @ColumnInfo(name = "portion") private int portion = 0;
    @ColumnInfo(name = "cooking_time") private long cookingTime = 0;
    @ColumnInfo(name = "creation_time") private long creationTime;
    @Ignore private ArrayList<Ingredient> ingredients = new ArrayList<>();
    @Ignore private ArrayList<DishRecipe> recipes = new ArrayList<>();


    // Конструктори
    @Ignore
    public Dish(long id, String name, int portion, long creationTime) {
        this.id = id;
        this.name = name;
        this.portion = portion;
        this.creationTime = creationTime;
    }

    @Ignore
    public Dish(Dish dish) {
        if (dish != null) {
            this.id = dish.getId();
            this.name = dish.getName();
            this.portion = dish.getPortion();
            this.creationTime = dish.getCreationTime();
            this.cookingTime = dish.getCookingTime();
            this.ingredients.addAll(dish.getIngredients());
            this.recipes.addAll(dish.getRecipes());
        }
    }

    @Ignore
    public Dish(long id, String name, int portion) {
        this.id = id;
        this.name = name;
        this.portion = portion;
        this.creationTime = System.currentTimeMillis();
    }

    @Ignore
    public Dish(long id, String name, int portion, ArrayList<Ingredient> ingredients, ArrayList<DishRecipe> recipes, long cookingTime) {
        this.id = id;
        this.name = name;
        this.portion = portion;
        this.ingredients.addAll(ingredients);
        this.recipes.addAll(recipes);
        this.creationTime = System.currentTimeMillis();
        this.cookingTime = cookingTime;
    }

    @Ignore
    public Dish(String name, int portion, ArrayList<Ingredient> ingredients, ArrayList<DishRecipe> recipes, long creationTime) {
        this.name = name;
        this.portion = portion;
        this.ingredients.addAll(ingredients);
        this.recipes.addAll(recipes);
        this.creationTime = creationTime;
    }

    @Ignore
    public Dish(String name) {
        this.name = name;
        this.creationTime = System.currentTimeMillis();
    }

    public Dish(String name, int portion) {
        this.name = name;
        this.portion = portion;
        this.creationTime = System.currentTimeMillis();
    }


    // Геттери і сеттери
    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getPortion() {
        return portion;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getCookingTime() {
        return cookingTime;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public ArrayList<DishRecipe> getRecipes() {
        return recipes;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setPortion(int portion) {
        this.portion = portion;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setCookingTime(long cookingTime) {
        this.cookingTime = cookingTime;
    }

    public void setRecipes(ArrayList<DishRecipe> recipes) {
        this.recipes = recipes;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }


    // Інші методи
    public void addRecipe(DishRecipe dishRecipe) {
        recipes.add(dishRecipe);
    }


    /**
     * Отримаємо один текст, який збирається із всіх DishRecipe типа Text
     *
     * @return весь рецепт в одним рядком
     */
    public ArrayList<String> getRecipeText() {
        ArrayList<String> recipeText = new ArrayList<>();

        for (DishRecipe dishRecipe : recipes) {
            if (dishRecipe.getTypeData() == DishRecipeType.TEXT) recipeText.add(dishRecipe.getTextData());
        }

        return recipeText;
    }


    /**
     * Отримаємо повний текстовий опис всієї страви.
     *
     * @param context контекст активності. Потрібен для отримання текстових ресурсів
     * @return текстовий опис всієї страви
     */
    public String getAsText(Context context) {
        StringBuilder builder = new StringBuilder();
        if (portion > 0) {
            builder.append(name + "\n\n").append(context.getString(R.string.portionship) + ": " + portion + "\n\n");
        }
        builder.append(context.getString(R.string.ingredients) + ":\n");
        for (Ingredient ingredient : ingredients) {
            String ingredientText = " - " +  ingredient.getName() + "  " + ingredient.getAmount() + " " + IngredientTypeConverter.fromIngredientTypeBySettingLocale(ingredient.getType()) + '\n';
            builder.append(ingredientText);
        }
        builder.append("\n");
        builder.append(context.getString(R.string.recipe) + ":\n");
        for (String recipe : getRecipeText()) builder.append(recipe + " ");
        return builder.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Dish dish = (Dish) object;

        return id == dish.id
                && portion == dish.portion
                && Objects.equals(name, dish.name)
                && Objects.equals(ingredients, dish.ingredients)
                && Objects.equals(recipes, dish.recipes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, portion, creationTime, ingredients, recipes);
    }
}
