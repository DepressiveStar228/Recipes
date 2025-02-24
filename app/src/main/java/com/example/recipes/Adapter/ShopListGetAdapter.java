package com.example.recipes.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Config;
import com.example.recipes.Controller.DataControllerForAdapter;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.IngredientShopList;
import com.example.recipes.R;
import com.example.recipes.Utils.RecipeUtils;

import java.util.ArrayList;

public class ShopListGetAdapter extends RecyclerView.Adapter<ShopListGetAdapter.ViewHolder> implements DataControllerForAdapter<Collection> {
    private final LifecycleOwner lifecycleOwner;
    private final TextView empty;
    private ArrayList<Collection> collections;
    private CollectionClickListener collectionClickListener;
    private RecipeUtils utils;

    public ShopListGetAdapter(Context context, LifecycleOwner lifecycleOwner, TextView empty, ArrayList<Collection> collections, CollectionClickListener clickListener) {
        this.lifecycleOwner = lifecycleOwner;
        this.empty = empty;
        this.collections = collections;
        this.collectionClickListener = clickListener;
        this.utils = new RecipeUtils(context);
    }

    @NonNull
    @Override
    public ShopListGetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_list_item, parent, false);
        return new ShopListGetAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);
        holder.shop_list_name.setText(collection.getName());

        utils.ByIngredientShopList()
                .getViewModel()
                .getBoughtCountByIdCollection(collection.getId())
                .observe(lifecycleOwner, data -> {
                    if (data != null) {
                        holder.boughtItem.setText(data.toString());
                    }
                }
        );

        utils.ByIngredientShopList()
                .getViewModel()
                .getCountByIdCollection(collection.getId())
                .observe(lifecycleOwner, data -> {
                            if (data != null) {
                                holder.allItem.setText(data.toString());
                            }
                        }
                );

        holder.menu_img.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onMenuIconClick(collection, holder.menu_img);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (collectionClickListener != null) {
                collectionClickListener.onCollectionClick(collection);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    @Override
    public void addItem(RecyclerView recyclerView, Collection item) {
        collections.add(0, item);
        int position = 0;
        notifyItemInserted(position);

        recyclerView.postDelayed(() -> {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                holder.itemView.setAlpha(0f);
                holder.itemView.animate()
                        .alpha(1f)
                        .setDuration(250)
                        .start();
            }
            checkEmptyList();
        }, 50);

        Log.d("ShopListGetAdapter", "Адаптер додав колекцію");
    }

    @Override
    public void addItems(RecyclerView recyclerView, ArrayList<Collection> items) {
        if (items.size() + collections.size() > Config.COUNT_LIMIT_SHOP_LIST) {
            for (Collection collection : items) {
                addItem(recyclerView, collection);
            }

            Log.d("ShopListGetAdapter", "Адаптер додав колекцію");
        }
    }

    @Override
    public void delItem(RecyclerView recyclerView, Collection item) {
        int position = getPosition(item);
        if (position != -1) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

            if (holder != null) {
                holder.itemView.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(() -> {
                            collections.remove(position);
                            notifyItemRemoved(position);
                            checkEmptyList();
                            Log.d("ShopListGetAdapter", "Адаптер видалив колекцію");
                        })
                        .start();
            }
        }
    }

    @Override
    public void upItem(RecyclerView recyclerView, Collection item) {
        int position = getPosition(item);
        if (position != -1) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                collections.set(position, item);
                notifyItemChanged(position);
                checkEmptyList();
            }
        }
    }

    @Override
    public void setItems(ArrayList<Collection> items) {
        this.collections = items;
        notifyDataSetChanged();
        checkEmptyList();
    }

    @Override
    public int getPosition(Collection item) {
        return collections.indexOf(item);
    }

    public void checkEmptyList() {
        if (empty != null) {
            if (!collections.isEmpty()) {
                empty.setVisibility(View.GONE);
            } else {
                empty.setVisibility(View.VISIBLE);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView shop_list_name, boughtItem, allItem;
        ImageView menu_img;

        ViewHolder(View itemView) {
            super(itemView);
            shop_list_name = itemView.findViewById(R.id.shop_list_name);
            boughtItem = itemView.findViewById(R.id.numbBoughItem);
            allItem = itemView.findViewById(R.id.numbAllItem);
            menu_img = itemView.findViewById(R.id.menu_shop_list_imageView);
        }
    }

    public interface CollectionClickListener {
        void onCollectionClick(Collection collection);
        void onMenuIconClick(Collection collection, View v);
    }
}
