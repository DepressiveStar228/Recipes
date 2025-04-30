package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;
import java.util.Objects;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    private final Context context;
    private boolean isRead;
    private ConstraintLayout empty;
    private final ArrayList<Collection> collections = new ArrayList<>();
    private RecipeUtils recipeUtils;
    private final String[] themeArray;
    private final PreferencesController preferencesController;

    public CollectionAdapter(Context context, boolean isRead, ConstraintLayout empty) {
        this.context = context;
        this.isRead = isRead;
        this.empty = empty;
        recipeUtils = new RecipeUtils(context);
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
    }

    @NonNull
    @Override
    public CollectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_item, parent, false);
        return new CollectionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionAdapter.ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;
        Collection collection = collections.get(currentPosition);

        // Вставка своїх іконок та назв для системних колекцій
        holder.collection_name.setText(recipeUtils.ByCollection().getCustomNameSystemCollectionByName(collection.getName()));
        holder.collection_img.setImageDrawable(recipeUtils.ByCollection().getDrawableByName(collection.getName()));
        if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
            holder.collection_img.setColorFilter(R.color.white);
        }

        int counterDishes = collection.getDishes().size();

        holder.collection_name.setPadding(0, 0, countDigits(counterDishes), 0);

        if (isRead) holder.imageView.setVisibility(View.GONE);
        else holder.imageView.setVisibility(View.VISIBLE);

        holder.imageView.setOnClickListener(v -> deleteCollection(collection));
    }

    /**
     * Метод для встановлення списку колекцій.
     */
    public void setCollections(ArrayList<Collection> collections) {
        this.collections.clear();
        this.collections.addAll(collections);
        notifyDataSetChanged();
        checkEmpty();
    }

    /**
     * Метод для додавання нової колекції до адаптера.
     */
    public void addCollection(Collection collection) {
        this.collections.add(collection);
        notifyItemInserted(collections.size() - 1);
        checkEmpty();
    }

    /**
     * Метод для додавання нової колекції до адаптера.
     */
    public void addCollections(ArrayList<Collection> collections) {
        for (Collection collection : collections) {
            if (!this.collections.contains(collection)) {
                addCollection(collection);
            }
        }
    }

    /**
     * Метод для видалення колекції з адаптера.
     */
    public void deleteCollection(Collection collection) {
        int position = collections.indexOf(collection);
        if (position >= 0 && position < collections.size()) {
            collections.remove(position);
            notifyItemRemoved(position);
            checkEmpty();
        }
    }

    /**
     * Метод для видалення колекцій з адаптера.
     */
    public void deleteCollections(ArrayList<Collection> collections) {
        for (Collection collection : collections) {
            deleteCollection(collection);
        }
    }

    /**
     * Метод для отримання списку колекцій.
     */
    public ArrayList<Collection> getCollections() {
        return collections;
    }

    /**
     * Перевіряє, чи список колекцій порожній, і оновлює відображення порожнього стану.
     */
    private void checkEmpty() {
        if (empty != null) {
            if (collections.isEmpty()) empty.setVisibility(View.VISIBLE);
            else empty.setVisibility(View.GONE);
        }
    }

    /**
     * Встановлює режим відображення списку
     *
     * @param isRead флаг режиму відображення
     */
    public void setReadMode(boolean isRead) {
        this.isRead = isRead;
        notifyDataSetChanged();
    }

    /**
     * Метод для підрахунку кількості цифр у числі та обчислення відступу.
     *
     * @param number Число для підрахунку цифр.
     * @return Відступ у пікселях.
     */
    private int countDigits(int number) {
        return (int) ((120 + (String.valueOf(number).length() * 10)) * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView collection_img, imageView;
        TextView collection_name, counter_dishes;

        ViewHolder(View itemView) {
            super(itemView);
            collection_img = itemView.findViewById(R.id.collection_imageView);

            collection_name = itemView.findViewById(R.id.collection_name);

            counter_dishes = itemView.findViewById(R.id.counter_dishes);
            if (counter_dishes != null) counter_dishes.setVisibility(View.GONE);

            imageView = itemView.findViewById(R.id.menu_collection_imageView);
            if (imageView != null) imageView.setImageResource(R.drawable.icon_delete);
        }
    }
}
