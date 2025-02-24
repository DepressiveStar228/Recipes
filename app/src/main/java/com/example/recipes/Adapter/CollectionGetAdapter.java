package com.example.recipes.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PreferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

public class CollectionGetAdapter extends ListAdapter<Collection, CollectionGetAdapter.ViewHolder> {
    private final Context context;
    private static String[] themeArray;
    private PreferencesController preferencesController;
    private CollectionClickListener collectionClickListener;

    public CollectionGetAdapter(Context context, CollectionClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.collectionClickListener = clickListener;
        preferencesController = new PreferencesController();
        preferencesController.loadPreferences(context);
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

        holder.collection_name.setText(collection.getName());

        if (Objects.equals(collection.getName(), context.getString(R.string.favorites))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star));
            if (Objects.equals(preferencesController.getTheme(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), context.getString(R.string.my_recipes))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book_a));
            if (Objects.equals(preferencesController.getTheme(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), context.getString(R.string.gpt_recipes))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_neurology));
            if (Objects.equals(preferencesController.getTheme(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else if (Objects.equals(collection.getName(), context.getString(R.string.import_recipes))) {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_download));
            if (Objects.equals(preferencesController.getTheme(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        } else {
            holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book));
            if (Objects.equals(preferencesController.getTheme(), themeArray[0])) {
                holder.collection_img.setColorFilter(R.color.white);
            }
        }

        int counter_dishes = collection.getDishes().size();

        holder.counter_dishes.setText(String.valueOf(counter_dishes));
        holder.collection_name.setPadding(0,0,countDigits(counter_dishes),0);

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

    public void setItems(ArrayList<Collection> newItems) {
        ArrayList<Collection> newList = new ArrayList<>(newItems.size());
        for (Collection item : newItems) {
            newList.add(new Collection(item));
        }

        submitList(newList);
        notifyDataSetChanged();
    }

    public static final DiffUtil.ItemCallback<Collection> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Collection>() {
                @Override
                public boolean areItemsTheSame(@NonNull Collection oldItem, @NonNull Collection newItem) {
                    boolean box = oldItem.getId() == newItem.getId();
                    return box;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Collection oldItem, @NonNull Collection newItem) {
                    boolean box = oldItem.equals(newItem);
                    return box;
                }
            };

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

    private int countDigits(int number) {
        return (int) ((120 + (String.valueOf(number).length() * 10)) * context.getResources().getDisplayMetrics().density);
    }

    public interface CollectionClickListener {
        void onCollectionClick(Collection collection, RecyclerView childRecyclerView);
        void onMenuIconClick(Collection collection, View anchorView);
    }
}
