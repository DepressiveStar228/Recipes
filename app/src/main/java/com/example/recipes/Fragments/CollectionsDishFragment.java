package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
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

import com.example.recipes.Activity.CollectionActivity;
import com.example.recipes.Adapter.CollectionGetAdapter;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Option.CollectionOptions;
import com.example.recipes.Utils.ClassUtils;
import com.example.recipes.Utils.Dialogues;
import com.example.recipes.Utils.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Фрагмент для відображення та управління колекціями страв.
 */
public class CollectionsDishFragment extends Fragment implements OnBackPressedListener {
    private TextView counter_dishes;
    private PreferencesController preferencesController;
    private RecyclerView collectionsRecyclerView;
    private ImageView addCollectionButton;
    private CollectionGetAdapter adapter;
    private String[] themeArray;
    private RecipeUtils utils;
    private Dialogues dialogues;
    private Disposable collectionDisposable;
    private ConstraintLayout empty;
    private CollectionOptions collectionOptions;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesController = PreferencesController.getInstance();
        utils = RecipeUtils.getInstance(getContext());
        compositeDisposable = new CompositeDisposable();
        collectionOptions = new CollectionOptions(requireActivity(), compositeDisposable);
        dialogues = new Dialogues(requireActivity());
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
        setCollectionAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        Log.d("CollectionsDishFragment", "Фрагмент успішно закритий");
    }

    /**
     * Ініціалізує UI елементи фрагмента
     * @param view кореневий view фрагмента
     */
    private void loadItemsActivity(View view) {
        collectionsRecyclerView = view.findViewById(R.id.collections_dishRecyclerView);
        addCollectionButton = view.findViewById(R.id.add_collection_button);
        counter_dishes = view.findViewById(R.id.counter_dishes_collectionAct);
        empty = view.findViewById(R.id.collectionEmpty);

        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
        Log.d("CollectionsDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    /**
     * Налаштовує слухачі подій для елементів UI
     */
    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners() {
        if (addCollectionButton != null) { addCollectionButton.setOnClickListener(v -> showAddCollectionDialog()); }

        utils.ByDish().getViewModel().getCount().observe(this, data -> {
            if (data != null && counter_dishes != null) {
                counter_dishes.setText(String.valueOf(data));
            }
        });

        Log.d("CollectionsDishFragment", "Слухачі фрагмента успішно завантажені.");
    }

    /**
     * Ініціалізує та налаштовує адаптер для списку колекцій
     */
    private void setCollectionAdapter() {
        if (adapter == null) {
            adapter = new CollectionGetAdapter(getContext(), empty, new CollectionGetAdapter.CollectionClickListener() {
                @Override
                public void onCollectionClick(Collection collection) {
                    Intent intent = new Intent(getContext(), CollectionActivity.class);
                    intent.putExtra(IntentKeys.COLLECTION_ID.name(), collection.getId());
                    startActivity(intent); // Відкриття Activity зі стравами колекції
                }

                @Override
                public void onImageViewClick(Collection collection, View anchorView) {
                    final boolean isSystemCollection = Objects.equals(collection.getName(), getString(R.string.favorites))
                            || Objects.equals(collection.getName(), getString(R.string.my_recipes))
                            || Objects.equals(collection.getName(), getString(R.string.gpt_recipes))
                            || Objects.equals(collection.getName(), getString(R.string.import_recipes));

                    PopupMenu popupMenu = new PopupMenu(getContext(), anchorView, Gravity.END);
                    if (isSystemCollection) {
                        popupMenu.getMenuInflater().inflate(R.menu.context_system_menu_collection, popupMenu.getMenu());
                    } else {
                        popupMenu.getMenuInflater().inflate(R.menu.context_menu_collection, popupMenu.getMenu());
                    }

                    for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                        MenuItem item = popupMenu.getMenu().getItem(i);
                        SpannableString spannableString = new SpannableString(item.getTitle());
                        if (!Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, spannableString.length(), 0);
                        } else {
                            spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.black)), 0, spannableString.length(), 0);
                        }
                        item.setTitle(spannableString);
                    }

                    // Обробляє кліки по пунктах контекстного меню
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_edit) {
                            collectionOptions.editNameCollection(collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_only_collection) {
                            collectionOptions.deleteCollectionOnly(collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_delete_collection_with_dishes) {
                            collectionOptions.deleteCollectionWithDish(collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_share) {
                            collectionOptions.shareCollection(collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_clear_collection) {
                            collectionOptions.clearCollection(collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_copy_another_collection) {
                            collectionOptions.copyToCollection(collection);
                            return true;
                        } else if (item.getItemId() == R.id.action_add_dishes_in_collection) {
                            collectionOptions.addToCollection(collection);
                            return true;
                        } else {
                            return false;
                        }
                    });

                    popupMenu.show();
                }
            });

            collectionsRecyclerView.setAdapter(adapter);
            collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Слухач на зміну кількості страв в БД
            utils.ByDish_Collection().getViewModel().getCount().observe(this, data -> {
                if (data != null) {
                    if (collectionDisposable != null) collectionDisposable.dispose();

                    collectionDisposable = utils.ByCollection().getAllByType(CollectionType.COLLECTION)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collections -> {
                                if (collections != null && adapter != null) {
                                    adapter.setItems(ClassUtils.getListOfType(collections, Collection.class));
                                }
                            });

                    compositeDisposable.add(collectionDisposable);
                }
            });

            // Слухач на зміну колекцій в БД
            utils.ByCollection().getViewModel().getAllByType(CollectionType.COLLECTION).observe(this, data -> {
                if (data != null && adapter != null) {
                    if (collectionDisposable != null) collectionDisposable.dispose();

                    collectionDisposable = utils.ByCollection().getAllByType(CollectionType.COLLECTION)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collections -> {
                                if (collections != null) {
                                    adapter.setItems(ClassUtils.getListOfType(collections, Collection.class));
                                }
                            });

                    compositeDisposable.add(collectionDisposable);
                }
            });
        }
    }

    /**
     * Показує діалогове вікно для створення нової колекції
     */
    private void showAddCollectionDialog() {
        if (dialogues != null) {
            dialogues.dialogSetStringParamCollection(Limits.MAX_CHAR_NAME_COLLECTION, collectionName -> {
                if (collectionName.isEmpty()) {
                    Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                } else {
                    // Перевірка на існування колекції з таким же ім'ям
                    Disposable disposable = utils.ByCollection().getIdByName(collectionName)
                            .flatMap(
                                    id -> {
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
                                    // Додавання колекції до БД
                                    return utils.ByCollection().add(new Collection(name, CollectionType.COLLECTION))
                                            .map(id -> new Pair<>((id > 0), name));
                                } else {
                                    return Single.just(new Pair<>(false, name));
                                }
                            })
                            .flatMap(pair -> {
                                if (pair.first) {
                                    Single<Collection> collectionSingle = utils.ByCollection().getByName(pair.second);
                                    if (collectionSingle == null) {
                                        return Single.just(new Collection("", CollectionType.COLLECTION, new ArrayList<>()));
                                    } else {
                                        return collectionSingle;
                                    }
                                } else {
                                    return Single.just(new Collection("", CollectionType.COLLECTION, new ArrayList<>()));
                                }
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(collection -> {
                                        if (collection != null && collection.getId() != -1) {
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
            }, R.string.add_collection, R.string.button_add);
        }
    }
}