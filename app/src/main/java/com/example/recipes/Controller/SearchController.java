package com.example.recipes.Controller;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.Interfaces.ChooseItem;
import com.example.recipes.Adapter.Interfaces.Search;
import com.example.recipes.Enum.SearchMode;
import com.example.recipes.Interface.Item;
import com.example.recipes.R;
import com.example.recipes.Utils.ClassUtils;
import com.jakewharton.rxbinding4.widget.RxTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для управління пошуком у додатку.
 * Відповідає за фільтрацію даних, відображення результатів пошуку та взаємодію з інтерфейсом користувача.
 */
public class SearchController<T> {
    private final Context context;
    private final ArrayList<T> arrayData;
    private final ArrayList<T> searchResults;
    private EditText searchEditText;
    private RecyclerView setSearchRecyclerView;
    private int sortMode = 0;
    private final RecyclerView.Adapter<?> adapter;
    private final AtomicBoolean flagAccessSort = new AtomicBoolean(true);
    private Disposable searchDisposable = Disposable.empty();
    private Disposable clearInputDisposable = Disposable.empty();

    /**
     * Конструктор для ініціалізації контролера з адаптером SearchResultsAdapter.
     *
     * @param context                   Контекст додатку.
     * @param searchEditText            Поле для введення пошукового запиту.
     * @param searchResultsRecyclerView RecyclerView для відображення результатів.
     * @param adapter                   Адаптер для списку.
     */
    public SearchController(Context context, EditText searchEditText, RecyclerView searchResultsRecyclerView, RecyclerView.Adapter<?> adapter) {
        this.context = context;
        this.arrayData = new ArrayList<>();
        this.searchEditText = searchEditText;
        this.searchResults = new ArrayList<>();
        this.adapter = adapter;

        initializeRecyclerView(searchResultsRecyclerView);
        search();
    }

    private void initializeRecyclerView(RecyclerView searchResultsRecyclerView) {
        this.setSearchRecyclerView = searchResultsRecyclerView;
        this.setSearchRecyclerView.setAdapter(adapter);
        this.setSearchRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        Log.d("SearchController", "RecyclerView initialized with adapter and layout manager.");
    }

