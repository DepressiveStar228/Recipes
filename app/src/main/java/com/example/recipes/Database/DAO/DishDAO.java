package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.recipes.Item.Dish;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Інтерфейс DAO для роботи з таблицею "dish" у базі даних.
 */
@Dao
public interface DishDAO {
    String TABLE_NAME = "dish";
    String ID = "id";
    String NAME = "name";
    String PORTION = "portion";
    String TIMESTAMP = "timestamp";

    @Insert
    Single<Long> insert(Dish dish);

    @Update
    Completable update(Dish dish);

    @Delete
    Completable delete(Dish dish);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<Dish>> getAll();

    @Query("SELECT " + ID + " FROM " + TABLE_NAME)
    Single<List<Long>> getAllIDs();

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<Dish>> getAll_Live();

    @RawQuery(observedEntities = { Dish.class })
    Single<List<Dish>> getWithFiltersAndSorting(SimpleSQLiteQuery query);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :idDish")
    Maybe<Dish> getByID(long idDish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :idDish")
    LiveData<Dish> getByIDLive(long idDish);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :nameDish")
    Maybe<Long> getIDByName(String nameDish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + NAME + " = :nameDish")
    Maybe<Dish> getByName(String nameDish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    LiveData<Integer> getCountLive();
}
