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

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> mValues;

    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> values) {
        super(context, resource, values);
        mContext = context;
        mValues = values;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(mValues.get(position));

        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.colorText, typedValue, true);
        int textColor = typedValue.data;

        textView.setTextColor(textColor);

        return convertView;
    }
}
