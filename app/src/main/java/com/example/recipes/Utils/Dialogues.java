package com.example.recipes.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Adapter.ChooseItemAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.SearchController;
import com.example.recipes.Enum.Limits;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітарний клас для роботи з діалогами.
 */
public class Dialogues {
    private Activity activity;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;

    public Dialogues(Activity activity) {
        this.activity = activity;
        this.builder = new AlertDialog.Builder(activity);
        this.inflater = activity.getLayoutInflater();
    }

    /**
     * Оновлює будівельник діалогу.
     */
    private void updateBuilder() {
        this.builder = new AlertDialog.Builder(activity);
    }

    public void dialogExit(Runnable callbackPositiveButton, Runnable callbackNegativeButton) {
        updateBuilder();

        View dialogView = inflater.inflate(R.layout.dialog_exit, null);
        if (dialogView != null) {
            Button yesButton = dialogView.findViewById(R.id.yesButton);
            Button noButton = dialogView.findViewById(R.id.noButton);

            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            if (yesButton != null) {
                yesButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callbackPositiveButton != null) callbackPositiveButton.run();
                });
            }
            if (noButton != null) {
                noButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callbackNegativeButton != null) callbackNegativeButton.run();
                });
            }
        }
    }

    /**
     * Відкриває діалог для введення рядка.
     *
     * @param limitsCharEditText - обмеження на кількість символів у полі вводу
     * @param callbackPositiveButton - колбек для обробки натискання кнопки "ОК"
     * @param nameDialogTextId - текст діалогу
     * @param namePositiveButtonTextId - текст кнопки "ОК"
     */
    public void dialogSetStringParamCollection(Limits limitsCharEditText, Consumer<String> callbackPositiveButton, int nameDialogTextId, int namePositiveButtonTextId) {
        updateBuilder();
        dialogSetStringParamCollection("", limitsCharEditText, callbackPositiveButton, nameDialogTextId, namePositiveButtonTextId);
    }

    /**
     * Відкриває діалог для введення рядка.
     *
     * @param startData - початкове значення для поля вводу
     * @param limitsCharEditText - обмеження на кількість символів у полі вводу
     * @param callbackPositiveButton - колбек для обробки натискання кнопки "ОК"
     * @param nameDialogTextId - текст діалогу
     * @param namePositiveButtonTextId - текст кнопки "ОК"
     */
    public void dialogSetStringParamCollection(String startData, Limits limitsCharEditText, Consumer<String> callbackPositiveButton, int nameDialogTextId, int namePositiveButtonTextId) {
        updateBuilder();

        View dialogView = inflater.inflate(R.layout.dialog_set_string_param, null);
        if (dialogView != null) {
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            if (dialogTitle != null) { dialogTitle.setText(nameDialogTextId); }

            Button yesButton = dialogView.findViewById(R.id.yesButton);
            Button noButton = dialogView.findViewById(R.id.noButton);

            EditText editText = dialogView.findViewById(R.id.add_collection_name_editText);
            if (editText != null) {
                editText.setText(startData);
                CharacterLimitTextWatcher.setCharacterLimit(activity, editText, limitsCharEditText);
            }

            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            if (yesButton != null) {
                yesButton.setText(namePositiveButtonTextId);

                yesButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callbackPositiveButton != null) callbackPositiveButton.accept((editText != null) ? editText.getText().toString().trim() : "");
                });
            }
            if (noButton != null) {
                noButton.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            }
        }
    }

    /**
     * Відкриває діалог для видору об'єктів.
     *
     * @param items - список об'єктів для вибору
     * @param callbackPositiveButton - колбек для обробки натискання кнопки "ОК"
     * @param nameDialogTextId - текст діалогу
     * @param namePositiveButtonTextId - текст кнопки "ОК"
     * @param <T> - тип об'єктів у списку
     */
    public <T> void dialogChooseItems(ArrayList<T> items, Consumer<ArrayList<T>> callbackPositiveButton, int nameDialogTextId, int namePositiveButtonTextId) {
        updateBuilder();
        dialogChooseItems(items, new ArrayList<>(), callbackPositiveButton, nameDialogTextId, namePositiveButtonTextId);
    }

    /**
     * Відкриває діалог для видору об'єктів.
     *
     * @param items - список об'єктів для вибору
     * @param selectedItems - список вибраних об'єктів
     * @param callbackPositiveButton - колбек для обробки натискання кнопки "ОК"
     * @param nameDialogTextId - текст діалогу
     * @param namePositiveButtonTextId - текст кнопки "ОК"
     * @param <T> - тип об'єктів у списку
     */
    public <T> void dialogChooseItems(ArrayList<T> items, ArrayList<T> selectedItems, Consumer<ArrayList<T>> callbackPositiveButton, int nameDialogTextId, int namePositiveButtonTextId) {
        updateBuilder();

        View dialogView = inflater.inflate(R.layout.dialog_choose_items, null);
        if (dialogView != null) {
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            if (dialogTitle != null) { dialogTitle.setText(nameDialogTextId); }

            Button yesButton = dialogView.findViewById(R.id.yesButton);
            Button noButton = dialogView.findViewById(R.id.noButton);

            RecyclerView recyclerView = dialogView.findViewById(R.id.items_check_RecyclerView);
            ConstraintLayout empty = dialogView.findViewById(R.id.empty);

            ChooseItemAdapter<T> chooseItemAdapter = new ChooseItemAdapter<>(activity, empty, (checkBox, item) -> { });
            chooseItemAdapter.setItemsAndSelected(items, selectedItems);
            if (recyclerView != null) {
                recyclerView.setAdapter(chooseItemAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            }

            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            if (yesButton != null) {
                yesButton.setText(namePositiveButtonTextId);

                yesButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (callbackPositiveButton != null) callbackPositiveButton.accept(chooseItemAdapter.getSelectedItem());
                });
            }
            if (noButton != null) {
                noButton.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            }
        }
    }

    /**
     * Відкриває діалог для видору об'єктів з пошуком.
     *
     * @param items - список об'єктів для вибору
     * @param callbackPositiveButton - колбек для обробки натискання кнопки "ОК"
     * @param callbackNeutralButton - колбек для обробки натискання кнопки "Скинути"
     * @param limitsCharEditText - обмеження на кількість символів у полі вводу
     * @param nameDialogTextId - текст діалогу
     * @param namePositiveButtonTextId - текст кнопки "ОК"
     * @param <T> - тип об'єктів у списку
     */
    public <T> void dialogChooseItemsWithSearch(ArrayList<T> items, Consumer<ArrayList<T>> callbackPositiveButton, Runnable callbackNeutralButton, Limits limitsCharEditText, int nameDialogTextId, int namePositiveButtonTextId) {
        dialogChooseItemsWithSearch(items, new ArrayList<T>(), callbackPositiveButton, callbackNeutralButton, limitsCharEditText, nameDialogTextId, namePositiveButtonTextId);
    }

    /**
     * Відкриває діалог для видору об'єктів з пошуком.
     *
     * @param items - список об'єктів для вибору
     * @param selectedItems - список вибраних об'єктів
     * @param callbackPositiveButton - колбек для обробки натискання кнопки "ОК"
     * @param callbackNeutralButton - колбек для обробки натискання кнопки "Скинути"
     * @param limitsCharEditText - обмеження на кількість символів у полі вводу
     * @param nameDialogTextId - текст діалогу
     * @param namePositiveButtonTextId - текст кнопки "ОК"
     * @param <T> - тип об'єктів у списку
     */
    public <T> void dialogChooseItemsWithSearch(ArrayList<T> items, ArrayList<T> selectedItems, Consumer<ArrayList<T>> callbackPositiveButton, Runnable callbackNeutralButton, Limits limitsCharEditText, int nameDialogTextId, int namePositiveButtonTextId) {
        updateBuilder();

        View dialogView = inflater.inflate(R.layout.dialog_choose_items_with_search, null);

        if (dialogView != null) {
            TextView textView = dialogView.findViewById(R.id.dialogTitle);
            if (textView != null) { textView.setText(nameDialogTextId); }

            Button yesButton = dialogView.findViewById(R.id.yesButton);
            Button neutralButton = dialogView.findViewById(R.id.neutralButton);
            Button noButton = dialogView.findViewById(R.id.noButton);

            RecyclerView recyclerView = dialogView.findViewById(R.id.items_result_check_RecyclerView);
            ConstraintLayout searchField = dialogView.findViewById(R.id.searchField);
            EditText editText = searchField.findViewById(R.id.searchEditText);
            AppCompatImageView clearButton = searchField.findViewById(R.id.clearInputTextButton);
            AppCompatImageView sortButton = searchField.findViewById(R.id.sortButton);
            ConstraintLayout empty = dialogView.findViewById(R.id.empty);

            if (editText != null) CharacterLimitTextWatcher.setCharacterLimit(activity, editText, limitsCharEditText);

            if (recyclerView != null) {
                // Налаштовуємо пошук по елементам
                SearchController<T> searchController = new SearchController<>(activity, editText, recyclerView, new ChooseItemAdapter<String>(activity, empty, (checkBox, item) -> { }));
                searchController.setArrayData(items);
                searchController.setArraySelectedData(selectedItems);
                searchController.setSearchEditText(editText);
                searchController.setSearchRecyclerView(recyclerView);

                if (editText != null) {
                    if (clearButton != null) searchController.setClearSearchEditText(clearButton); // Додаємо кнопку очищення поля вводу
                    if (sortButton != null) searchController.setSortResultSearchButton(sortButton); // Додаємо кнопку сортування
                }

                ChooseItemAdapter<T> adapterChooseObjects = (ChooseItemAdapter) searchController.getAdapter();
                ArrayList<T> newSelectedItems = adapterChooseObjects.getSelectedItem();

                builder.setView(dialogView);

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.setOnDismissListener(v -> searchController.clear());

                if (yesButton != null) {
                    yesButton.setText(namePositiveButtonTextId);

                    yesButton.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (callbackPositiveButton != null) callbackPositiveButton.accept(newSelectedItems);
                    });
                }

                if (neutralButton != null && callbackNeutralButton != null) {
                    neutralButton.setText(R.string.reset);

                    neutralButton.setOnClickListener(v -> {
                        dialog.dismiss();
                        callbackNeutralButton.run();
                    });
                } else {
                    if (neutralButton != null) {
                        neutralButton.setVisibility(View.GONE);
                    }
                }

                if (noButton != null) {
                    noButton.setOnClickListener(v -> dialog.dismiss());
                }
            }
        }
    }
}
