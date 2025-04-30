package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення списку колекцій з можливістю кліку на елементи та відображення додаткових опцій (меню).
 * Використовує DiffUtil для ефективного оновлення списку.
 */
public class CollectionGetAdapter extends ListAdapter<Collection, CollectionGetAdapter.ViewHolder> {
    private final Context context;
    private final String[] themeArray;
    private ConstraintLayout empty;
    private RecipeUtils recipeUtils;
    private final PreferencesController preferencesController;
    private final CollectionClickListener collectionClickListener;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param clickListener Лістенер для обробки кліків на елементи.
     */
    public CollectionGetAdapter(Context context, ConstraintLayout empty, CollectionClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.collectionClickListener = clickListener;
        this.empty = empty;
        this.recipeUtils = new RecipeUtils(context);
        preferencesController = PreferencesController.getInstance();
        themeArray = preferencesController.getStringArrayForLocale(R.array.theme_options, "en");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getBindingAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) return;
        Collection collection = getItem(currentPosition);

        // Вставка своїх іконок та назв для системних колекцій
        holder.collection_name.setText(recipeUtils.ByCollection().getCustomNameSystemCollectionByName(collection.getName()));
        holder.collection_img.setImageDrawable(recipeUtils.ByCollection().getDrawableByName(collection.getName()));
        if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
            holder.collection_img.setColorFilter(R.color.white);
        }

        int counterDishes = collection.getDishes().size();

        holder.counter_dishes.setText(String.valueOf(counterDishes));
        holder.collection_name.setPadding(0, 0, countDigits(counterDishes), 0);

        holder.imageView.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onImageViewClick(collection, holder.imageView);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onCollectionClick(collection);
            }
        });
    }

    /**
     * Перевіряє, чи список колекцій порожній, і оновлює відображення порожнього стану.
     */
    private void checkEmpty() {
        if (empty != null) {
            if (getCurrentList().isEmpty()) empty.setVisibility(View.VISIBLE);
            else empty.setVisibility(View.GONE);
        }
    }

    /**
     * Оновлює список елементів у адаптері.
     *
     * @param newItems Новий список колекцій.
     */
    public void setItems(ArrayList<Collection> newItems) {
        ArrayList<Collection> newList = new ArrayList<>(newItems.size());
        for (Collection item : newItems) {
            newList.add(new Collection(item));
        }

        submitList(newList);
        notifyDataSetChanged();
        checkEmpty();
    }

    /**
     * Callback для порівняння елементів списку за допомогою DiffUtil.
     */
    public static final DiffUtil.ItemCallback<Collection> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Collection oldItem, @NonNull Collection newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Collection oldItem, @NonNull Collection newItem) {
                    return oldItem.equals(newItem);
                }
            };

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку колекцій.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView collection_img, imageView;
        TextView collection_name, counter_dishes;

        ViewHolder(View itemView) {
            super(itemView);
            collection_img = itemView.findViewById(R.id.collection_imageView);
            collection_name = itemView.findViewById(R.id.collection_name);
            counter_dishes = itemView.findViewById(R.id.counter_dishes);
            imageView = itemView.findViewById(R.id.menu_collection_imageView);
        }
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

    /**
     * Інтерфейс для обробки кліків на елементи списку колекцій.
     */
    public interface CollectionClickListener {
        void onCollectionClick(Collection collection);
        void onImageViewClick(Collection collection, View anchorView);
    }
}
