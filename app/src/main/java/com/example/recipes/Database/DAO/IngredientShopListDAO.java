package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

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

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID+ " = :id")
    Single<IngredientShopList> getById(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION+ " = :id_collection AND " + NAME + " = :name")
    Maybe<IngredientShopList> getByNameAndIDCollection(String name, Long id_collection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection")
    Single<List<IngredientShopList>> getAllByIDCollection(long id_collection);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection")
    LiveData<List<IngredientShopList>> getAllByIDCollection_Live(long id_collection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection")
    LiveData<Integer> getCountByIdCollection_Live(long id_collection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection AND " + IS_BUY + " = 1")
    LiveData<Integer> getBoughtCountByIdCollection_Live(long id_collection);
}
