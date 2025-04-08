package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import com.example.recipes.Activity.CollectionActivity;
import com.example.recipes.Adapter.AddDishToCollectionsAdapter;
import com.example.recipes.Adapter.ChooseItemAdapter;
import com.example.recipes.Adapter.CollectionGetAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Enum.CollectionType;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Interface.OnBackPressedListener;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Utils.ClassUtils;
import com.example.recipes.Utils.FileUtils;
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
    private Disposable collectionDisposable;
    private ImportExportController importExportController;
    private CompositeDisposable compositeDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesController = PreferencesController.getInstance();
        utils = RecipeUtils.getInstance(getContext());
        importExportController = new ImportExportController(getContext());
        compositeDisposable = new CompositeDisposable();
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

        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options,"en");
        Log.d("CollectionsDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    /**
     * Налаштовує слухачі подій для елементів UI
     */
    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
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
            adapter = new CollectionGetAdapter(getContext(), new CollectionGetAdapter.CollectionClickListener() {
                @Override
                public void onCollectionClick(Collection collection, RecyclerView childRecyclerView) {
                    Intent intent = new Intent(getContext(), CollectionActivity.class);
                    intent.putExtra(IntentKeys.COLLECTION_ID.name(), collection.getId());
                    startActivity(intent); // Відкриття Activity зі стравами колекції
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
     * Обробляє зміну назви колекції через діалогове вікно
     * @param collection колекція для редагування
     */
    private void handleEditNameCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.edit_collection_name_editText);
        editText.setText(collection.getName());

        // Встановлення обмеження на кількість символів
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, Limits.MAX_CHAR_NAME_COLLECTION);
        builder.setView(dialogView)
                .setTitle(R.string.edit_collection)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    }

                    // Перевірка на дублікат назви та оновлення колекції
                    Disposable disposable = utils.ByCollection().getIdByName(collectionName)
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
                                        Log.d("CollectionsDishFragment", "Помилка виконання запиту отримання айді колекції за ім'ям");
                                        return Single.error(new Exception(getString(R.string.warning_dublicate_name_collection)));
                                    }
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    updatedCollection -> Toast.makeText(getContext(), R.string.successful_edit_collection, Toast.LENGTH_SHORT).show(),
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

    /**
     * Обробляє видалення колекції з додатковими параметрами
     * @param collection колекція для видалення
     * @param mode режим видалення (true - з видаленням страв, false - тільки колекція)
     */
    private void handleDeleteCollectionAction(Collection collection, boolean mode) {
        String message;
        if (!mode) { message = getString(R.string.warning_delete_collection); }
        else { message = getString(R.string.warning_delete_collection_with_dishes); }

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    if (mode) {
                        Disposable disposable = utils.ByCollection().deleteWithDishes(collection)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {
                                            Toast.makeText(getContext(), getContext().getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                        },
                                        throwable -> {
                                            Toast.makeText(getContext(), getContext().getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                            Log.e("CollectionsDishFragment", "Помилка видалення колекції: " + throwable.getMessage());
                                        });

                        compositeDisposable.add(disposable);
                    } else {
                        Disposable disposable = utils.ByCollection().delete(collection)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {
                                            Toast.makeText(getContext(), getContext().getString(R.string.successful_delete_collection), Toast.LENGTH_SHORT).show();
                                        },
                                        throwable -> {
                                            Toast.makeText(getContext(), getContext().getString(R.string.error_delete_collection), Toast.LENGTH_SHORT).show();
                                            Log.e("CollectionsDishFragment", "Помилка видалення колекції: " + throwable.getMessage());
                                        });

                        compositeDisposable.add(disposable);
                    }
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Обробляє експорт колекції у зовнішній файл
     * @param collection колекція для експорту
     */
    private void handleShareCollectionAction(Collection collection) {
        if (!collection.getDishes().isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        importExportController.exportRecipeData(getContext(), collection, new ExportCallbackUri() {
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
                                Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.error_export), Toast.LENGTH_SHORT).show();
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

    /**
     * Обробляє очищення колекції (видалення всіх страв з колекції, а не з БД)
     * @param collection колекція, яку потрібно очистити
     */
    private void handleClearCollectionAction(Collection collection) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_clear_collection))
                .setMessage(getString(R.string.warning_clear_collection))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    Disposable disposable = utils.ByDish_Collection().deleteAllByIDCollection(collection.getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    status -> {
                                        Toast.makeText(getContext(), R.string.successful_clear_collerction, Toast.LENGTH_SHORT).show();
                                        Log.d("CollectionsDishFragment", "Колекція успішно очищена");
                                    },
                                    throwable -> Log.e("CollectionsDishFragment", "Помилка очищення колекції")
                            );

                    compositeDisposable.add(disposable);
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    /**
     * Обробляє копіювання страв з поточної колекції в інші колекції
     * @param collection колекція-джерело для копіювання
     */
    private void handleCopyCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);

        Disposable disposable = utils.ByCollection().getAllByType(CollectionType.COLLECTION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        listData -> {
                            ArrayList<Collection> allCollections = ClassUtils.getListOfType(listData, Collection.class);
                            allCollections.remove(collection); // Видаляємо поточну колекцію зі списку цілей

                            // Налаштування адаптера для списку колекцій
                            AddDishToCollectionsAdapter adapter = new AddDishToCollectionsAdapter(getContext(), allCollections);
                            collectionsRecyclerView.setAdapter(adapter);
                            collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                        // Обробка копіювання при натисканні кнопки
                                        ArrayList<Long> selectedCollectionIds = adapter.getSelectedCollectionIds();
                                        if (!selectedCollectionIds.isEmpty()){
                                            Disposable disposable1 = utils.ByDish_Collection().copyDishesToAnotherCollections(collection.getId(), selectedCollectionIds)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
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
                            Log.e("CollectionsDishFragment", "Помилка отримання усіх колекцій");
                        }
                );

        compositeDisposable.add(disposable);
    }

    /**
     * Обробляє додавання нових страв до колекції
     * @param collection колекція, до якої додаються страви
     */
    private void handleAddDishesToCollectionAction(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_items, null);

        // Налаштування заголовка діалогового вікна
        TextView textView = dialogView.findViewById(R.id.textView22);
        if (textView != null) { textView.setText(R.string.your_dish); }

        RecyclerView dishesRecyclerView = dialogView.findViewById(R.id.items_check_RecyclerView);

        // Отримання страв, які ще не додані до колекції
        Disposable disposable = utils.ByCollection().getUnusedDish(collection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused_dishes -> {
                            // Налаштування адаптера для списку страв
                            ChooseItemAdapter<Dish> adapter = new ChooseItemAdapter<>(getContext(), (checkBox, item) -> {});
                            adapter.setItems(unused_dishes);
                            dishesRecyclerView.setAdapter(adapter);
                            dishesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            builder.setView(dialogView)
                                    .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                                        // Додавання вибраних страв до колекції
                                        ArrayList<Dish> selectedDish = adapter.getSelectedItem();
                                        ArrayList<Long> selectedDishLongIds = new ArrayList<>();
                                        for (Dish dish : selectedDish) {
                                            selectedDishLongIds.add(dish.getId());
                                        }

                                        if (!selectedDish.isEmpty()) {
                                            Disposable disposable1 = utils.ByDish_Collection().addAllWithCheckExist(selectedDish, collection.getId())
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(status -> {
                                                                if (status) {
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

    /**
     * Показує діалогове вікно для створення нової колекції
     */
    private void showAddCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.add_collection_name_editText);

        // Встановлення обмеження на довжину назви колекції
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, Limits.MAX_CHAR_NAME_COLLECTION);

        builder.setView(dialogView)
                .setTitle(R.string.add_collection)
                .setPositiveButton(R.string.button_add, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    } else {
                        // Перевірка на існування колекції з таким же ім'ям
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
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}