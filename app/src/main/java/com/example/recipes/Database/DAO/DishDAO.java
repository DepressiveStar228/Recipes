package com.example.recipes.Database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.recipes.Item.Dish;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DishDAO {
    String TABLE_NAME = "dish";
    String ID = "id";
    String NAME = "name";
    String RECIPE = "recipe";

    @Insert
    Single<Long> insert(Dish dish);

    @Update
    Completable update(Dish dish);

    @Delete
    Completable delete(Dish dish);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<Dish>> getAllDishes();

    @Query("SELECT * FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC")
    Single<List<Dish>> getAllDishesOrdered();

    @Query("SELECT " + NAME + " FROM " + TABLE_NAME)
    Single<List<String>> getAllNameDishes();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id_dish")
    Maybe<Dish> getDishById(long id_dish);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name_dish")
    Maybe<Long> getIdByName(String name_dish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + NAME + " = :name_dish")
    Maybe<Dish> getDishByName(String name_dish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getDishCount();
}
