package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface IngredientDAO {
    String TABLE_NAME = "ingredient";
    String ID = "id";
    String NAME = "name";
    String AMOUNT = "amount";
    String TYPE = "type";
    String ID_DISH = "id_dish";

    @Insert
    Single<Long> insert(Ingredient ingredient);

    @Update
    Completable update(Ingredient ingredient);

    @Delete
    Completable delete(Ingredient ingredient);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name AND " + ID_DISH + " = :id_dish")
    Maybe<Long> getIdByNameAndIdDish(String name, long id_dish);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<Ingredient>> getAllIngredients();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish")
    Single<List<Ingredient>> getAllIngredientsByIdDish(long id_dish);

    @Query("SELECT DISTINCT " + NAME + " FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC ")
    Single<List<String>> getNameIngredientsUnique();

    @Query("SELECT DISTINCT * FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC ")
    Single<List<Ingredient>> getAllIngredientsNameOrdered();

    @Query("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Single<List<Long>> getIdDishesByName(String name);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getIngredientCount();
}
