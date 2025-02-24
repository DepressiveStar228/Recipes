package com.example.recipes.Fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activity.ShopListActivity;
import com.example.recipes.Adapter.ShopListGetAdapter;
import com.example.recipes.Config;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Controller.VerticalSpaceItemDecoration;
import com.example.recipes.Decoration.SlideOutItemAnimator;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ShoplistFragment extends Fragment implements OnBackPressedListener {
    private RecipeUtils utils;
    private PreferencesController preferencesController;
    private CompositeDisposable compositeDisposable, compositeByIngredients;
    private TextView empty;
    private ConstraintLayout data_box;
    private RecyclerView shopListsRecyclerView;
    private ImageView addShopListButton;
    private ShopListGetAdapter adapter;
    private ArrayList<Collection> collections;
    private AtomicBoolean clearFlag = new AtomicBoolean(false);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(getContext());
        utils = new RecipeUtils(getContext());
        compositeDisposable = new CompositeDisposable();
        compositeByIngredients = new CompositeDisposable();
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
        compositeByIngredients.clear();
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
        Disposable disposable = utils.ByCollection().getAllByType(Config.SHOP_LIST_TYPE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        allCollections -> {
                            collections.clear();
                            collections.addAll(allCollections);
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
                                Intent intent = new Intent(getContext(), ShopListActivity.class);
                                intent.putExtra(Config.KEY_SHOP_LIST, collection.getId());
                                startActivity(intent);
                            }

                            @Override
                            public void onMenuIconClick(Collection collection, View v) {
                                PopupMenu popupMenu = new PopupMenu(getContext(), v, Gravity.END);
                                popupMenu.getMenuInflater().inflate(R.menu.context_menu_shop_list, popupMenu.getMenu());
                                String[] themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options,"en");

                                for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                                    MenuItem item = popupMenu.getMenu().getItem(i);
                                    SpannableString spannableString = new SpannableString(item.getTitle());

                                    if (!Objects.equals(preferencesController.getTheme(), themeArray[0])) {
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
                                    }
                                    else if (item.getItemId() == R.id.action_delete) {
                                        handleDeleteCollectionAction(collection);
                                        return true;
                                    }
                                    else if (item.getItemId() == R.id.action_copy_as_text) {
                                        String text = "";
                                        text = text + getString(R.string.list) + " " + collection.getName() + "\n";

                                        for (IngredientShopList ing : collection.getIngredients()) {
                                            if (!ing.getIsBuy()) {
                                                text = text + "   - " + ing.getName() + ": " + ing.getGroupedAmountTypeToString() + "\n";
                                            }
                                        }

                                        text.substring(0, text.length() - 1);

                                        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", text);
                                        clipboard.setPrimaryClip(clip);

                                        return true;
                                    }
                                    else if (item.getItemId() == R.id.action_clear) {
                                        new AlertDialog.Builder(getContext())
                                                .setTitle(getString(R.string.confirm_clear_shop_list))
                                                .setMessage(getString(R.string.warning_clear_shop_list))
                                                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                                                    handleClearIngredientsCollection(collection, true);
                                                })
                                                .setNegativeButton(getString(R.string.no), null).show();
                                        return true;
                                    }
                                    else { return false; }
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
                        Disposable disposable = utils.ByCollection().generateUniqueNameForShopList()
                                .flatMap(name -> Single.just(new Pair<>(utils.ByCollection().add(new Collection(name, Config.SHOP_LIST_TYPE)), name)))
                                .flatMap(pair -> pair.first
                                        .flatMap(id -> {
                                            if (id > 0) {
                                                return utils.ByCollection().getByName(pair.second);
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
                        Disposable disposable = utils.ByCollection().getIdByName(collectionName)
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
                                        return utils.ByCollection().add(new Collection(name, Config.SHOP_LIST_TYPE))
                                                .map(id -> new Pair<>((id > 0), name));
                                    } else {
                                        return Single.just(new Pair<>(false, name));
                                    }
                                })
                                .flatMap(pair -> {
                                    if (pair.first) {
                                        Single<Collection> collectionSingle = utils.ByCollection().getByName(pair.second);
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
                        Disposable disposable = utils.ByCollection().generateUniqueNameForShopList()
                                .flatMap(name -> {
                                    collection.setName(name);
                                    collection.setType(Config.SHOP_LIST_TYPE);
                                    return utils.ByCollection().updateAndGet(collection);
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
                        Disposable disposable = utils.ByCollection().getIdByNameAndType(collectionName, Config.SHOP_LIST_TYPE)
                                .flatMap(id -> {
                                            if (id != -1) {
                                                Toast.makeText(getContext(), R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                                                return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                            } else {
                                                collection.setName(collectionName);
                                                return utils.ByCollection().update(collection).toSingleDefault(collection);
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

    private void handleDeleteCollectionAction(Collection collection) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(getString(R.string.warning_delete_shop_list))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByCollection().delete(collection)
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
    }

    private void handleClearIngredientsCollection(Collection collection, boolean showToast) {
        clearFlag.set(true);

        Disposable disposable = utils.ByCollection().clear(collection)
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

                        clearFlag.set(false);
                    }
                });
        compositeDisposable.add(disposable);
    }
}
