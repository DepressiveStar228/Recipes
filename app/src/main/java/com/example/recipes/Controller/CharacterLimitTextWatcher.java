package com.example.recipes.Controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.Toast;

import com.example.recipes.Enum.Limits;
import com.example.recipes.R;
import com.example.recipes.Utils.AnotherUtils;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для обмеження кількості символів у текстовому полі (EditText).
 * Використовує TextWatcher для відстеження змін у тексті та встановлення обмежень.
 */
public class CharacterLimitTextWatcher implements TextWatcher {
    private final Context context;
    private final EditText editText;
    private final int maxCharacters;
    private boolean flagLimit = false;

    /**
     * Конструктор класу CharacterLimitTextWatcher.
     *
     * @param context       Контекст додатку.
     * @param editText      Текстове поле, до якого застосовується обмеження.
     * @param maxCharacters Максимальна кількість символів.
     */
    public CharacterLimitTextWatcher(Context context, EditText editText, int maxCharacters) {
        this.context = context;
        this.editText = editText;
        this.maxCharacters = maxCharacters;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > maxCharacters) {
            if (!flagLimit) {
                // Встановлюємо червоний колір для підсвітки текстового поля
                editText.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
                Toast.makeText(editText.getContext(), context.getString(R.string.warning_char_limit) + "(" + maxCharacters + ")", Toast.LENGTH_SHORT).show();
                flagLimit = true;
            }

            // Видаляємо TextWatcher, щоб уникнути рекурсії
            editText.removeTextChangedListener(this);
            // Обрізаємо текст до максимально дозволеної кількості символів
            editText.setText(s.subSequence(0, maxCharacters));
            // Встановлюємо курсор в кінець тексту
            editText.setSelection(maxCharacters);
            editText.addTextChangedListener(this);
        } else if (flagLimit && s.length() <= maxCharacters) {
            // Якщо кількість символів не перевищує обмеження, але раніше було перевищення
            // Відновлюємо стандартний колір підсвітки
            editText.setBackgroundTintList(ColorStateList.valueOf(AnotherUtils.getAttrColor(context, R.attr.colorHintText)));
            flagLimit = false;
        }
    }

    /**
     * Встановлює обмеження на кількість символів для текстового поля на основі значення з переліку Limits.
     *
     * @param context  Контекст додатку.
     * @param editText Текстове поле, до якого застосовується обмеження.
     * @param limit    Значення з переліку Limits, яке визначає максимальну кількість символів.
     */
    public static void setCharacterLimit(Context context, EditText editText, Limits limit) {
        editText.addTextChangedListener(new CharacterLimitTextWatcher(context, editText, limit.getLimit()));
    }
}
