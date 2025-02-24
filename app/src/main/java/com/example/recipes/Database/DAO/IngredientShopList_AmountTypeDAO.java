package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.Item.IngredientShopList_AmountType;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface IngredientShopList_AmountTypeDAO {
    String TABLE_NAME = "ingredient_shop_list_amount_type";
    String ID = "id";
    String AMOUNT = "amount";
    String TYPE = "type";
    String ID_INGREDIENT = "id_ingredient";
    String ID_DISH = "id_dish";

    @Insert
    Single<Long> insert(IngredientShopList_AmountType amountType);

    @Update
    Completable update(IngredientShopList_AmountType amountType);

    @Delete
    Completable delete(IngredientShopList_AmountType amountType);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<IngredientShopList_AmountType>> getAll();

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<IngredientShopList_AmountType>> getAllLive();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Single<IngredientShopList_AmountType> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :id_ingredient")
    Maybe<List<IngredientShopList_AmountType>> getByIDIngredient(long id_ingredient);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish")
    Single<List<IngredientShopList_AmountType>> getByIDDish(long id_dish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :id_ingredient")
    LiveData<List<IngredientShopList_AmountType>> getByIDIngredientLive(long id_ingredient);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :id_dish")
    LiveData<List<IngredientShopList_AmountType>> getByIDDishLive(long id_dish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :id_ingredient")
    LiveData<Integer> getCountByID(long id_ingredient);
}
