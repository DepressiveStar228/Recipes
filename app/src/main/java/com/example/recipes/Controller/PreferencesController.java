package com.example.recipes.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.recipes.Config;
import com.example.recipes.R;

import java.util.Locale;
import java.util.Objects;

public class PreferencesController {
    private SharedPreferences preferences;
    public final String language_key = "language";
    public final String theme_key = "theme";
    public final String palette_key = "palette";
    private final String status_ing_hints_key = "ing_hints";
    private String language, theme, palette;
    private Boolean status_ing_hints, tip_shop_list_buttons;
    private Context context;

    public void loadPreferences(Context context){
        this.context = context;
        preferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        language = preferences.getString(language_key, "ua");
        theme = preferences.getString(theme_key, "Light");
        palette = preferences.getString(palette_key, "Brown");
        status_ing_hints = preferences.getBoolean(status_ing_hints_key, true);
        setLocale(language);
        setAppTheme(theme, palette);
        Log.d(getActivityName(context), "Завантаження налаштувань: мова - " + language + ", тема - " + theme + ", палітра - " + palette);
    }

    public void savePreferences(String language, String theme, String palette) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(language_key, language);
        editor.putString(theme_key, theme);
        editor.putString(palette_key, palette);
        editor.apply();
        Log.d(getActivityName(context), "Збереження налаштувань: мова - " + language + ", тема - " + theme + ", палітра - " + palette);
    }

    public void savePreferences(Boolean status_ing_hints) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(status_ing_hints_key, status_ing_hints).apply();
        Log.d(getActivityName(context), "Збереження налаштувань: статус підказок назв інгредієнтів - " + status_ing_hints);
    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        Log.d(getActivityName(context), "Зміна мови");
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

    public void useTip(String tip) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(tip, true).apply();
        Log.d(getActivityName(context), "Підказка " + tip + " використана");
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

    public boolean getStatusUsedTip(String tip) {
        return preferences.getBoolean(tip, false);
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

    public String getLanguage() {
        return language;
    }

    public String getTheme() {
        return theme;
    }

    public String getPalette() {
        return palette;
    }

    public Boolean getStatus_ing_hints() {
        return status_ing_hints;
    }

    private String getActivityName(Context context) {
        return context.getClass().getSimpleName();
    }
}
