package com.example.recipes.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
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
 * Адаптер для відображення списку колекцій з можливістю кліку на елементи та відображення додаткових опцій (меню).
 * Використовує DiffUtil для ефективного оновлення списку.
 */
public class CollectionGetAdapter extends ListAdapter<Collection, CollectionGetAdapter.ViewHolder> {
    private final Context context;
    private static String[] themeArray;
    private PreferencesController preferencesController;
    private CollectionClickListener collectionClickListener;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст додатку.
     * @param clickListener Лістенер для обробки кліків на елементи.
     */
    public CollectionGetAdapter(Context context, CollectionClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.collectionClickListener = clickListener;
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
        if (Objects.equals(collection.getName(), Collection.SYSTEM_COLLECTION_TAG + "1") || Objects.equals(collection.getName(), context.getString(R.string.favorites))) {
            holder.collection_name.setText(context.getString(R.string.favorites));
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), Collection.SYSTEM_COLLECTION_TAG + "2") || Objects.equals(collection.getName(), context.getString(R.string.my_recipes))) {
            holder.collection_name.setText(context.getString(R.string.my_recipes));
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book_a));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), Collection.SYSTEM_COLLECTION_TAG + "3") || Objects.equals(collection.getName(), context.getString(R.string.gpt_recipes))) {
            holder.collection_name.setText(context.getString(R.string.gpt_recipes));
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_neurology));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), Collection.SYSTEM_COLLECTION_TAG + "4") || Objects.equals(collection.getName(), context.getString(R.string.import_recipes))) {
            holder.collection_name.setText(context.getString(R.string.import_recipes));
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_download));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else {
            holder.collection_name.setText(collection.getName());
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book));
            if (Objects.equals(preferencesController.getThemeString(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        }

        int counterDishes = collection.getDishes().size();

        holder.counter_dishes.setText(String.valueOf(counterDishes));
        holder.collection_name.setPadding(0, 0, countDigits(counterDishes), 0);

        holder.menu_img.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onMenuIconClick(collection, holder.menu_img);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onCollectionClick(collection, holder.childRecyclerView);
            }
        });
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
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView collection_img, menu_img;
        TextView collection_name, counter_dishes;
        RecyclerView childRecyclerView;

        ViewHolder(View itemView) {
            super(itemView);
            collection_img = itemView.findViewById(R.id.collection_imageView);
            collection_name = itemView.findViewById(R.id.collection_name);
            childRecyclerView = itemView.findViewById(R.id.child_recycler_view);
            counter_dishes = itemView.findViewById(R.id.counter_dishes);
            menu_img = itemView.findViewById(R.id.menu_collection_imageView);
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
        void onCollectionClick(Collection collection, RecyclerView childRecyclerView);
        void onMenuIconClick(Collection collection, View anchorView);
    }
}
