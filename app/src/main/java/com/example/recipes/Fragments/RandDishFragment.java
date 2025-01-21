package com.example.recipes.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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

import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.Adapter.CustomSpinnerAdapter;
import com.example.recipes.Config;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RandDishFragment extends Fragment implements OnBackPressedListener {
    private Random random = new Random();
    private TextView dish_name;
    private ConstraintLayout dish_name_box;
    private Dish randDish;
    private Button rand_button;
    private Spinner collectionsSpinner;
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
        View view =  inflater.inflate(R.layout.rand_dish_page, container, false);
        loadItemsActivity(view);
        loadClickListeners();
        return view;
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCollection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("RandDishFragment", "Фрагмент успішно закритий");
    }

    private void loadItemsActivity(View view){
        dish_name_box = view.findViewById(R.id.name_rand_dish_box);
        if (dish_name_box != null) { dish_name = dish_name_box.findViewById(R.id.name_rand_dish); }
        else { dish_name = new TextView(getContext()); }
        rand_button = view.findViewById(R.id.getRandDishButton);
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

        dish_name_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ReadDataDishActivity.class);
                intent.putExtra(Config.KEY_DISH, randDish.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    public void onClickGetDish(View view) {
        if (Objects.equals(currentNameCollection, getString(R.string.system_collection_tag) + "All")) {
            Disposable disposable = utils.getAllDishes()
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
        dish_name.setText(randDish.getName());
        this.randDish = randDish;
    }

    private Dish getRandomIndex(ArrayList<Dish> dishes) {
        return dishes.get(random.nextInt(dishes.size()));
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
