package com.example.recipes.Database.ViewModels;

import androidx.lifecycle.LiveData;

import com.example.recipes.Database.DAO.DishRecipeDAO;
import com.example.recipes.Item.DishRecipe;

import java.util.List;

public class DishRecipeViewModel {
    public final DishRecipeDAO dao;

    public DishRecipeViewModel(DishRecipeDAO dao) {
        this.dao = dao;
    }

    public LiveData<List<DishRecipe>> getAll() {
        return dao.getAll_Live();
    }

    public LiveData<DishRecipe> getByID(Long id_dish) { return dao.getByIDLive(id_dish); }

    public LiveData<List<DishRecipe>> getByDishID(Long id_dish) { return dao.getByDishIDLive(id_dish); }

    public LiveData<Integer> getCount() { return dao.getCountLive(); }
}
