package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activity.EditorDishActivity;
import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
import com.example.recipes.Adapter.AddChooseObjectsAdapter;
import com.example.recipes.Adapter.CollectionGetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.FileUtils;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CollectionsDishFragment extends Fragment implements OnBackPressedListener {
    private TextView counter_dishes;
    private PerferencesController perferencesController;
    private RecyclerView collectionsRecyclerView;
    private ImageView addCollectionButton;
    private ArrayList<Collection> collections;
    private CollectionGetAdapter adapter;
    private String[] themeArray;
    private Map<String, Runnable> functionMap;
    private RecipeUtils utils;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(getContext());
        utils = new RecipeUtils(getContext());
        compositeDisposable = new CompositeDisposable();
        collections = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.collections_page, container, false);
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
        updateCollectionRecyclerView();
        updateCounterDishes();
        updateCollections();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("CollectionsDishFragment", "Фрагмент успішно закритий");
    }

    private void loadItemsActivity(View view) {
        collectionsRecyclerView = view.findViewById(R.id.collections_dishRecyclerView);
        addCollectionButton = view.findViewById(R.id.add_collection_button);
        counter_dishes = view.findViewById(R.id.counter_dishes_collectionAct);

        themeArray = perferencesController.getStringArrayForLocale(R.array.theme_options,"en");
        Log.d("CollectionsDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
        if (collections != null) { updateCollectionRecyclerView(); }
        addCollectionButton.setOnClickListener(v -> { showAddCollectionDialog(); });

        Log.d("CollectionsDishFragment", "Слухачі фрагмента успішно завантажені.");
    }

    private void updateCollectionRecyclerView() {
        adapter = new CollectionGetAdapter(getContext(), collections, new CollectionGetAdapter.CollectionClickListener() {
            @Override
            public void onCollectionClick(Collection collection, RecyclerView childRecyclerView) {
                if (childRecyclerView.getVisibility() == View.GONE) {
                    childRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    childRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onDishClick(Dish dish, View v) {
                Intent intent = new Intent(v.getContext(), ReadDataDishActivity.class);
                intent.putExtra(Config.KEY_DISH, dish.getId());
                v.getContext().startActivity(intent);
            }

            @Override
            public void onDishMenuIconClick(Dish dish, Collection collection, View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), v, Gravity.END);
                popupMenu.getMenuInflater().inflate(R.menu.context_menu_dish, popupMenu.getMenu());

                for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                    MenuItem item = popupMenu.getMenu().getItem(i);
                    SpannableString spannableString = new SpannableString(item.getTitle());
                    if (!Objects.equals(perferencesController.getTheme(), themeArray[0])) {
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, spannableString.length(), 0);
                    } else {
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.black)), 0, spannableString.length(), 0);
                    }
                    item.setTitle(spannableString);
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit_dish) {
                        handleEditDishAction(dish);
                        return true;
                    } else if (item.getItemId() == R.id.action_remove_dish) {
                        handleRemoveDishAction(collection, dish);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_dish) {
                        handleDeleteDishAction(dish);
                        return true;
                    } else if (item.getItemId() == R.id.action_copy_as_text_dish) {
                        handleCopyAsTextDishAction(dish);
                        return true;
                    } else if (item.getItemId() == R.id.action_share_dish) {
                        handleShareDishAction(dish);
                        return true;
                    } else if (item.getItemId() == R.id.action_add_in_collection_dish) {
                        handleAddInCollectionDishAction(dish);
                        return true;
                    }  else {
                        return false;
                    }
                });

                popupMenu.show();
            }

            @Override
            public void onMenuIconClick(Collection collection, View anchorView) {
                final boolean isSystemCollection = Objects.equals(collection.getName(), getString(R.string.favorites)) ||
                        Objects.equals(collection.getName(), getString(R.string.my_recipes)) ||
                        Objects.equals(collection.getName(), getString(R.string.gpt_recipes)) ||
                        Objects.equals(collection.getName(), getString(R.string.import_recipes));

                PopupMenu popupMenu = new PopupMenu(getContext(), anchorView, Gravity.END);
                if (isSystemCollection) {
                    popupMenu.getMenuInflater().inflate(R.menu.context_system_menu_collection, popupMenu.getMenu());
                } else {
                    popupMenu.getMenuInflater().inflate(R.menu.context_menu_collection, popupMenu.getMenu());
                }

                for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                    MenuItem item = popupMenu.getMenu().getItem(i);
                    SpannableString spannableString = new SpannableString(item.getTitle());
                    if (!Objects.equals(perferencesController.getTheme(), themeArray[0])) {
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, spannableString.length(), 0);
                    } else {
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.black)), 0, spannableString.length(), 0);
                    }
                    item.setTitle(spannableString);
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        handleEditNameCollectionAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_only_collection) {
                        handleDeleteCollectionAction(collection, false);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_collection_with_dishes) {
                        handleDeleteCollectionAction(collection, true);
                        return true;
                    } else if (item.getItemId() == R.id.action_share) {
                        handleShareCollectionAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_clear_collection) {
                        handleClearCollectionAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_copy_another_collection) {
                        handleCopyCollectionAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_add_dishes_in_collection) {
                        handleAddDishesToCollectionAction(collection);
                        return true;
                    } else {
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
        Log.d("CollectionsDishFragment", "Адаптер колекцій успішно створено");
        collectionsRecyclerView.setAdapter(adapter);
        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.notifyDataSetChanged();
    }

    public void updateCounterDishes() {
        counter_dishes.setText(R.string.loading);

        Disposable disposable = utils.getDishCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(numb -> {
                            counter_dishes.setText(String.valueOf(numb));
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання кількості страв");
                        }
                );
        compositeDisposable.add(disposable);
    }

    public void updateCollections() {
        Disposable disposable = utils.getAllCollectionsByType(Config.COLLECTION_TYPE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allCollections -> {
                            collections = (ArrayList<Collection>) allCollections;
                            updateCollectionRecyclerView();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання всіх колекцій");
                        }
                );

        compositeDisposable.add(disposable);
        Log.d("CollectionsDishFragment", "Колекції фрагмента успішно завантажені");
    }

    private void handleEditNameCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.edit_collection_name_editText);
        editText.setText(collection.getName());
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, Config.CHAR_LIMIT_NAME_COLLECTION);
        builder.setView(dialogView)
                .setTitle(R.string.edit_collection)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    }

                    Disposable disposable = utils.getIdCollectionByName(collectionName)
                            .flatMap(id -> {
                                        if (id != -1) {
                                            Toast.makeText(getContext(), R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                            return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                        } else {
                                            collection.setName(collectionName);
                                            return utils.updateCollection(collection).toSingleDefault(collection);
                                        }
                                    },
                                    throwable -> {
                                        Log.d("CollectionsDishFragment", "Помилка виконання запиту отримання айді колекції за ім'ям");
                                        return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                    }
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    updatedCollection -> {
                                        Toast.makeText(getContext(), R.string.successful_edit_collection, Toast.LENGTH_SHORT).show();

                                        int index = collections.indexOf(collection);
                                        if (index != -1) {
                                            collections.set(index, updatedCollection);
                                            adapter.notifyItemChanged(index);
                                        }
                                    },
                                    throwable -> {
                                        if (throwable.getMessage() != null && throwable.getMessage().equals(getString(R.string.warning_dublicate_name_collection))) {
                                            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void handleDeleteCollectionAction(Collection collection, boolean mode) {
        String message;
        if (!mode) { message = getString(R.string.warning_delete_collection); }
        else { message = getString(R.string.warning_delete_collection_with_dishes); }

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.deleteCollection(collection, mode)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        adapter.delCollection(collection);
                                        updateCounterDishes();
                                        updateCollections();
                                        Toast.makeText(getContext(), getContext().getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        Toast.makeText(getContext(), getContext().getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                        Log.e("CollectionsDishFragment", "Помилка видалення колекції: " + throwable.getMessage());
                                    });

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleShareCollectionAction(Collection collection) {
        if (!collection.getDishes().isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        ImportExportController.exportRecipeData(getContext(), collection, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(getContext(), uri);
                                    FileUtils.deleteFileByUri(getContext(), uri);
                                    Toast.makeText(getContext(), getContext().getString(R.string.successful_export) + uri, Toast.LENGTH_LONG).show();
                                    Log.d("CollectionsDishFragment", "Рецепти успішно експортовані");
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(getContext(), getContext().getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e("CollectionsDishFragment", "Помилка експорту", throwable);
                            }

                            @Override
                            public void getDisposable(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        });
                    })
                    .setNegativeButton(getString(R.string.no), null).show();
        } else {
            Toast.makeText(getContext(), R.string.error_empty_collection, Toast.LENGTH_SHORT).show();
            Log.d("CollectionsDishFragment", "Помилка. Колекція порожня");
        }
    }

    private void handleClearCollectionAction(Collection collection) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_clear_collection))
                .setMessage(getString(R.string.warning_clear_collection))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.deleteDishCollectionData(collection)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        updateCollections();
                                        Toast.makeText(getContext(), R.string.successful_clear_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d("CollectionsDishFragment", "Колекція успішно очищена");
                                    },
                                    throwable -> {
                                        Log.e("CollectionsDishFragment", "Помилка очищення колекції");
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleCopyCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);

        Disposable disposable = utils.getAllCollectionsByType(Config.COLLECTION_TYPE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allCollections -> {
                            allCollections.remove(collection);
                            AddDishToCollectionsAdapter adapter = new AddDishToCollectionsAdapter(getContext(), (ArrayList<Collection>) allCollections);
                            collectionsRecyclerView.setAdapter(adapter);
                            collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                        ArrayList<Long> selectedCollectionIds = adapter.getSelectedCollectionIds();
                                        if (!selectedCollectionIds.isEmpty()){
                                            Disposable disposable1 = utils.copyDishesToAnotherCollections(collection.getId(), selectedCollectionIds)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
                                                                    updateCollections();
                                                                    Toast.makeText(getContext(), getString(R.string.successful_copy_dishes), Toast.LENGTH_SHORT).show();
                                                                    Log.d("CollectionsDishFragment", "Страви успішно скопійовано до колекції(й)");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.d("CollectionsDishFragment", "Помилка копіювання страв до колекції(й)");
                                                            }
                                                    );

                                            compositeDisposable.add(disposable1);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання усіх колекцій");
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void handleAddDishesToCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items, null);
        TextView textView = dialogView.findViewById(R.id.textView22);
        if (textView != null) { textView.setText(R.string.your_dish); }
        RecyclerView dishesRecyclerView = dialogView.findViewById(R.id.items_check_RecyclerView);
        Disposable disposable = utils.getUnusedDishInCollection(collection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> Log.d("CollectionsDishFragment", "Started fetching unused dishes"))
                .doOnSuccess(unused_dishes -> Log.d("CollectionsDishFragment", "Fetched unused dishes: " + unused_dishes))
                .doOnError(throwable -> Log.e("CollectionsDishFragment", "Error fetching unused dishes", throwable))
                .subscribe(unused_dishes -> {
                            AddChooseObjectsAdapter adapter = new AddChooseObjectsAdapter(getContext(), (ArrayList) unused_dishes, new AddChooseObjectsAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(CheckBox checkBox, ArrayList<Object> selectedItem, Object item) {
                                    if (!checkBox.isChecked()) {
                                        selectedItem.add(((Dish)item).getId());
                                        checkBox.setChecked(true);
                                    } else {
                                        selectedItem.remove(((Dish)item).getId());
                                        checkBox.setChecked(false);
                                    }
                                }
                            });
                            dishesRecyclerView.setAdapter(adapter);
                            dishesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                        ArrayList<Object> selectedDishIds = adapter.getSelectedItem();
                                        if (!selectedDishIds.isEmpty()) {
                                            Disposable disposable1 = utils.getDishes(selectedDishIds)
                                                    .subscribeOn(Schedulers.io())
                                                    .flatMap(dishes -> utils.addDishCollectionData(dishes, collection.getId()))
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
                                                                    updateCollections();
                                                                    Toast.makeText(getContext(), getString(R.string.successful_add_dishes), Toast.LENGTH_SHORT).show();
                                                                    Log.d("ReadDataDishActivity", "Страви успішно додано до колекції (й)");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.d("CollectionsDishFragment", "Помилка додавання страв до колекції(й)");
                                                            }
                                                    );
                                            compositeDisposable.add(disposable1);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання страв, які не лежать в колекції");
                        }
                );

        compositeDisposable.add(disposable);
    }

    private void showAddCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.add_collection_name_editText);
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, Config.CHAR_LIMIT_NAME_COLLECTION);

        builder.setView(dialogView)
                .setTitle(R.string.add_collection)
                .setPositiveButton(R.string.button_add, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    } else {
                        Disposable disposable = utils.getIdCollectionByName(collectionName)
                                .flatMap(id -> {
                                            if (id != -1) {
                                                Toast.makeText(getContext(), R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                                return Single.just(null);
                                            } else {
                                                return Single.just(collectionName);
                                            }
                                        },
                                        throwable -> {
                                            Log.d("CollectionsDishFragment", "Помилка отримання айді колекції за ім'ям");
                                            return Single.just(null);
                                        }
                                )
                                .flatMap(name -> {
                                    if (name != null) {
                                        return utils.addCollection(new Collection(name, Config.COLLECTION_TYPE))
                                                .map(result -> new Pair<>(result, name));
                                    } else {
                                        return Single.just(new Pair<>(false, name));
                                    }
                                })
                                .flatMap(pair -> {
                                    if (pair.first) {
                                        Single<Collection> collectionSingle = utils.getCollectionByName(pair.second);
                                        if (collectionSingle == null) {
                                            return Single.just(new Collection("", Config.COLLECTION_TYPE, new ArrayList<>()));
                                        } else {
                                            return collectionSingle;
                                        }
                                    } else {
                                        return Single.just(new Collection("", Config.COLLECTION_TYPE, new ArrayList<>()));
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(collection -> {
                                            if (collection != null && collection.getId() != -1) {
                                                adapter.addCollection(collection);
                                                collectionsRecyclerView.scrollToPosition(collections.size() - 1);
                                                Toast.makeText(getContext(), R.string.successful_add_collection, Toast.LENGTH_SHORT).show();
                                                Log.d("CollectionsDishFragment", "Колекція успішно створена");
                                            } else {
                                                Log.e("CollectionsDishFragment", "Помилка створення колекції");
                                            }
                                        },
                                        throwable -> {
                                            Log.d("CollectionsDishFragment", "Помилка отримання колекції за ім'ям");
                                        }
                                );

                        compositeDisposable.add(disposable);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void handleEditDishAction(Dish dish) {
        Intent intent = new Intent(getContext(), EditorDishActivity.class);
        intent.putExtra(Config.KEY_DISH, dish.getId());
        startActivity(intent);
        updateCollectionRecyclerView();
    }

    private void handleRemoveDishAction(Collection collection, Dish dish) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_remove_dish))
                .setMessage(getString(R.string.warning_remove_dish))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.getDishCollection(dish.getId(), collection.getId())
                            .flatMapCompletable(dish_collection -> {
                                if (dish_collection.getId_dish() == 0 || dish_collection.getId_collection() == 0) {
                                    return Completable.error(new Throwable("Error. Dish_collection was not found"));
                                } else {
                                    return utils.deleteDishCollection(dish_collection);
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        updateCollections();
                                        Toast.makeText(getContext(), R.string.successful_remove_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d("CollectionsDishFragment", "Страва успішно прибрана з колекції");
                                    },
                                    throwable -> {
                                        Log.e("CollectionsDishFragment", "Помилка прибирання страви з колекції", throwable);
                                    }
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleDeleteDishAction(Dish dish) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_dish))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.deleteDish(dish)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        updateCounterDishes();
                                        updateCollections();
                                        Toast.makeText(getContext(), getString(R.string.successful_delete_dish), Toast.LENGTH_SHORT).show();
                                    },
                                    throwable -> {
                                        Toast.makeText(getContext(), getString(R.string.error_delete_dish), Toast.LENGTH_SHORT).show();
                                    }
                            );
                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleCopyAsTextDishAction(Dish dish) {
        Disposable disposable = utils.getIngredients(dish.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ingredients -> {
                            String ingredientsText = "";

                            for (Ingredient ingredient : ingredients) {
                                String ingredientText = ingredient.getName() + "  " +
                                        ingredient.getAmount() + ingredient.getType() + '\n';
                                ingredientsText = ingredientsText + ingredientText;
                            }

                            String text = dish.getName() + "\n\n" + getString(R.string.ingredients) + "\n" + ingredientsText + "\n" + getString(R.string.recipe) + "\n" + dish.getRecipe();

                            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label", text);
                            clipboard.setPrimaryClip(clip);

                            Toast.makeText(getContext(), getString(R.string.copy_clipboard_text), Toast.LENGTH_SHORT).show();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання інгредієнтів страви");
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void handleAddInCollectionDishAction(Dish dish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);
        Disposable disposable = utils.getUnusedCollectionInDish(dish)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused_collections -> {
                            AddDishToCollectionsAdapter adapter = new AddDishToCollectionsAdapter(getContext(), unused_collections);
                            collectionsRecyclerView.setAdapter(adapter);
                            collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_add, (dialog, which) -> {
                                        ArrayList<Long> selectedCollectionIds = adapter.getSelectedCollectionIds();
                                        if (!selectedCollectionIds.isEmpty()) {
                                            Disposable disposable1 = utils.addDishCollectionData(dish, selectedCollectionIds)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
                                                                    updateCollectionRecyclerView();
                                                                    Toast.makeText(getContext(), getString(R.string.successful_add_dish_in_collection), Toast.LENGTH_SHORT).show();
                                                                    Log.d("CollectionsDishFragment", "Страва успішно додана в колекцію(ї)");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.d("CollectionsDishFragment", "Помилка додавання страви в колекцію(ї)");
                                                            }
                                                    );
                                            compositeDisposable.add(disposable1);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання колекцій, в яких немає поточної страви");
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void handleShareDishAction(Dish dish) {
        if (dish != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        ImportExportController.exportDish(getContext(), dish, new ExportCallbackUri() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (uri != null) {
                                    FileUtils.sendFileByUri(getContext(), uri);
                                    FileUtils.deleteFileByUri(getContext(), uri);
                                    Log.d("CollectionsDishFragment", "Рецепт успішно експортовані");
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Toast.makeText(getContext(), getContext().getString(R.string.error_export), Toast.LENGTH_SHORT).show();
                                Log.e("ImportExportController", "Помилка при експорті рецептів", throwable);
                            }

                            @Override
                            public void getDisposable(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        });
                    })
                    .setNegativeButton(getString(R.string.no), null).show();
        } else {
            Toast.makeText(getContext(), R.string.error_read_get_dish, Toast.LENGTH_SHORT).show();
            Log.d("CollectionsDishFragment", "Помилка. Страва порожня");
        }
    }
}