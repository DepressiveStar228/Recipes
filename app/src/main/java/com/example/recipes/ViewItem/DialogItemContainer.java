package com.example.recipes.ViewItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.recipes.R;

import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Кастомний контейнер для елементів діалогового вікна.
 * Відображає інформацію про страву з можливістю додавання до БД.
 */
@SuppressLint("ViewConstructor")
public class DialogItemContainer extends ConstraintLayout {
    private boolean dishIsAdded = false;
    private String originalText;
    private TextView role_item;
    private TextView text_item;
    private ImageView add_dish;

    /**
     * Конструктор класу.
     * @param context Контекст додатку
     */
    public DialogItemContainer(Context context) {
        super(context);
        init(context);
    }

    /**
     * Ініціалізує UI елементи контейнера.
     * @param context Контекст додатку
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.dialog_item_container, this, true);
        role_item = findViewById(R.id.roleItem);
        text_item = findViewById(R.id.textItem);
        add_dish = findViewById(R.id.addDishButton);
    }

    /**
     * Встановлює текст для ролі елемента.
     * @param name Текст ролі
     */
    public void setRole_item(String name) {
        role_item.setText(name);
    }

    /**
     * Встановлює основний текст елемента.
     * @param name Текст елемента
     */
    public void setText_item(String name) {
        text_item.setText(name);
    }

    /**
     * Зберігає оригінальний текст елемента.
     * @param text Оригінальний текст
     */
    public void setOriginalText(String text) {
        originalText = text;
    }

    /**
     * Встановлює видимість кнопки додавання.
     * @param visibility Значення видимості (View.VISIBLE, View.INVISIBLE або View.GONE)
     */
    public void setVisibilityAddButton(int visibility) {
        add_dish.setVisibility(visibility);
    }

    /**
     * Отримує основний текст елемента.
     * @return Поточний текст елемента
     */
    public String getText_item() {
        return text_item.getText().toString();
    }

    /**
     * Отримує текст ролі елемента.
     * @return Поточний текст ролі
     */
    public String getRole_item() {
        return role_item.getText().toString();
    }

    /**
     * Отримує видимість кнопки додавання.
     * @return Значення видимості кнопки
     */
    public int getVisibilityAddButton() {
        return add_dish.getVisibility();
    }

    /**
     * Отримує оригінальний текст елемента.
     * @return Збережений оригінальний текст
     */
    public String getOriginalText() { return originalText; }

    /**
     * Перевіряє, чи додано страву до списку.
     * @return Стан додавання страви (true - додано, false - не додано)
     */
    public boolean isDishIsAdded() {
        return dishIsAdded;
    }

    /**
     * Встановлює стан додавання страви.
     * @param dishIsAdded Новий стан додавання
     */
    public void setDishIsAdded(boolean dishIsAdded) {
        this.dishIsAdded = dishIsAdded;
    }
}
