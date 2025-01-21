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

    @Query("SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name AND " + ID_DISH + " = :id_dish")
    Maybe<Long> getIdIngredientShopListByNameAndIdDish(String name, long id_dish);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<IngredientShopList>> getAllIngredientsShopList();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID+ " = :id")
    Single<IngredientShopList> getIngredientsShopListById(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish")
    Single<List<IngredientShopList>> getAllIngredientsShopListByIdDish(long id_dish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection")
    Single<List<IngredientShopList>> getAllIngredientsShopListByIdCollection(long id_collection);

    @Query("SELECT DISTINCT " + NAME + " FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC ")
    Single<List<String>> getNameIngredientsUnique();

    @Query("SELECT DISTINCT * FROM " + TABLE_NAME + " ORDER BY " + NAME + " ASC ")
    Single<List<IngredientShopList>> getAllIngredientsShopListNameOrdered();

    @Query("SELECT " + ID_DISH + " FROM " + TABLE_NAME + " WHERE " + NAME + " = :name ")
    Single<List<Long>> getIdDishesByNameIngredientShopList(String name);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME)
    Single<Integer> getIngredientShopListCount();

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection")
    LiveData<Integer> getIngredientShopListCountByIdCollection(long id_collection);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_COLLECTION + " = :id_collection AND " + IS_BUY + " = 1")
    LiveData<Integer> getBoughtIngredientShopListCountByIdCollection(long id_collection);
}
