package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Item.Collection;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Інтерфейс DAO для роботи з таблицею "collection" у базі даних.
 */
@Dao
public interface CollectionDAO {
    String TABLE_NAME = "collection";
    String ID = "id";
    String NAME = "name";
    String TYPE = "type";

    @Insert
    Single<Long> insert(Collection collection);

    @Update
    Completable update(Collection collection);

    @Delete
    Completable delete(Collection collection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id ")
    Maybe<Collection> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id ")
    LiveData<Collection> getByIDLive(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Maybe<Collection> getByName(String name);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Maybe<Long> getIDByName(String name);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<Collection>> getAll();

    @Query("SELECT " + NAME + " FROM " + TABLE_NAME)
    Single<List<String>> getAllName();

    @Query("SELECT " + NAME + " FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    Single<List<String>> getAllNameByType(CollectionType type);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    Single<List<Collection>> getAllByType(CollectionType type);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    LiveData<List<Collection>> getAllByTypeLive(CollectionType type);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name AND " + TYPE + " = :type")
    Maybe<Long> getIDByNameAndType(String name, CollectionType type);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TYPE + " =:type")
    Single<Integer> getCountByType(CollectionType type);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TYPE + " =:type")
    LiveData<Integer> getCountByTypeLive(CollectionType type);
}
