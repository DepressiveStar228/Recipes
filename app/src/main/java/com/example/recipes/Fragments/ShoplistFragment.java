package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.AddChooseObjectsAdapter;
import com.example.recipes.Adapter.CustomSpinnerAdapter;
import com.example.recipes.Adapter.IngredientShopListGetAdapter;
import com.example.recipes.Adapter.ShopListGetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.Ingredient;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShoplistFragment extends Fragment implements OnBackPressedListener {
    private RecipeUtils utils;
    private PerferencesController perferencesController;
    private CompositeDisposable compositeDisposable;
    private TextView empty;
    private ConstraintLayout data_box;
    private RecyclerView shopListsRecyclerView;
    private ImageView addShopListButton;
    private ShopListGetAdapter adapter;
    private ArrayList<Collection> collections;

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
        View view = inflater.inflate(R.layout.shoplist_page, container, false);
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
        updateCollections();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }

    private void loadItemsActivity(View view){
        data_box = view.findViewById(R.id.data_box);
        addShopListButton = view.findViewById(R.id.add_shopListButton);

        if (data_box != null) {
            shopListsRecyclerView = data_box.findViewById(R.id.shop_listsRecyclerView);
            empty = data_box.findViewById(R.id.empty_textView);
        }
    }

    private void loadClickListeners() {
        addShopListButton.setOnClickListener(v -> { showAddShopListDialog(); });
    }

    public void updateCollections() {
        Disposable disposable = utils.getAllCollectionsByType(Config.SHOP_LIST_TYPE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allCollections -> {
                            if (!collections.equals(allCollections)) {
                                collections.clear();
                                collections.addAll(allCollections);
                            }
                            updateCollectionRecyclerView(collections);
                        },
                        throwable -> {
                            Log.d("CollectionsDishFragment", "Помилка отримання всіх колекцій");
                        }
                );

        compositeDisposable.add(disposable);
        Log.d("CollectionsDishFragment", "Колекції фрагмента успішно завантажені");
    }

    private void updateCollectionRecyclerView(ArrayList<Collection> collections_) {
        if (collections_ != null) {
            if (adapter == null) {
                adapter = new ShopListGetAdapter(
                        getContext(),
                        getViewLifecycleOwner(),
                        empty,
                        collections_,
                        new ShopListGetAdapter.CollectionClickListener() {
                            @Override
                            public void onCollectionClick(Collection collection) {
                                showShopListDialog(collection);
                            }

                            @Override
                            public void onMenuIconClick(Collection collection, View v) {
                                PopupMenu popupMenu = new PopupMenu(getContext(), v, Gravity.END);
                                popupMenu.getMenuInflater().inflate(R.menu.context_menu_shop_list, popupMenu.getMenu());
                                String[] themeArray = perferencesController.getStringArrayForLocale(R.array.theme_options,"en");

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
                                    } else if (item.getItemId() == R.id.action_delete) {
                                        new AlertDialog.Builder(getContext())
                                                .setTitle(getString(R.string.confirm_delete_dish))
                                                .setMessage(getString(R.string.warning_delete_shop_list))
                                                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                                                    Disposable disposable = utils.deleteCollection(collection, true)
                                                            .subscribeOn(Schedulers.newThread())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(
                                                                    () -> {
                                                                        handleClearIngredientsCollection(collection, false);
                                                                        adapter.delItem(shopListsRecyclerView, collection);
                                                                        Toast.makeText(getContext(), R.string.successful_delete_shop_list, Toast.LENGTH_SHORT).show();
                                                                    }
                                                            );

                                                    compositeDisposable.add(disposable);
                                                })
                                                .setNegativeButton(getString(R.string.no), null).show();
                                        return true;
                                    } else if (item.getItemId() == R.id.action_copy_as_text) {
                                        String text = "";
                                        text = text + getString(R.string.list) + " " + collection.getName() + "\n";

                                        for (IngredientShopList ing : collection.getIngredients()) {
                                            if (!ing.getIsBuy()) {
                                                text = text + "   - " + ing.getName() + ": " + ing.getAmount() + " " + ing.getType() + "\n";
                                            }
                                        }

                                        text.substring(0, text.length() - 1);

                                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", text);
                                        clipboard.setPrimaryClip(clip);

                                        return true;
                                    } else if (item.getItemId() == R.id.action_clear) {
                                        new AlertDialog.Builder(getContext())
                                                .setTitle(getString(R.string.confirm_clear_shop_list))
                                                .setMessage(getString(R.string.warning_clear_shop_list))
                                                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                                                    handleClearIngredientsCollection(collection, true);
                                                })
                                                .setNegativeButton(getString(R.string.no), null).show();
                                        return true;
                                    }  else {
                                        return false;
                                    }
                                });

                                popupMenu.show();
                            }
                        }
                );

                shopListsRecyclerView.setAdapter(adapter);
                shopListsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                shopListsRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(16));
                adapter.setItems(collections);
            } else {
                adapter.setItems(collections);
            }
        }
    }

    private void showAddShopListDialog() {
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
                        Disposable disposable = utils.generateUniqueNameForShopList()
                                .flatMap(name -> Single.just(new Pair<>(utils.addCollection(new Collection(name, Config.SHOP_LIST_TYPE)), name)))
                                .flatMap(pair -> pair.first
                                        .flatMap(success -> {
                                            if (success) {
                                                return utils.getCollectionByName(pair.second);
                                            } else {
                                                return Single.just(null);
                                            }
                                        }))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(collection_ -> {
                                    adapter.addItem(shopListsRecyclerView, collection_);
                                });
                        compositeDisposable.add(disposable);
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
                                            Log.d("ShoplistFragment", "Помилка отримання айді колекції за ім'ям");
                                            return Single.just(null);
                                        }
                                )
                                .flatMap(name -> {
                                    if (name != null) {
                                        return utils.addCollection(new Collection(name, Config.SHOP_LIST_TYPE))
                                                .map(result -> new Pair<>(result, name));
                                    } else {
                                        return Single.just(new Pair<>(false, name));
                                    }
                                })
                                .flatMap(pair -> {
                                    if (pair.first) {
                                        Single<Collection> collectionSingle = utils.getCollectionByName(pair.second);
                                        if (collectionSingle == null) {
                                            return Single.just(new Collection("", Config.SHOP_LIST_TYPE, new ArrayList<>()));
                                        } else {
                                            return collectionSingle;
                                        }
                                    } else {
                                        return Single.just(new Collection("", Config.SHOP_LIST_TYPE, new ArrayList<>()));
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(collection -> {
                                            if (collection != null && collection.getId() != -1) {
                                                adapter.addItem(shopListsRecyclerView, collection);
                                                Toast.makeText(getContext(), R.string.successful_add_collection, Toast.LENGTH_SHORT).show();
                                                Log.d("ShoplistFragment", "Колекція успішно створена");
                                            } else {
                                                Log.e("ShoplistFragment", "Помилка створення колекції");
                                            }
                                        },
                                        throwable -> {
                                            Log.d("ShoplistFragment", "Помилка отримання колекції за ім'ям");
                                        }
                                );

                        compositeDisposable.add(disposable);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showShopListDialog(Collection shopList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_shop_list, null);
        AlertDialog dialog = builder.setView(dialogView).create();

        TextView name = dialogView.findViewById(R.id.nameShopList);
        TextView bought_items = dialogView.findViewById(R.id.bought_items);
        TextView all_item = dialogView.findViewById(R.id.all_item);
        RecyclerView ingShopListInDialog = dialogView.findViewById(R.id.ingShopListInDialog);
        ImageView addIngredient = dialogView.findViewById(R.id.addIngredientShopListButton);
        ImageView addDish = dialogView.findViewById(R.id.addDishShopListButton);
        ImageView exit = dialogView.findViewById(R.id.exitButton);

        if (name != null) { name.setText(shopList.getName()); }
        if (bought_items != null) {
            utils.ByIngredientShopList().getViewModel().getBoughtIngredientShopListCountByIdCollection(shopList.getId()).observe(this, data -> {
                if (data != null) {
                    bought_items.setText(data.toString());
                }
            });
        }
        if (all_item != null) {
            utils.ByIngredientShopList().getViewModel().getIngredientShopListCountByIdCollection(shopList.getId()).observe(this, data -> {
                if (data != null) {
                    all_item.setText(data.toString());

                    Disposable disposable = utils.ByIngredientShopList().getIngredientsShopListByIdCollection(shopList.getId())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(ingredientShopLists -> {
                                if (ingredientShopLists != null) {
                                    shopList.setIngredients((ArrayList<IngredientShopList>) ingredientShopLists);

                                    Handler handler = new Handler(Looper.getMainLooper());
                                    handler.postDelayed(() -> {
                                        IngredientShopListGetAdapter ingShopListAdapter = (IngredientShopListGetAdapter) ingShopListInDialog.getAdapter();
                                        if (ingShopListAdapter != null) {
                                            ingShopListAdapter.setItems(shopList.getIngredients());
                                        }
                                    }, 250);
                                }
                            });
                    compositeDisposable.add(disposable);
                }
            });
        }
        if (ingShopListInDialog != null) {
            setIngredientAdapter(shopList, ingShopListInDialog);
        }
        if (addIngredient != null) {
            addIngredient.setOnClickListener(view -> {
                onAddIngredientButtonClick(shopList);
            });
        }
        if (addDish != null) {
            addDish.setOnClickListener(view -> {
                onAddDishButtonClick(shopList);
            });
        }
        if (exit != null) {
            exit.setOnClickListener(view -> { dialog.dismiss(); });
        }

        dialog.show();
    }

    private void setIngredientAdapter(Collection shopList, RecyclerView ingShopListInDialog) {
        if (ingShopListInDialog.getAdapter() == null) {
            IngredientShopListGetAdapter ingAdapter = new IngredientShopListGetAdapter(
                    getContext(),
                    shopList.getIngredients(),
                    new IngredientShopListGetAdapter.IngredientShopListClickListener() {
                        private AtomicBoolean flagAccess = new AtomicBoolean(true);

                        @Override
                        public void onIngredientShopListClick(IngredientShopList ingredientShopList, View view) {
                            if (flagAccess.get()) {
                                ingredientShopList.setIsBuy(!ingredientShopList.getIsBuy());
                                flagAccess.set(false);
                                Disposable disposable = utils.ByIngredientShopList().updateIngredientShopList(ingredientShopList)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> { flagAccess.set(true); });

                                compositeDisposable.add(disposable);
                            }
                        }

                        @Override
                        public void onDeleteClick(IngredientShopList ingredientShopList, View view) {
                            if (flagAccess.get()) {
                                flagAccess.set(false);
                                Disposable disposable = utils.ByIngredientShopList().deleteIngredientShopList(ingredientShopList)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(() -> { flagAccess.set(true); });

                                compositeDisposable.add(disposable);
                            }
                        }
                    }
            );

            ingShopListInDialog.setAdapter(ingAdapter);
            ingShopListInDialog.setLayoutManager(new LinearLayoutManager(getContext()));
            ingShopListInDialog.addItemDecoration(new VerticalSpaceItemDecoration(16));
            ingShopListInDialog.getAdapter().notifyDataSetChanged();
        } else {
            IngredientShopListGetAdapter ingAdapter = (IngredientShopListGetAdapter) ingShopListInDialog.getAdapter();

            if (ingAdapter != null) {
                ingAdapter.setItems(shopList.getIngredients());
            }
        }
    }

    private void handleEditNameCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.edit_collection_name_editText);
        editText.setText(collection.getName());
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, Config.CHAR_LIMIT_NAME_COLLECTION);
        builder.setView(dialogView)
                .setTitle(R.string.edit_shop_list)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();

                    if (collectionName.isEmpty()) {
                        Disposable disposable = utils.generateUniqueNameForShopList()
                                .flatMap(name -> {
                                    collection.setName(name);
                                    collection.setType(Config.SHOP_LIST_TYPE);
                                    return utils.updateAndGetCollection(collection);
                                })
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(collection_ -> {
                                    if (collection_ != null && collection_.getId() != -1) {
                                        int index = collections.indexOf(collection);
                                        if (index != -1) {
                                            collections.set(index, collection_);
                                            adapter.notifyItemChanged(index);
                                        }

                                        Toast.makeText(getContext(), R.string.successful_edit_collection, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
                                    }
                                });
                        compositeDisposable.add(disposable);
                    } else if (!collectionName.equals(collection.getName())) {
                        Disposable disposable = utils.getIdCollectionByNameAndType(collectionName, Config.SHOP_LIST_TYPE)
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
                                            Log.d("ShoplistFragment", "Помилка виконання запиту отримання айді колекції за ім'ям");
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
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    public void onAddIngredientButtonClick(Collection shopList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.set_ingredient_shop_list_item, null);
        EditText name = dialogView.findViewById(R.id.nameIngredientEditText);
        EditText amount = dialogView.findViewById(R.id.countIngredientEditText);
        Spinner type = dialogView.findViewById(R.id.spinnerTypeIngredient);

        if (name != null) { CharacterLimitTextWatcher.setCharacterLimit(getContext(), name, Config.CHAR_LIMIT_NAME_INGREDIENT); }
        if (amount != null) { CharacterLimitTextWatcher.setCharacterLimit(getContext(), amount, Config.CHAR_LIMIT_AMOUNT_INGREDIENT); }

        if (type != null) {
            ArrayAdapter<String> typeAdapter = new CustomSpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, Arrays.asList(getContext().getResources().getStringArray(R.array.options_array)));
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            type.setAdapter(typeAdapter);
        }

        IngredientShopList ing = new IngredientShopList();

        builder.setView(dialogView)
                .setPositiveButton(R.string.button_add, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

        Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (addButton != null) {
            addButton.setOnClickListener(v -> {
                if (name != null && amount != null && type != null) {
                    String name_ = name.getText().toString();
                    String amount_ = amount.getText().toString();
                    String type_ = type.getSelectedItem().toString();

                    if (!name_.isEmpty() && !amount_.isEmpty() && !type_.isEmpty()) {
                        ing.setName(name_);
                        ing.setAmount(amount_);
                        ing.setType(type_);

                        Disposable disposable = utils.ByIngredientShopList().addIngredientShopList(shopList.getId(), ing)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(id -> {
                                    if (id > 0) {
                                        dialog.dismiss();
                                    }
                                });

                        compositeDisposable.add(disposable);
                    } else {
                        Toast.makeText(getContext(), R.string.warning_set_all_data, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void onAddDishButtonClick(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items, null);
        TextView textView = dialogView.findViewById(R.id.textView22);
        if (textView != null) { textView.setText(R.string.your_dish); }
        RecyclerView dishesRecyclerView = dialogView.findViewById(R.id.items_check_RecyclerView);
        Disposable disposable = utils.getUnusedDishInCollection(collection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> Log.d("ShoplistFragment", "Started fetching unused dishes"))
                .doOnSuccess(unused_dishes -> Log.d("ShoplistFragment", "Fetched unused dishes: " + unused_dishes))
                .doOnError(throwable -> Log.e("ShoplistFragment", "Error fetching unused dishes", throwable))
                .subscribe(unused_dishes -> {
                            AddChooseObjectsAdapter adapterChooseObjects = new AddChooseObjectsAdapter(getContext(), (ArrayList) unused_dishes, (checkBox, selectedItem, item) -> {
                                if (!checkBox.isChecked()) {
                                    selectedItem.add(((Dish)item).getId());
                                    checkBox.setChecked(true);
                                } else {
                                    selectedItem.remove(((Dish)item).getId());
                                    checkBox.setChecked(false);
                                }
                            });
                            dishesRecyclerView.setAdapter(adapterChooseObjects);
                            dishesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                        ArrayList<Object> selectedDishIds = adapterChooseObjects.getSelectedItem();
                                        if (!selectedDishIds.isEmpty()) {
                                            @SuppressLint("NotifyDataSetChanged") Disposable disposable1 = utils.getDishes(selectedDishIds)
                                                    .flatMap(dishes -> {
                                                        collection.setDishes(dishes);
                                                        return getCollectionWithIngredientFromDishes(collection, dishes);
                                                    })
                                                    .subscribeOn(Schedulers.newThread())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(
                                                            collection_ -> {
                                                                if (!collection_.getName().isEmpty() && collection_.getId() != -1) {
                                                                    collection_.setType(Config.SHOP_LIST_TYPE);
                                                                    int index = collections.indexOf(collection_);
                                                                    if (index != -1) {
                                                                        collections.set(index, collection_);
                                                                        adapter.notifyItemChanged(index);
                                                                    }

                                                                    Toast.makeText(getContext(), getString(R.string.successful_add_dishes), Toast.LENGTH_SHORT).show();
                                                                    Log.d("ShoplistFragment", "Страви успішно додано до списку");
                                                                } else {
                                                                    Toast.makeText(getContext(), R.string.error_add_dish, Toast.LENGTH_SHORT).show();
                                                                    Log.d("ShoplistFragment", "Помилка додавання страв до колекції(й)");
                                                                }
                                                            },
                                                            throwable -> {
                                                                Log.e("ShoplistFragment", "Error occurred", throwable);
                                                                Toast.makeText(getContext(), R.string.error_add_dish, Toast.LENGTH_SHORT).show();
                                                            }
                                                    );

                                            compositeDisposable.add(disposable1);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                            builder.create().show();
                        },
                        throwable -> {
                            Log.d("ShoplistFragment", "Помилка отримання страв, які не лежать в колекції");
                        }
                );

        compositeDisposable.add(disposable);
    }

    private Single<ArrayList<IngredientShopList>> insertAndGetIngredientsShopList(Collection collection, ArrayList<IngredientShopList> ingredients) {
        return Observable.fromIterable(ingredients)
                .flatMapSingle(ingredientShopList -> utils.ByIngredientShopList().addIngredientShopList(collection.getId(), ingredientShopList)
                        .flatMap(id -> {
                            if (id > 0) {
                                return utils.ByIngredientShopList()
                                        .getIngredientsShopListById(id)
                                        .onErrorReturnItem(new IngredientShopList());
                            } else {
                                return Single.just(new IngredientShopList());
                            }
                        })
                )
                .collectInto(new ArrayList<>(), (allIngredients, getIng) -> {
                    if (!getIng.getName().isEmpty()) {
                        allIngredients.add(getIng);
                    }
                });
    }

    private Single<Collection> getCollectionWithIngredientFromDishes(Collection collection, ArrayList<Dish> dishes) {
        return Observable.fromIterable(dishes)
                .flatMapSingle(dish -> utils.getIngredients(dish.getId()))
                .collectInto(new ArrayList<Ingredient>(), ArrayList::addAll)
                .flatMap(ingredients -> utils.ByIngredientShopList().convertIngredientsToIngredientShopList(ingredients))
                .flatMap(ingredients -> insertAndGetIngredientsShopList(collection, ingredients))
                .flatMap(allIngredients -> {
                    collection.addIngredients(allIngredients);
                    return utils.updateAndGetCollection(collection);
                });
    }

    private void handleClearIngredientsCollection(Collection collection, boolean showToast) {
        Disposable disposable = utils.ByIngredientShopList().deleteIngredientShopList(collection.getIngredients())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    if (status) {
                        int index = collections.indexOf(collection);
                        collection.setIngredients(new ArrayList<>());
                        collection.setDishes(new ArrayList<>());
                        collections.set(index, collection);

                        if (showToast) {
                            Toast.makeText(getContext(), R.string.successful_clear_collerction, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        compositeDisposable.add(disposable);
    }
}
