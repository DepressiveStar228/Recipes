package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.IngredientShopList;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Інтерфейс DAO для роботи з таблицею "ingredient_shop_list" у базі даних.
 */
@Dao
public interface IngredientShopListDAO {
    String TABLE_NAME = "ingredient_shop_list";
    String ID = "id";
    String NAME = "name";
    String AMOUNT = "amount";
    String TYPE = "type";
    String ID_DISH = "id_dish";
    String ID_COLLECTION = "id_collection";
    String IS_BUY = "is_buy";

    @Insert
    Single<Long> insert(IngredientShopList ingredient);

    @Update
    Completable update(IngredientShopList ingredient);

    @Delete
    Completable delete(IngredientShopList ingredient);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<IngredientShopList>> getAll();

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<IngredientShopList>> getAllLive();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Maybe<IngredientShopList> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection AND " + NAME + " = :name")
    Maybe<IngredientShopList> getByNameAndIDCollection(String name, long idCollection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection")
    Maybe<List<IngredientShopList>> getAllByIDCollection(long idCollection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection")
    LiveData<List<IngredientShopList>> getAllByIDCollectionLive(long idCollection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection")
    Single<Integer> getCountByIDShopList(long idCollection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection AND " + IS_BUY + " = 1")
    Single<Integer> getBoughtCountByIDShopList(long idCollection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection")
    LiveData<Integer> getCountByIDShopListLive(long idCollection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :idCollection AND " + IS_BUY + " = 1")
    LiveData<Integer> getBoughtCountByIDShopListLive(long idCollection);
}
