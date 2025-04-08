package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.recipes.Item.Dish_Collection;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Інтерфейс DAO для роботи з таблицею "dish_collection" у базі даних.
 */
@Dao
public interface DishCollectionDAO {
    String TABLE_NAME = "dish_collection";
    String ID = "id";
    String ID_DISH = "id_dish";
    String ID_COLLECTION = "id_collection";

    @Insert
    Single<Long> insert(Dish_Collection dish_collection);

    @Update
    Completable update(Dish_Collection dish_collection);

    @Delete
    Completable delete(Dish_Collection dish_collection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish AND " + ID_COLLECTION + " = :id_collection ")
    Maybe<Dish_Collection> getByIDDishAndIDCollection(long id_dish, long id_collection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Maybe<Dish_Collection> getByID(long id);

    @Query("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection ")
    Single<List<Long>> getAllIDsDishByIDCollection(long id_collection);

    @Query("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection ")
    LiveData<List<Long>> getAllDishIDsLive(long id_collection);

    @Query("SELECT " + ID_COLLECTION + " FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish ")
    Single<List<Long>> getAllIDsCollectionByIDDish(long id_dish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    LiveData<Integer> getCountLive();
}
