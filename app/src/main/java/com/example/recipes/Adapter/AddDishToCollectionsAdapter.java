package com.example.recipes.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку колекцій з можливістю вибору (чекбокси).
 * Використовується для додавання страв до колекцій.
 */
public class AddDishToCollectionsAdapter extends RecyclerView.Adapter<AddDishToCollectionsAdapter.CollectionViewHolder> {
    private static Context context;
    private static String[] themeArray;
    private static ArrayList<Collection> collections = new ArrayList<>();
    private static ArrayList<Long> selectedCollectionIds = new ArrayList<>();
    private static PreferencesController preferencesController;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param collections Список колекцій для відображення.
     */
    public AddDishToCollectionsAdapter(Context context, ArrayList<Collection> collections) {
        this.context = context;
        this.collections.clear();
        this.collections.addAll(collections);
        this.selectedCollectionIds.clear();
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_check__item, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        Collection collection = collections.get(position);
        holder.bind(collection);

        holder.collection_name.setText(collection.getName());

        setImage(holder, collection);

        holder.collection_check.setOnCheckedChangeListener(null);
        holder.collection_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCollectionIds.add(collection.getId());
            } else {
                selectedCollectionIds.remove(collection.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    /**
     * Повертає список вибраних ID колекцій.
     *
     * @return Список вибраних ID колекцій.
     */
    public ArrayList<Long> getSelectedCollectionIds() {
        return selectedCollectionIds;
    }

    /**
     * Встановлює зображення для колекції в залежності від її типу.
     *
     * @param holder ViewHolder для елемента списку.
     * @param collection Колекція, для якої встановлюється зображення.
     */
    private static void setImage(CollectionViewHolder holder, Collection collection) {
        // Вставка своїх іконок для системних колекцій
        if (Objects.equals(collection.getName(), context.getString(R.string.favorites))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), context.getString(R.string.my_recipes))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book_a));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), context.getString(R.string.gpt_recipes))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_neurology));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), context.getString(R.string.import_recipes))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_download));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        }
    }

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку колекцій.
     */
    static class CollectionViewHolder extends RecyclerView.ViewHolder {
        TextView collection_name;
        ImageView collection_img;
        CheckBox collection_check;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            collection_name = itemView.findViewById(R.id.collection_name_checkItem);
            collection_img = itemView.findViewById(R.id.collection_imageView_checkItem);
            collection_check = itemView.findViewById(R.id.collection_check_checkItem);
        }

        /**
         * Прив'язує дані до елементів ViewHolder.
         *
         * @param collection Колекція, яку потрібно відобразити.
         */
        void bind(Collection collection) {
            collection_name.setText(collection.getName());
            setImage(this, collection);

            collection_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        collections.get(position).setName(s.toString());
                    }
                }
            });

            collection_check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (isChecked) {
                        selectedCollectionIds.add(collections.get(position).getId());
                    } else {
                        selectedCollectionIds.remove(collections.get(position).getId());
                    }
                }
            });
        }
    }
}
