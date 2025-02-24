package com.example.recipes.Item;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.bumptech.glide.load.engine.Resource;
import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Interface.SelectableItem;
import com.example.recipes.R;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.Resources;

import java.util.ArrayList;
import java.util.Objects;

@Entity(
        tableName = "dish",
        indices = {@Index("id")}
)
public class Dish implements SelectableItem {
    @PrimaryKey(autoGenerate = true) private long id;
    @ColumnInfo(name = "name") private String name = "";
    @ColumnInfo(name = "portion") private int portion = 0;
    @ColumnInfo(name = "timestamp") private long timestamp;
    @Ignore private ArrayList<Ingredient> ingredients = new ArrayList<>();
    @Ignore private ArrayList<DishRecipe> recipes = new ArrayList<>();

    @Ignore
    public Dish(long id, String name, String recipe, int portion, long timestamp){
        this.id = id;
        this.name = name;
        this.portion = portion;
        this.timestamp = timestamp;
    }

    @Ignore
    public Dish(Dish dish){
        this.id = dish.getId();
        this.name = dish.getName();
        this.portion = dish.getPortion();
        this.timestamp = dish.getTimestamp();
        this.ingredients.addAll(dish.getIngredients());
        this.recipes.addAll(dish.getRecipes());
    }

    @Ignore
    public Dish(long id, String name, int portion){
        this.id = id;
        this.name = name;
        this.portion = portion;
        this.timestamp = System.currentTimeMillis();
    }

    @Ignore
    public Dish(long id, String name, int portion, ArrayList<Ingredient> ingredients, ArrayList<DishRecipe> recipes){
        this.id = id;
        this.name = name;
        this.portion = portion;
        this.ingredients.addAll(ingredients);
        this.recipes.addAll(recipes);
        this.timestamp = System.currentTimeMillis();
    }

    @Ignore
    public Dish(String name){
        this.name = name;
        this.timestamp = System.currentTimeMillis();
    }

    public Dish(String name, int portion){
        this.name = name;
        this.portion = portion;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public ArrayList<String> getRecipeText() {
        ArrayList<String> recipeText = new ArrayList<>();

        for (DishRecipe dishRecipe : recipes) {
            if (dishRecipe.getTypeData() == DishRecipeType.TEXT) recipeText.add(dishRecipe.getTextData());
        }

        return recipeText;
    }

    public int getPortion() {
        return portion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public ArrayList<DishRecipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(ArrayList<DishRecipe> recipes) {
        this.recipes = recipes;
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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }

    public void addRecipe(DishRecipe dishRecipe) {
        recipes.add(dishRecipe);
    }

    public String getAsText(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(name + "\n\n").append(context.getString(R.string.portionship) + ": " + portion + "\n");
        builder.append(context.getString(R.string.ingredients) + ":\n");
        for (Ingredient ingredient : ingredients) {
            String ingredientText = ingredient.getName() + "  " +
                    ingredient.getAmount() + ingredient.getType() + '\n';
            builder.append(ingredientText);
        }
        builder.append("\n");
        builder.append(context.getString(R.string.recipe) + ":\n");
        for (String recipe : getRecipeText()) builder.append(recipe + " ");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Dish dish = (Dish) obj;

        if (id != dish.id) return false;
        if (!name.equals(dish.name)) return false;
        if (portion == dish.portion) return false;
        if (timestamp != dish.timestamp) return false;
        return recipes == dish.recipes;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + portion;
        result = 31 * result + Long.hashCode(timestamp);
        for (DishRecipe dishRecipe : recipes) result = 31 * result + dishRecipe.hashCode();
        return result;
    }

}
