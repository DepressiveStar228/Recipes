package com.example.recipes.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Activity.ReadDataDishActivity;
import com.example.recipes.Adapter.AddDishToCollectionAdapter;
import com.example.recipes.Adapter.CollectionGetAdapter;
import com.example.recipes.Controller.CharacterLimitTextWatcher;
import com.example.recipes.Controller.ImportExportController;
import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.FileUtils;
import com.example.recipes.Item.RecipeUtils;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

public class CollectionsDishFragment extends Fragment {
    private PerferencesController perferencesController;
    private RecyclerView collectionsRecyclerView;
    private Button addCollectionButton;
    private ArrayList<Collection> collections;
    private CollectionGetAdapter adapter;
    private RecipeUtils utils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.collections_activity, container, false);

        utils = new RecipeUtils(getContext());

        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(getContext());

        loadItemsActivity(view);
        loadClickListeners();
        return view;
    }

    private void loadItemsActivity(View view) {
        collectionsRecyclerView = view.findViewById(R.id.collections_dishRecyclerView);
        addCollectionButton = view.findViewById(R.id.add_collection_button);
        Log.d("CollectionsDishFragment", "Об'єкти фрагмента успішно завантажені.");
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadClickListeners(){
        updateCollectionRecyclerView();

        addCollectionButton.setOnClickListener(v -> { showAddCollectionDialog(); });

        Log.d("CollectionsDishFragment", "Слухачі фрагмента успішно завантажені.");
    }

    private void loadCollections() {
        collections = utils.getAllCollections();
        Log.d("CollectionsDishFragment", "Колекції фрагмента успішно завантажені");
    }

    private void handleEditAction(Collection collection) {
        showEditCollectionDialog(collection);
    }

    private void handleDeleteAction(Collection collection, boolean mode) {
        String message;
        if (!mode) { message = getString(R.string.warning_delete_collection); }
        else { message = getString(R.string.warning_delete_collection_with_dishes); }

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_delete_dish))
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    if (utils.deleteCollection(collection.getId(), mode)) {
                        adapter.delCollection(collection);
                        updateCollectionRecyclerView();
                    }
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleShareAction(Collection collection) {
        if (!collection.getDishes().isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.confirm_export))
                    .setMessage(getString(R.string.warning_export))
                    .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                        Uri fileUri = ImportExportController.exportRecipeData(getContext(), collection);

                        if (fileUri != null) {
                            FileUtils.sendFileByUri(getContext(), fileUri);
                            FileUtils.deleteFileByUri(getContext(), fileUri);
                            Log.d("CollectionsDishFragment", "Рецепти успішно експортовані");
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null).show();
        } else {
            Toast.makeText(getContext(), R.string.error_empty_collection, Toast.LENGTH_SHORT).show();
            Log.d("CollectionsDishFragment", "Помилка. Колекція порожня");
        }
    }

    private void handleClearAction(Collection collection) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.confirm_clear_collection))
                .setMessage(getString(R.string.warning_clear_collection))
                .setPositiveButton(getString(R.string.yes), (dialog, whichButton) -> {
                    if (utils.deleteDishCollectionData(collection)) {
                        updateCollectionRecyclerView();
                        Toast.makeText(getContext(), R.string.successful_clear_collerction, Toast.LENGTH_SHORT).show();
                        Log.d("CollectionsDishFragment", "Страви успішно копійовані до колекції");
                    } else {
                        Log.e("CollectionsDishFragment", "Помилка копіювання страв у колекцію");
                    }
                })
                .setNegativeButton(getString(R.string.no), null).show();
    }

    private void handleCopyAction(Collection collection) {
        showCopyCollectionDialog(collection);
    }

    private void addCollection(String name) {
        if (utils.addCollection(name)) {
            adapter.addCollection(utils.getCollectionByName(name));
            collectionsRecyclerView.scrollToPosition(collections.size() - 1);
            Toast.makeText(getContext(), R.string.successful_add_collection, Toast.LENGTH_SHORT).show();
            Log.d("CollectionsDishFragment", "Колекція успішно створена");
        } else {
            Log.e("CollectionsDishFragment", "Помилка створення колекції");
        }
    }


    private void editCollection(Collection collection, String name) {
        Collection newCollection = new Collection(name, collection.getDishes());

        int index = collections.indexOf(collection);
        if (index != -1) {
            collections.set(index, newCollection);
        }
        adapter.notifyDataSetChanged();

        if (utils.updateCollection(collection.getId(), newCollection)) {
            Toast.makeText(getContext(), R.string.successful_add_collection, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.error_edit_collection, Toast.LENGTH_SHORT).show();
        }

    }

    private void showAddCollectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_collection, null);
        EditText editText = dialogView.findViewById(R.id.add_collection_name_editText);
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, 16);
        builder.setView(dialogView)
                .setTitle(R.string.add_collection_dialog)
                .setPositiveButton(R.string.button_add, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    } else if (utils.getIdCollectionByName(collectionName) != -1) {
                        Toast.makeText(getContext(), R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                    } else {
                        addCollection(collectionName);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showEditCollectionDialog(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_collection, null);
        EditText editText = dialogView.findViewById(R.id.edit_collection_name_editText);
        editText.setText(collection.getName());
        CharacterLimitTextWatcher.setCharacterLimit(getContext(), editText, 16);
        builder.setView(dialogView)
                .setTitle(R.string.edit_collection_dialog)
                .setPositiveButton(R.string.edit, (dialog, which) -> {
                    String collectionName = editText.getText().toString().trim();
                    if (collectionName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show();
                    } else if (utils.getIdCollectionByName(collectionName) != -1) {
                        Toast.makeText(getContext(), R.string.warning_dublicate_name_collection, Toast.LENGTH_SHORT).show();
                    } else {
                        editCollection(collection, collectionName);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void showCopyCollectionDialog(Collection collection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_dish_in_collection, null);
        RecyclerView collectionsRecyclerView = dialogView.findViewById(R.id.collection_RecyclerView);
        ArrayList<Collection> collections = utils.getAllCollections();
        AddDishToCollectionAdapter adapter = new AddDishToCollectionAdapter(getContext(), collections);
        collectionsRecyclerView.setAdapter(adapter);
        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        builder.setView(dialogView)
                .setTitle(R.string.copy_dishes_another_collection)
                .setPositiveButton(R.string.button_copy, (dialog, which) -> {
                    ArrayList<Integer> selectedCollectionIds = adapter.getSelectedCollectionIds();
                    if (!selectedCollectionIds.isEmpty()){
                        if (utils.copyDishesToAnotherCollections(collection.getId(), selectedCollectionIds)) {
                            updateCollectionRecyclerView();
                            Toast.makeText(getContext(), getString(R.string.successful_copy_dishes), Toast.LENGTH_SHORT).show();
                            Log.d("ReadDataDishActivity", "Страви успішно додано до колекцію (ї)");
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateCollectionRecyclerView() {
        loadCollections();

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
                intent.putExtra("dish_id", dish.getID());
                v.getContext().startActivity(intent);
            }

            @Override
            public void onMenuIconClick(Collection collection, View anchorView) {
                final boolean isSystemCollection = Objects.equals(collection.getName(), getString(R.string.system_collection_tag) + "1") ||
                        Objects.equals(collection.getName(), getString(R.string.system_collection_tag) + "2") ||
                        Objects.equals(collection.getName(), getString(R.string.system_collection_tag) + "3") ||
                        Objects.equals(collection.getName(), getString(R.string.system_collection_tag) + "4");

                PopupMenu popupMenu = new PopupMenu(getContext(), anchorView, Gravity.END);
                if (isSystemCollection) {
                    popupMenu.getMenuInflater().inflate(R.menu.context_system_menu_collection, popupMenu.getMenu());
                } else {
                    popupMenu.getMenuInflater().inflate(R.menu.context_menu_collection, popupMenu.getMenu());
                }

                for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                    MenuItem item = popupMenu.getMenu().getItem(i);
                    SpannableString spannableString = new SpannableString(item.getTitle());
                    if (!Objects.equals(perferencesController.theme, "Light")) {
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.white)), 0, spannableString.length(), 0);
                    } else {
                        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.black)), 0, spannableString.length(), 0);
                    }
                    item.setTitle(spannableString);
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_edit) {
                        handleEditAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_only_collection) {
                        handleDeleteAction(collection, false);
                        return true;
                    } else if (item.getItemId() == R.id.action_delete_collection_with_dishes) {
                        handleDeleteAction(collection, true);
                        return true;
                    } else if (item.getItemId() == R.id.action_share) {
                        handleShareAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_clear_collection) {
                        handleClearAction(collection);
                        return true;
                    } else if (item.getItemId() == R.id.action_copy_another_collection) {
                        handleCopyAction(collection);
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
    }
}