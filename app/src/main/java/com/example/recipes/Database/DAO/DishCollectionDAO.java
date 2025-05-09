package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.DishCollection;

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
    Single<Long> insert(DishCollection dishCollection);

    @Update
    Completable update(DishCollection dishCollection);

    @Delete
    Completable delete(DishCollection dishCollection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish AND " + ID_COLLECTION + " = :idCollection ")
    Maybe<DishCollection> getByIDDishAndIDCollection(long idDish, long idCollection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Maybe<DishCollection> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish")
    Maybe<List<DishCollection>> getByIDDish(long idDish);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<DishCollection>> getAll();

    @Query("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection ")
    Maybe<List<Long>> getAllIDsDishByIDCollection(long idCollection);

    @Query("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection ")
    LiveData<List<Long>> getAllDishIDsLive(long idCollection);

    @Query("SELECT " + ID_COLLECTION + " FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish ")
    Maybe<List<Long>> getAllIDsCollectionByIDDish(long idDish);

    @Query("SELECT " + ID_COLLECTION + " FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish ")
    LiveData<List<Long>> getAllIDsCollectionLive(long idDish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    LiveData<Integer> getCountLive();
}
