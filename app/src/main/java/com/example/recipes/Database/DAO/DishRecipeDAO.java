package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.DishRecipe;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Інтерфейс DAO для роботи з таблицею "dish_recipe" у базі даних.
 */
@Dao
public interface DishRecipeDAO {
    String TABLE_NAME = "dish_recipe";
    String ID = "id";
    String ID_DISH = "id_dish";
    String DATA = "data";
    String POSITION = "position";
    String TYPE_DATA = "type_data";

    @Insert
    Single<Long> insert(DishRecipe recipe);

    @Update
    Completable update(DishRecipe recipe);

    @Delete
    Completable delete(DishRecipe recipe);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<DishRecipe>> getAll();

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<DishRecipe>> getAll_Live();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Maybe<DishRecipe> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    LiveData<DishRecipe> getByIDLive(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish ORDER BY " + POSITION)
    Single<List<DishRecipe>> getByDishID(long idDish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish ORDER BY " + POSITION)
    LiveData<List<DishRecipe>> getByDishIDLive(long idDish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    LiveData<Integer> getCountLive();
}
