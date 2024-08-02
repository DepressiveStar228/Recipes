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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.Controller.PerferencesController;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.R;

import java.util.ArrayList;
import java.util.Objects;

public class CollectionGetAdapter extends RecyclerView.Adapter<CollectionGetAdapter.ViewHolder>{
    private final Context context;
    private static String[] themeArray;
    private final ArrayList<Collection> collections;
    private PerferencesController perferencesController;
    private CollectionClickListener collectionClickListener;

    public CollectionGetAdapter(Context context, ArrayList<Collection> collections, CollectionClickListener clickListener) {
        this.context = context;
        this.collections = collections;
        this.collectionClickListener = clickListener;
        perferencesController = new PerferencesController();
        perferencesController.loadPreferences(context);
        themeArray = perferencesController.getStringArrayForLocale(R.array.theme_options, "en");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);
        if (Objects.equals(collection.getName(), context.getString(R.string.system_collection_tag) + "1")) {
            holder.collection_name.setText(context.getString(R.string.favorites));
        } else if (Objects.equals(collection.getName(), context.getString(R.string.system_collection_tag) + "2")) {
            holder.collection_name.setText(context.getString(R.string.my_recipes));
        } else if (Objects.equals(collection.getName(), context.getString(R.string.system_collection_tag) + "3")) {
            holder.collection_name.setText(context.getString(R.string.gpt_recipes));
        } else if (Objects.equals(collection.getName(), context.getString(R.string.system_collection_tag) + "4")) {
            holder.collection_name.setText(context.getString(R.string.import_recipes));
        } else {
            holder.collection_name.setText(collection.getName());
        }

        if (Objects.equals(collection.getName(), context.getString(R.string.system_collection_tag) + "1")) {
            if (Objects.equals(perferencesController.theme, themeArray[0])) {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star));
            } else {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_star_darkmode));
            }
        } else {
            if (Objects.equals(perferencesController.theme, themeArray[0])) {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book));
            } else {
                holder.collection_img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_book_darkmode));
            }
        }

        ArrayList<Dish> dishes = new ArrayList<>();
        int counter_dishes = 0;

        try {
            dishes = collection.getDishes();
            counter_dishes = dishes.size();
        } catch (Exception e) {
            Log.e("CollectionGetAdapter", "Адаптер не зміг рахувати страви колекції");
        }

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

        ChildItemAdapter childItemAdapter = new ChildItemAdapter(context, dishes, collectionClickListener);
        holder.childRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.childRecyclerView.setAdapter(childItemAdapter);
    }

    @Override
    public int getItemCount() {
        return collections.size();
    }

    public void addCollection(Collection collection) {
        collections.add(collection);
        notifyItemInserted(collections.size() - 1);
        Log.d("CollectionGetAdapter", "Адаптер додав колекцію");
    }

    public void delCollection(Collection collection) {
        int position = collections.indexOf(collection);
        if (position != -1) {
            collections.remove(position);
            notifyItemRemoved(position);
            Log.d("CollectionGetAdapter", "Адаптер видалив колекцію");
        }
    }

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
        void onDishClick(Dish item, View v);
        void onMenuIconClick(Collection collection, View anchorView);
        void onDishMenuIconClick(Dish item, View v);
    }

    class ChildItemAdapter extends RecyclerView.Adapter<ChildItemAdapter.ViewHolder> {
        private final Context context;
        private final ArrayList<Dish> dishes;
        private final CollectionGetAdapter.CollectionClickListener commandClickListener;

        public ChildItemAdapter(Context context, ArrayList<Dish> dishes, CollectionGetAdapter.CollectionClickListener clickListener) {
            this.context = context;
            this.dishes = dishes;
            this.commandClickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dish_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Dish dish = dishes.get(position);
            holder.child_item_name.setText(dish.getName());

            if (Objects.equals(perferencesController.theme, themeArray[0])) {
                holder.child_item_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_menu));
            } else {
                holder.child_item_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_menu_darkmode));
            }

            holder.child_item_image.setOnClickListener(v -> {
                if (collectionClickListener != null) {
                    collectionClickListener.onDishMenuIconClick(dish, holder.child_item_image);
                }
            });

            holder.itemView.setOnClickListener(v -> {
                if (commandClickListener != null) {
                    commandClickListener.onDishClick(dish, v);
                }
            });
        }

        @Override
        public int getItemCount() {
            return dishes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView child_item_name;
            ImageView child_item_image;

            ViewHolder(View itemView) {
                super(itemView);
                child_item_name = itemView.findViewById(R.id.dish_name);
                child_item_image = itemView.findViewById(R.id.menu_dish_imageView);
            }
        }
    }
}
