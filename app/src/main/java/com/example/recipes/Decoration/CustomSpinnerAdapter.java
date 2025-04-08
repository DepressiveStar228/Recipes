package com.example.recipes.Decoration;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.recipes.R;

import java.util.List;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Адаптер для кастомного відображення елементів у Spinner.
 * Дозволяє налаштувати зовнішній вигляд елементів відповідно до поточної теми.
 */
public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private List<String> mValues;

    /**
     * Конструктор адаптера.
     *
     * @param context Контекст Activity, де використовується Spinner.
     * @param resource Ресурс макета для елемента Spinner.
     * @param values Список рядків, які відображатимуться у Spinner.
     */
    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> values) {
        super(context, resource, values);
        mContext = context;
        mValues = values;
    }

    /**
     * Створює View для відображення обраного елемента у закритому стані Spinner.
     *
     * @param position Позиція елемента у списку.
     * @param convertView Перевикористаний View.
     * @param parent Батьківський ViewGroup.
     * @return View для відображення елемента.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    /**
     * Створює View для відображення елемента у випадаючому списку Spinner.
     *
     * @param position Позиція елемента у списку.
     * @param convertView Перевикористаний View.
     * @param parent Батьківський ViewGroup.
     * @return View для відображення елемента у випадаючому списку.
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    /**
     * Допоміжний метод для створення View елемента Spinner.
     * Встановлює текст та колір відповідно до поточної теми.
     *
     * @param position Позиція елемента у списку.
     * @param convertView Перевикористаний View.
     * @param parent Батьківський ViewGroup.
     * @return Сформований View для елемента Spinner.
     */
    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(mValues.get(position));

        // Отримання кольору тексту з атрибута теми
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorText, typedValue, true);
        int textColor = typedValue.data;

        textView.setTextColor(textColor);

        return convertView;
    }
}
