package com.example.recipes.Database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recipes.Item.IngredientShopListAmountType;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Інтерфейс DAO для роботи з таблицею "ingredient_shop_list_amount_type" у базі даних.
 */
@Dao
public interface IngredientShopListAmountTypeDAO {
    String TABLE_NAME = "ingredient_shop_list_amount_type";
    String ID = "id";
    String AMOUNT = "amount";
    String TYPE = "type";
    String ID_INGREDIENT = "id_ingredient";
    String ID_DISH = "id_dish";

    @Insert
    Single<Long> insert(IngredientShopListAmountType amountType);

    @Update
    Completable update(IngredientShopListAmountType amountType);

    @Delete
    Completable delete(IngredientShopListAmountType amountType);

    @Query("SELECT * FROM " + TABLE_NAME)
    Single<List<IngredientShopListAmountType>> getAll();

    @Query("SELECT * FROM " + TABLE_NAME)
    LiveData<List<IngredientShopListAmountType>> getAllLive();

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID + " = :id")
    Maybe<IngredientShopListAmountType> getByID(long id);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :idIngredient")
    Maybe<List<IngredientShopListAmountType>> getByIDIngredient(long idIngredient);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish")
    Maybe<List<IngredientShopListAmountType>> getByIDDish(long idDish);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :idIngredient")
    LiveData<List<IngredientShopListAmountType>> getByIDIngredientLive(long idIngredient);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + ID_DISH + " = :idDish")
    LiveData<List<IngredientShopListAmountType>> getByIDDishLive(long idDish);

    @Query("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + ID_INGREDIENT + " = :idIngredient")
    LiveData<Integer> getCountByID(long idIngredient);
}
