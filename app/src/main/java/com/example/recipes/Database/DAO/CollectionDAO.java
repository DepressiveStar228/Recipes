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
    Maybe<Collection> getCollectionById(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Maybe<Collection> getCollectionByName(String name);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Maybe<Long> getIdByName(String name);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<Collection>> getAllCollections();

    @Query("SELECT " + NAME + " FROM " + TABLE_NAME)
    Single<List<String>> getAllNameCollections();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE + " = :type")
    Single<List<Collection>> getAllCollectionsByType(String type);

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name AND " + TYPE + " = :type")
    Maybe<Long> getIdCollectionByNameAndType(String name, String type);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getCollectionCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + TYPE + " =:type")
    Single<Integer> getTypeCollectionCount(String type);
}