    /**
     * Виконує пошук за поточним текстом у полі пошуку.
     */
    public void search() {
        if (searchEditText != null) {
            searchDisposable.dispose();
            searchDisposable = RxTextView.textChanges(searchEditText)
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .map(CharSequence::toString)
                    .distinctUntilChanged()
                    .switchMap(query -> Observable.fromCallable(() -> performSearch(query))
                            .subscribeOn(Schedulers.computation()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::updateResults, throwable -> Log.e("SearchController", "Search error", throwable));

            Log.d("SearchController", "The search field has been initialized.");
        }
    }

    /**
     * Виконує пошук за введеним текстом та повертає результати.
     *
     * @param data Текст для пошуку.
     * @return Список знайдених елементів.
     */
    public ArrayList<T> searchAndGetResult(String data) {
        ArrayList<T> result = performSearch(data);
        updateResults(result);
        Log.d("SearchController", "Search performed with query: " + data + ", found " + result.size() + " items.");

        return result;
    }

    /**
     * Виконує пошук у наборі даних за введеним запитом.
     *
     * @param searchQuery Пошуковий запит.
     * @return Список результатів пошуку.
     */
    private ArrayList<T> performSearch(String searchQuery) {
        ArrayList<T> result = new ArrayList<>();

        if (arrayData != null) {
            String query  = searchQuery.toLowerCase();

            for (T item : arrayData) {
                String searchBox = "";
                if (item instanceof Item) { searchBox = ((Item) item).getName().toLowerCase(); }
                else if (item instanceof String) { searchBox = item.toString().toLowerCase(); }

                if (searchBox.contains(query)) {
                    result.add(item); // Додавання елемента, якщо він відповідає запиту
                } else if (searchBox.isEmpty()) {
                    result.add(item); // Додавання елемента, якщо пошуковий запит порожній
                }
            }
        }
        Log.d("SearchController", "Search performed with query: " + searchQuery + ", found " + result.size() + " items.");

        return result;
    }

    /**
     * Оновлює результати пошуку у RecyclerView.
     *
     * @param searchResult Результати пошуку.
     */
    public void updateResults(ArrayList<T> searchResult) {
        searchResults.clear();
        searchResults.addAll(searchResult);
        if (adapter instanceof Search) ((Search<T>) adapter).setResultItems(searchResult);
        scrollToStart(); // Прокручуємо до початку списку після оновлення результатів
        Log.d("SearchController", "Results updated in RecyclerView.");
    }

    private void sortResults() {
        if (searchResults == null || searchResults.isEmpty()) return;

        sortMode++;

        if (adapter instanceof ChooseItem<?>) {
            switch (sortMode % 3) {
                case 2: // Сортування за вибраними
                    ArrayList<T> sortBySelectedItems = new ArrayList<>(searchResults);
                    ArrayList<T> selectedItems = ((ChooseItem<T>) adapter).getSelectedItem();

                    if (!selectedItems.isEmpty()) {
                        Collections.sort(sortBySelectedItems, (a, b) -> {
                            boolean aSelected = selectedItems.contains(a);
                            boolean bSelected = selectedItems.contains(b);

                            if (aSelected && !bSelected) return -1;
                            if (!aSelected && bSelected) return 1;
                            return 0;
                        });
                    } else {
                        Toast.makeText(context, R.string.empty_selected_items_for_sort, Toast.LENGTH_SHORT).show();
                        Log.w("SearchController", "No items selected to sort.");
                    }

                    ((ChooseItem<T>) adapter).setItems(sortBySelectedItems);
                    Log.d("SearchController", "Search results sorted by selected items.");
                    break;

                case 1: // Сортування за спаданням (A → Z)
                    sortAlgorithm(false);
                    ((ChooseItem<T>) adapter).setItems(searchResults);
                    Log.d("SearchController", "Search results sorted in descending order.");
                    break;

                case 0: // Сортування за зростанням (Z → A)
                    sortAlgorithm(true);
                    ((ChooseItem<T>) adapter).setItems(searchResults);
                    Log.d("SearchController", "Search results sorted in ascending order.");
                    break;

                default:
                    Log.w("SearchController", "Unknown sort mode: " + sortMode);
            }
        } else if (adapter instanceof Search<?>) {
            switch (sortMode % 2) {
                case 1: // Сортування за спаданням (Z → A)
                    sortAlgorithm(false);
                    ((Search<T>) adapter).setResultItems(searchResults);
                    Log.d("SearchController", "Search results sorted in descending order.");
                    break;

                case 0: // Сортування за зростанням (A → Z)
                    sortAlgorithm(true);
                    ((Search<T>) adapter).setResultItems(searchResults);
                    Log.d("SearchController", "Search results sorted in ascending order.");
                    break;

                default:
                    Log.d("SearchController", "Unknown sort mode: " + sortMode);
            }

            ((Search<T>) adapter).setResultItems(searchResults);
            adapter.notifyDataSetChanged();
            Log.d("SearchController", "Results sorted and adapter notified.");
        }
    }

    /**
     * Сортує результати пошуку за алфавітом.
     * Використовується для сортування рядків або об'єктів Item.
     *
     * @param mode Режим сортування: true - за зростанням, false - за спаданням.
     */
    private void sortAlgorithm(boolean mode) {
        if (searchResults == null || searchResults.isEmpty()) return;

        if (ClassUtils.isListOfType(searchResults, String.class)) {
            searchResults.sort((Comparator<T>) Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER));
        } else if (ClassUtils.isListOfType(searchResults, Item.class)) {
            searchResults.sort(Comparator.comparing(item -> ((Item) item).getName(), String.CASE_INSENSITIVE_ORDER));
        }

        if (!mode) {
            Collections.reverse(searchResults);
            Log.d("SearchController", "Search results sorted in descending order.");
        } else Log.d("SearchController", "Search results sorted in ascending order.");
    }


    /**
     * Встановлює новий набір даних для пошуку.
     *
     * @param arrayData Новий набір даних.
     */
    public void setArrayData(ArrayList<T> arrayData) {
        this.arrayData.clear();
        this.arrayData.addAll(arrayData);
        this.searchResults.clear();
        this.searchResults.addAll(arrayData);

        if (adapter instanceof Search) ((Search<T>) adapter).setResultItems(arrayData);
    }

