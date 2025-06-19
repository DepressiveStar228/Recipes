package com.example.recipes.Controller;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.Interfaces.ChooseItem;
import com.example.recipes.Adapter.Interfaces.Search;
import com.example.recipes.Enum.Limits;
import com.example.recipes.Interface.Item;
import com.example.recipes.R;
import com.example.recipes.Utils.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для управління пошуком у додатку.
 * Відповідає за фільтрацію даних, відображення результатів пошуку та взаємодію з інтерфейсом користувача.
 */
public class SearchController<T> {
    private Context context;
    private ArrayList<T> arrayData, searchResults;
    private int sortMode = 0;
    private EditText searchEditText;
    private AppCompatImageView clearSearchEditText, sortResultSearchButton;
    private RecyclerView.Adapter<?> adapter;
    private RecyclerView searchResultsRecyclerView;
    private AtomicBoolean flagAccessSort = new AtomicBoolean(true);

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
        setListener();
    }

    private void initializeRecyclerView(RecyclerView searchResultsRecyclerView) {
        this.searchResultsRecyclerView = searchResultsRecyclerView;
        this.searchResultsRecyclerView.setAdapter(adapter);
        this.searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    /**
     * Встановлює слухача для поля пошуку.
     */
    private void setListener() {
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Очищення результатів перед зміною тексту
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    search(); // Виконання пошуку при зміні тексту
                }

                @Override
                public void afterTextChanged(Editable s) {
                    adapter.notifyDataSetChanged(); // Оновлення адаптера після зміни тексту

                    // Показати або приховати кнопку очищення тексту
                    if (clearSearchEditText != null) {
                        if (s.length() > 0) {
                            clearSearchEditText.setVisibility(AppCompatImageView.VISIBLE);
                        } else {
                            clearSearchEditText.setVisibility(AppCompatImageView.GONE);
                        }
                    }
                }
            });
        }
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
        Log.d("SearchController", "Проведено пошук за даними.");

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
                        Log.w("SearchController", "Немає вибраних елементів для сортування.");
                        Toast.makeText(context, R.string.empty_selected_items_for_sort, Toast.LENGTH_SHORT).show();
                    }

                    ((ChooseItem<T>) adapter).setItems(sortBySelectedItems);
                    Log.d("SearchController", "Результати пошуку відсортовані за зростанням.");
                    break;

                case 1: // Сортування за спаданням (A → Z)
                    sortAlgorithm(false);
                    ((ChooseItem<T>) adapter).setItems(searchResults);
                    break;

                case 0: // Сортування за зростанням (Z → A)
                    sortAlgorithm(true);
                    ((ChooseItem<T>) adapter).setItems(searchResults);
                    break;

                default:
                    Log.w("SearchController", "Невідомий режим сортування: " + sortMode);
            }
        } else if (adapter instanceof Search<?>) {
            switch (sortMode % 2) {
                case 1: // Сортування за спаданням (Z → A)
                    sortAlgorithm(false);
                    ((Search<T>) adapter).setResultItems(searchResults);
                    break;

                case 0: // Сортування за зростанням (A → Z)
                    sortAlgorithm(true);
                    ((Search<T>) adapter).setResultItems(searchResults);
                    break;

                default:
                    Log.w("SearchController", "Невідомий режим сортування: " + sortMode);
            }

            ((Search<T>) adapter).setResultItems(searchResults);
            adapter.notifyDataSetChanged();
        }
    }

    private void sortAlgorithm(boolean mode) {
        if (searchResults == null || searchResults.isEmpty()) return;

        if (ClassUtils.isListOfType(searchResults, String.class)) {
            searchResults.sort((Comparator<T>) Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER));
        } else if (ClassUtils.isListOfType(searchResults, Item.class)) {
            searchResults.sort(Comparator.comparing(item -> ((Item) item).getName(), String.CASE_INSENSITIVE_ORDER));
        }

        if (!mode) {
            Collections.reverse(searchResults);
            Log.d("SearchController", "Результати пошуку відсортовані за спаданням.");
        } else Log.d("SearchController", "Результати пошуку відсортовані за зростанням.");
    }


    /**
     * Виконує пошук за поточним текстом у полі пошуку.
     */
    public void search() {
        if (searchEditText != null) {
            updateResults(performSearch(searchEditText.getText().toString().trim()));
        }
    }

    public ArrayList<T> searchAndGetResult(String data) {
        ArrayList<T> result = performSearch(data);
        updateResults(result);

        return result;
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
        this.searchEditText = searchEditText;
        setListener();
    }

    /**
     * Встановлює новий RecyclerView для відображення результатів пошуку.
     *
     * @param searchResultsRecyclerView Новий RecyclerView.
     */
    public void setSearchResultsRecyclerView(@NonNull RecyclerView searchResultsRecyclerView) {
        initializeRecyclerView(searchResultsRecyclerView);
    }

    public ArrayList<T> getSearchResults() {
        return searchResults;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter;
    }

    public RecyclerView getRecyclerView() { return searchResultsRecyclerView; }

    public void setClearSearchEditText(AppCompatImageView clearSearchEditText) {
        this.clearSearchEditText = clearSearchEditText;

        // Додаємо обробник натискання на кнопку очищення
        this.clearSearchEditText.setOnClickListener(v -> {
            searchEditText.setText("");
            search();
        });
    }

    public void setSortResultSearchButton(AppCompatImageView sortResultSearchButton) {
        this.sortResultSearchButton = sortResultSearchButton;

        this.sortResultSearchButton.setOnClickListener(v -> {
            if (flagAccessSort.get()) {
                flagAccessSort.set(false);
                sortResults();

                getRecyclerView().postDelayed(() -> {
                    flagAccessSort.set(true);
                    getRecyclerView().smoothScrollToPosition(0); // Прокрутка до початку списку після сортування
                }, 400);
            }
        });
    }
}
