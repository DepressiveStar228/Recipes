package com.example.recipes.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes.R;
import com.example.recipes.ViewItem.DialogItemContainer;

import java.util.ArrayList;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для відображення діалогу з GPT у вигляді списку повідомлень.
 * Кожен елемент списку містить роль (наприклад, "користувач" або "GPT"), текст повідомлення
 * та кнопку для додавання страви (якщо це повідомлення від GPT).
 */
public class DialogGPTAdapter extends RecyclerView.Adapter<DialogGPTAdapter.DialogGPTItemViewHolder> {
    private ArrayList<DialogItemContainer> containers;
    private AddDishListener listener;

    /**
     * Конструктор адаптера.
     *
     * @param listener Лістенер для обробки кліків на кнопку додавання страви.
     */
    public DialogGPTAdapter(AddDishListener listener) {
        this.listener = listener;
        this.containers = new ArrayList<>();
    }

    @NonNull
    @Override
    public DialogGPTItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_item_container, parent, false);
        return new DialogGPTAdapter.DialogGPTItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DialogGPTAdapter.DialogGPTItemViewHolder holder, int position) {
        DialogItemContainer container = containers.get(position);
        holder.role.setText(container.getRole_item());
        holder.contentText.setText(container.getText_item());

        // Встановлюємо іконку кнопки додавання страви в залежності від стану (додано чи ні)
        if (container.isDishIsAdded()) holder.addDishButton.setImageResource(R.drawable.icon_check);
        else holder.addDishButton.setImageResource(R.drawable.icon_add);

        // Встановлюємо видимість кнопки додавання страви
        holder.addDishButton.setVisibility(container.getVisibilityAddButton());

        // Обробка кліку на кнопку додавання страви
        holder.addDishButton.setOnClickListener(view -> {
            if (!container.isDishIsAdded()) listener.addDishClick(container.getOriginalText(), position);
        });
    }

    /**
     * Додає новий контейнер з повідомленням до списку.
     *
     * @param container Контейнер з повідомленням.
     */
    public void addContainer(DialogItemContainer container) {
        containers.add(container);
        notifyItemInserted(containers.size() - 1);
    }

    /**
     * Позначає, що страва була додана для конкретного повідомлення.
     *
     * @param position Позиція повідомлення у списку.
     */
    public void dishAdded(int position) {
        if (position < containers.size()) {
            containers.get(position).setDishIsAdded(true);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return containers.size();
    }

    /**
     * Внутрішній клас, який представляє ViewHolder для елементів списку повідомлень.
     */
    static class DialogGPTItemViewHolder extends RecyclerView.ViewHolder {
        TextView role, contentText;
        AppCompatImageView addDishButton;

        DialogGPTItemViewHolder(@NonNull View itemView) {
            super(itemView);
            role = itemView.findViewById(R.id.roleItem);
            contentText = itemView.findViewById(R.id.textItem);
            addDishButton = itemView.findViewById(R.id.addDishButton);
        }
    }

    /**
     * Інтерфейс для обробки кліків на кнопку додавання страви.
     */
    public interface AddDishListener { void addDishClick(String text, int position); }
}
