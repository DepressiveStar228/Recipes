package com.example.recipes.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.recipes.R;

import java.util.Locale;
import java.util.Objects;

public class PerferencesController {
    private SharedPreferences preferences;
    public String language, theme, palette;
    private Context context;

    public void loadPreferences(Context context){
        this.context = context;
        preferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        language = preferences.getString("language", "uk");
        theme = preferences.getString("theme", "Light");
        palette = preferences.getString("palette", "Brown");
        setLocale(language);
        setAppTheme(theme, palette);
        Log.d(getActivityName(context), "Загруженные настройки: язык - " + language + ", тема - " + theme + ", палитра - " + palette);
    }

    public void savePreferences(String language, String theme, String palette) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("language", language);
        editor.putString("theme", theme);
        editor.putString("palette", palette);
        editor.apply();
        Log.d(getActivityName(context), "Сохранение настроек: язык - " + language + ", тема - " + theme + ", палитра - " + palette);
    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        Log.d(getActivityName(context), "Смена языка");
    }

    public void setAppTheme(String theme, String palette) {
        if (palette.equals("Brown")) {
            if (theme.equals("Light")) {
                context.setTheme(R.style.AppTheme_Brown_Light);
                Log.d(getActivityName(context), "Зміна теми на світлу");
            } else if (theme.equals("Dark")) {
                context.setTheme(R.style.AppTheme_Brown_Dark);
                Log.d(getActivityName(context), "Зміна теми на темну");
            }
        } else if (palette.equals("Blue")) {
            if (theme.equals("Light")) {
                context.setTheme(R.style.AppTheme_Blue_Light);
                Log.d(getActivityName(context), "Зміна теми на світлу");
            } else if (theme.equals("Dark")) {
                context.setTheme(R.style.AppTheme_Blue_Dark);
                Log.d(getActivityName(context), "Зміна теми на темну");
            }
        } else if (palette.equals("Purple")) {
            if (theme.equals("Light")) {
                context.setTheme(R.style.AppTheme_Purple_Light);
                Log.d(getActivityName(context), "Зміна теми на світлу");
            } else if (theme.equals("Dark")) {
                context.setTheme(R.style.AppTheme_Purple_Dark);
                Log.d(getActivityName(context), "Зміна теми на темну");
            }
        } else if (palette.equals("Green")) {
            if (theme.equals("Light")) {
                context.setTheme(R.style.AppTheme_Green_Light);
                Log.d(getActivityName(context), "Зміна теми на світлу");
            } else if (theme.equals("Dark")) {
                context.setTheme(R.style.AppTheme_Green_Dark);
                Log.d(getActivityName(context), "Зміна теми на темну");
            }
        }

        Log.d(getActivityName(context), "Поточний режим: " + AppCompatDelegate.getDefaultNightMode());
    }

    public String getLanguageNameBySpinner(int position) {
        String[] langArray = context.getResources().getStringArray(R.array.language_values);
        return langArray[position];
    }
    public String getThemeNameBySpinner(int position) {
        String[] themeArray = getStringArrayForLocale(R.array.theme_options, "en");
        return themeArray[position];
    }
    public String getPaletteNameBySpinner(int position) {
        String[] paletteArray = getStringArrayForLocale(R.array.palette_options, "en");
        return paletteArray[position];
    }

    public int getIndexLanguage() {
        int index = 0;
        String[] langArray = context.getResources().getStringArray(R.array.language_values);
        for (String land : langArray) {
            if (Objects.equals(land, language)) { return index; }
            else { index++; }
        }
        return 0;
    }

    public int getIndexTheme() {
        int index = 0;
        String[] themeArray = getStringArrayForLocale(R.array.theme_options, "en");
        for (String th : themeArray) {
            if (Objects.equals(th, theme)) { return index; }
            else { index++; }
        }
        return 0;
    }

    public int getIndexPalette() {
        int index = 0;
        String[] paletteArray = getStringArrayForLocale(R.array.palette_options, "en");
        for (String pal : paletteArray) {
            if (Objects.equals(pal, palette)) { return index; }
            else { index++; }
        }
        return 0;
    }

    public String[] getStringArrayForLocale(int resId, String locate) {
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(new Locale(locate));
        Resources localizedResources = context.createConfigurationContext(config).getResources();
        return localizedResources.getStringArray(resId);
    }

    private String getActivityName(Context context) {
        return context.getClass().getSimpleName();
    }
}
