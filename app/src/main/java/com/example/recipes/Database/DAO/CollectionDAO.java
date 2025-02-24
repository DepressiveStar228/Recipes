package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.recipes.Item.Collection;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

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
    Single<Collection> getById(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id ")
    LiveData<Collection> getByID_Live(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Single<Collection> getByName(String name);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Maybe<Long> getIdByName(String name);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<Collection>> getAll();

    @Query("SELECT " + NAME + " FROM " + TABLE_NAME)
    Single<List<String>> getAllName();

    @Query("SELECT " + NAME + " FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    Single<List<String>> getAllNameByType(String type);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    Single<List<Collection>> getAllByType(String type);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    LiveData<List<Collection>> getAllByTypeLive(String type);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name AND " + TYPE + " = :type")
    Maybe<Long> getIdByNameAndType(String name, String type);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TYPE + " =:type")
    Single<Integer> getCountByType(String type);
}
