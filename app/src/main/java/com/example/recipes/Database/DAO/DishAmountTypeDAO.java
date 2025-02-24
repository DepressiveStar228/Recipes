package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.DishAmountType;
import com.example.recipes.Item.Dish_Collection;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DishAmountTypeDAO {
    String TABLE_NAME = "dish_amount_type";
    String ID = "id";
    String ID_DISH = "id_dish";
    String ID_INGREDIENT = "id_ingredient";

    @Insert
    Single<Long> insert(DishAmountType dishAmountType);

    @Update
    Completable update(Dish_Collection dish_collection);

    @Delete
    Completable delete(Dish_Collection dish_collection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish AND " + ID_INGREDIENT + " = :id_ingredient ")
    Single<DishAmountType> getByIDDishAndIDIngredient(long id_dish, long id_ingredient);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Single<DishAmountType> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :id_ingredient ")
    Single<List<Long>> getAllByIDIngredient(long id_ingredient);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :id_ingredient ")
    LiveData<List<Long>> getAllIDsLive(long id_ingredient);

    @Query("SELECT " + ID_INGREDIENT + " FROM " + TABLE_NAME + " WHERE " + ID + " = :id ")
    Single<List<Long>> getAllIDsIngredientByID(long id);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();
}
