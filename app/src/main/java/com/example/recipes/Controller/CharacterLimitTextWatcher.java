package com.example.recipes.Controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.Toast;

import com.example.recipes.R;

public class CharacterLimitTextWatcher implements TextWatcher {
    private final Context context;
    private final EditText editText;
    private final int maxCharacters;
    private boolean flagLimit = false;

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
                editText.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
                Toast.makeText(editText.getContext(), context.getString(R.string.warning_char_limit), Toast.LENGTH_SHORT).show();
                flagLimit = true;
            }

            editText.removeTextChangedListener(this);
            editText.setText(s.subSequence(0, maxCharacters));
            editText.setSelection(maxCharacters);
            editText.addTextChangedListener(this);
        } else if (flagLimit && s.length() <= maxCharacters) {
            editText.setBackgroundTintList(getColorFromAttr(context, com.google.android.material.R.attr.colorControlNormal));
            flagLimit = false;
        }
    }

    public static void setCharacterLimit(Context context, EditText editText, int maxCharacters) {
        editText.addTextChangedListener(new CharacterLimitTextWatcher(context, editText, maxCharacters));
    }

    private ColorStateList getColorFromAttr(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        int color = typedValue.data;
        return ColorStateList.valueOf(color);
    }
}
