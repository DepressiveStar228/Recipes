package com.example.recipes.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipes.Adapter.CustomSpinnerAdapter;
import com.example.recipes.Adapter.IngredientGetAdapter;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RandDishFragment extends Fragment {
    private Random random = new Random();
    private TextView dish, recipe;
    private Button rand_button;
    private Spinner collectionsSpinner;
    private RecyclerView ingredientRecyclerView;
    private IngredientGetAdapter ingredientGetAdapter;
    private String currentNameCollection;
    private RecipeUtils utils;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PerferencesController perferencesController = new PerferencesController();
        perferencesController.loadPreferences(getContext());
        utils = new RecipeUtils(getContext());
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.rand_dish_activity, container, false);
        loadItemsActivity(view);
        loadCollection();
        loadClickListeners();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("RandDishFragment", "Фрагмент успішно закритий");
    }

    private void loadItemsActivity(View view){
        dish = view.findViewById(R.id.nameDishTextView);
        recipe = view.findViewById(R.id.randRecipeTextView);
        rand_button = view.findViewById(R.id.getRandDishButton);

        ingredientRecyclerView = view.findViewById(R.id.ingredientRecyclerView);
        ingredientGetAdapter = new IngredientGetAdapter(new ArrayList<>());
        ingredientRecyclerView.setAdapter(ingredientGetAdapter);
        ingredientRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        collectionsSpinner = view.findViewById(R.id.collections_spinner);

        rand_button.setOnClickListener(this::onClickGetDish);
    }

    private void loadClickListeners() {
        collectionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    currentNameCollection = (String) parent.getItemAtPosition(position);
                } else {
                    currentNameCollection = getString(R.string.system_collection_tag) + "All";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentNameCollection = getString(R.string.system_collection_tag) + "All";
            }
        });
    }

    public void onClickGetDish(View view) {
        if (Objects.equals(currentNameCollection, getString(R.string.system_collection_tag) + "All")) {
            Disposable disposable = utils.getDishesOrdered()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dishes -> {
                                if (dishes != null && !dishes.isEmpty()) {
                                    getDataDish(getRandomIndex(new ArrayList<>(dishes)));
                                    Log.d("RandDishFragment", "Успішне отримання списку страв.");
                                } else {
                                    Log.e("RandDishFragment", "Список страв пустий.");
                                    Toast.makeText(getContext(), getString(R.string.error_void_dishes), Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                Log.e("RandDishFragment", "Помилка отримання списку страв.", throwable);
                                Toast.makeText(getContext(), getString(R.string.error_get_dishes_by_db), Toast.LENGTH_SHORT).show();
                            }
                    );
            compositeDisposable.add(disposable);
        } else {
            Disposable disposable = utils.getIdCollectionByName(currentNameCollection)
                    .flatMap(
                            id -> {
                                if (id > 0) {
                                    Log.d("RandDishFragment", "Успішне отримання айді колекції.");
                                    return utils.getDishesByCollection(id);
                                } else {
                                    Log.e("RandDishFragment", "Помилка. Айді колекції нульовий.");
                                    return null;
                                }
                            },
                            throwable -> {
                                Log.e("RandDishFragment", "Помилка отримання айді колекції з бд.", throwable);
                                return null;
                            }
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dishes -> {
                                if (dishes != null && !dishes.isEmpty()) {
                                    getDataDish(getRandomIndex(new ArrayList<>(dishes)));
                                    Log.d("RandDishFragment", "Успішне отримання списку страв в колекції.");
                                } else {
                                    Log.e("RandDishFragment", "Помилка. Список страв в колекції пустий.");
                                    Toast.makeText(getContext(), getString(R.string.error_void_dishes), Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                Log.e("RandDishFragment", "Помилка отримання списку страв в колекції з бд.", throwable);
                                Toast.makeText(getContext(), getString(R.string.error_get_dishes_by_db), Toast.LENGTH_SHORT).show();
                            }
                    );

            compositeDisposable.add(disposable);
        }
    }

    private void getDataDish(Dish randDish) {
        dish.setText(randDish.getName());
        loadIngredients(randDish);
    }

    private Dish getRandomIndex(ArrayList<Dish> dishes) {
        return dishes.get(random.nextInt(dishes.size()));
    }

    private void loadIngredients(Dish randDish) {
        if (randDish != null && !randDish.getRecipe().isEmpty()){
            recipe.setVisibility(View.VISIBLE);
        }

        Disposable disposable = utils.getIngredients(randDish.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        ingredients -> {
                            ingredients.add(0, (new Ingredient(getString(R.string.ingredients), "", "")));
                            ingredients.add((new Ingredient(" ", "", "")));
                            recipe.setText(getString(R.string.recipe) + "\n" + randDish.getRecipe());

                            if (ingredients != null && !ingredients.isEmpty()) {
                                ingredientGetAdapter.clear();
                                ingredientGetAdapter.addAll(new ArrayList<>(ingredients));
                                Log.d("RandDishFragment", "Успішне отримання списку інгредієнтів для вибраної страви.");
                            } else {
                                Log.d("RandDishFragment", "Список інгредієнтів поточної страви пустий.");
                                Toast.makeText(getContext(), getString(R.string.error_get_ingredients), Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Log.e("RandDishFragment", "Помилка отримання списку інгредієнтів для поточної страви з бд.", throwable);
                            Toast.makeText(getContext(), getString(R.string.error_get_ingredients_by_db), Toast.LENGTH_SHORT).show();
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void loadCollection() {
        ArrayList<String> nameCollections = new ArrayList<>();
        nameCollections.add(getString(R.string.all));

        Disposable disposable =  utils.getAllNameCollections()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allNameCollection -> {
                            if (allNameCollection != null) {
                                nameCollections.addAll(allNameCollection);
                                Log.d("RandDishFragment", "Успішне отримання списку назв колекцій.");
                            }

                            if (!nameCollections.isEmpty()){
                                ArrayAdapter<String> themeAdapter = new CustomSpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, nameCollections);
                                themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                collectionsSpinner.setAdapter(themeAdapter);
                            }
                        },
                        throwable -> {
                            Log.e("RandDishFragment", "Помилка отримання списку назв всіх колекцій з бд.", throwable);
                        }
                );

        compositeDisposable.add(disposable);
    }
}
