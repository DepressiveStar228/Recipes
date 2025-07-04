package com.example.recipes.Database;

import android.util.Pair;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.example.recipes.Database.DAO.DishDAO;
import com.example.recipes.Item.Dish;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class DishSortAndFilters {
    private final DishDAO dao;
    private Boolean sortByName = null;
    private Boolean sortByCreationTime = null;
    private Pair<Long, Long> cookingTimeRange = new Pair<>(0L, Long.MAX_VALUE);
    private ArrayList<String> usedIngredients = new ArrayList<>();
    private ArrayList<String> skipIngredients = new ArrayList<>();

    public DishSortAndFilters(DishDAO dao) {
        this.dao = dao;
    }

    public Single<List<Dish>> getResult() {
        String DISH_TABLE_NAME = "dish";

        StringBuilder query = new StringBuilder();
        query.append("SELECT d.* FROM " + DISH_TABLE_NAME + " d ");

        if (isAbleToFilterUsedIngredients() || isAbleToFilterSkipIngredients()) {
            addFilterPart(query);
        } else {
            if (isAbleToFilterCreationTimeRange()) {
                query.append(" WHERE ");
                addFilterCookingTimeRange(query);
            }
        }

        addSortPart(query);

        return dao.getWithFiltersAndSorting(new SimpleSQLiteQuery(query.toString())).switchIfEmpty(Single.just(new ArrayList<>()));
    }

    private void addFilterPart(StringBuilder query) {
        String INGREDIENT_TABLE_NAME = "ingredient";
        query.append("LEFT JOIN " + INGREDIENT_TABLE_NAME + " ing ON d.id = ing.id_dish ");
        query.append("GROUP BY d.id HAVING ");

        addFilterUsedIngredients(query);
        addAND(query, isAbleToFilterUsedIngredients() && isAbleToFilterSkipIngredients());
        addFilterSkipIngredients(query);
        addAND(query, isAbleToFilterCreationTimeRange() || isAbleToFilterSkipIngredients());
        addFilterCookingTimeRange(query);
    }

    private void addFilterUsedIngredients(StringBuilder query) {
        if (isAbleToFilterUsedIngredients()) {
            query.append("SUM(CASE WHEN ing.name IN ('");
            for (int i = 0; i < usedIngredients.size(); i++) {
                query.append(usedIngredients.get(i));
                if (i < usedIngredients.size() - 1) query.append("', '");
            }
            query.append("') THEN 1 ELSE 0 END) = ").append(usedIngredients.size());
        }
    }

    private void addFilterSkipIngredients(StringBuilder query) {
        if (isAbleToFilterSkipIngredients()) {
            query.append("SUM(CASE WHEN ing.name IN ('");
            for (int i = 0; i < skipIngredients.size(); i++) {
                query.append(skipIngredients.get(i));
                if (i < skipIngredients.size() - 1) query.append("', '");
            }
            query.append("') THEN 1 ELSE 0 END) = 0");
        }
    }

    private void addFilterCookingTimeRange(StringBuilder query) {
        if (cookingTimeRange != null) {
            query.append("d.cooking_time BETWEEN ").append(cookingTimeRange.first).append(" AND ").append(cookingTimeRange.second);
        }
    }

    private void addSortPart(StringBuilder query) {
        if (sortByName != null || sortByCreationTime != null) {
            query.append(" ORDER BY ");

            if (sortByCreationTime != null) {
                query.append("d.creation_time ").append(sortByCreationTime ? "DESC" : "ASC").append(", ");
            }

            if (sortByName != null) {
                query.append("d.name ").append(sortByName ? "ASC" : "DESC");
            } else {
                query.setLength(query.length() - 2);
            }
        }
    }

    private void addAND(StringBuilder query, boolean condition) {
        if (condition) query.append(" AND ");

    }

    private boolean isAbleToFilterUsedIngredients() {
        return (usedIngredients != null && !usedIngredients.isEmpty());
    }

    private boolean isAbleToFilterSkipIngredients() {
        return (skipIngredients != null && !skipIngredients.isEmpty());
    }

    private boolean isAbleToFilterCreationTimeRange() {
        return (cookingTimeRange != null && cookingTimeRange.first != null && cookingTimeRange.second != null);
    }

    public Boolean isSortByName() {
        return sortByName;
    }

    public void enableSortByName(Boolean sortByName) {
        this.sortByName = sortByName;
    }

    public Boolean isSortByCreationTime() {
        return sortByCreationTime;
    }

    public void enableSortByCreationTime(Boolean sortByCreationTime) {
        this.sortByCreationTime = sortByCreationTime;
    }

    public Pair<Long, Long> getCookingTimeRange() {
        return cookingTimeRange;
    }

    public void setCookingTimeRange(Pair<Long, Long> cookingTimeRange) {
        this.cookingTimeRange = cookingTimeRange;
    }

    public void setCookingTimeRange(Long start, Long end) {
        this.cookingTimeRange = new Pair<>(start, end);
    }

    public ArrayList<String> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(ArrayList<String> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public ArrayList<String> getSkipIngredients() {
        return skipIngredients;
    }

    public void setSkipIngredients(ArrayList<String> skipIngredients) {
        this.skipIngredients = skipIngredients;
    }
}