    /**
     * Встановлює вибрані елементи у адаптері ChooseItemAdapter.
     *
     * @param arrayData Список вибраних елементів.
     */
    public void setArraySelectedData(ArrayList<T> arrayData) {
        if (adapter instanceof ChooseItem) ((ChooseItem<T>) adapter).setSelectedItems(arrayData);
    }

    /**
     * Встановлює нове поле для введення пошукового запиту.
     *
     * @param searchEditText Нове поле для введення пошукового запиту.
     */
    public void setSearchEditText(@NonNull EditText searchEditText) {
        if (searchEditText != null) {
            this.searchEditText = searchEditText;
            clear();
            search(); // Перезапускаємо спостереження за полем пошуку
            Log.d("SearchController", "Search EditText has been set.");
        }
        else Log.w("SearchController", "Search EditText is null, cannot set listener.");
    }

    /**
     * Встановлює новий RecyclerView для відображення результатів пошуку.
     *
     * @param setSearchRecyclerView Новий RecyclerView.
     */
    public void setSearchRecyclerView(@NonNull RecyclerView setSearchRecyclerView) {
        if (setSearchRecyclerView != null) initializeRecyclerView(setSearchRecyclerView);
    }

    /**
     * Встановлює індикатор порожнього результату пошуку.
     *
     * @param emptyIndicator Індикатор, який відображається, коли немає результатів пошуку.
     */
    public void setEmptyIndicator(ConstraintLayout emptyIndicator) {
        if (emptyIndicator != null) {
            if (searchResults.isEmpty()) emptyIndicator.setVisibility(ConstraintLayout.VISIBLE);
            else emptyIndicator.setVisibility(ConstraintLayout.GONE);
        } else {
            Log.d("SearchController", "Empty indicator is null, cannot set visibility.");
        }
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter;
    }

    public RecyclerView getRecyclerView() { return setSearchRecyclerView; }

    /**
     * Встановлює кнопку для очищення тексту у полі пошуку.
     *
     * @param clearSearchEditText Кнопка для очищення тексту.
     */
    public void setClearSearchEditText(AppCompatImageView clearSearchEditText) {
        if (clearSearchEditText != null) {
            // Додаємо обробник натискання на кнопку очищення
            clearSearchEditText.setOnClickListener(v -> {
                searchEditText.setText("");
                Log.d("SearchController", "Search EditText cleared.");
            });

            if (searchEditText != null) {
                clearInputDisposable.dispose();
                clearInputDisposable = RxTextView.textChanges(searchEditText)
                        .subscribe(charSequence -> {
                            if (charSequence.length() > 0) clearSearchEditText.setVisibility(AppCompatImageView.VISIBLE);
                            else clearSearchEditText.setVisibility(AppCompatImageView.GONE);
                        });
            } else {
                Log.d("SearchController", "Search EditText is null, cannot set text change listener.");
            }
        }
    }

    /**
     * Встановлює кнопку для сортування результатів пошуку.
     *
     * @param sortResultSearchButton Кнопка для сортування результатів.
     */
    public void setSortResultSearchButton(AppCompatImageView sortResultSearchButton) {
        if (sortResultSearchButton != null) {
            sortResultSearchButton.setOnClickListener(v -> {
                if (flagAccessSort.get()) {
                    flagAccessSort.set(false);
                    sortResults();
                    scrollToStart(); // Прокручуємо до початку списку після сортування
                    Log.d("SearchController", "Search results sorted.");
                }
            });
        }
    }

    /**
     * Прокручує RecyclerView до початку списку.
     * Використовується для повернення до початку результатів пошуку після сортування.
     */
    private void scrollToStart() {
        if (setSearchRecyclerView != null) {
            setSearchRecyclerView.postDelayed(() -> {
                flagAccessSort.set(true);
                getRecyclerView().smoothScrollToPosition(0); // Прокрутка до початку списку
                Log.d("SearchController", "RecyclerView scrolled to start.");
            }, 400);
        } else {
            Log.w("SearchController", "RecyclerView is null, cannot scroll to start.");
        }
    }

    /**
     * Очищає всі ресурси, пов'язані з контролером пошуку.
     * Використовується для звільнення пам'яті та уникнення витоків.
     */
    public void clear() {
        searchDisposable.dispose();
        clearInputDisposable.dispose();
        Log.d("SearchController", "SearchController cleared.");
    }

}
