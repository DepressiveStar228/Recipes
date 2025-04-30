package com.example.recipes.Controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.example.recipes.Enum.Tips;
import com.example.recipes.R;

import java.util.Locale;
import java.util.Objects;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для управління налаштуваннями додатку.
 * Відповідає за збереження, завантаження та оновлення налаштувань, таких як мова, тема, палітра тощо.
 */
public class PreferencesController {
    private static PreferencesController instance;
    private SharedPreferences preferences;
    public final String language_key = "language";
    public final String theme_key = "theme";
    public final String palette_key = "palette";
    private final String status_ing_hints_key = "ing_hints";
    private String language, theme, palette;
    private Boolean status_ing_hints, tip_shop_list_buttons = false;
    private Context context;

    private PreferencesController() { }

    /**
     * Повертає єдиний екземпляр класу PreferencesController (Singleton).
     *
     * @return Екземпляр PreferencesController.
     */
    public static synchronized PreferencesController getInstance() {
        if (instance == null) {
            instance = new PreferencesController();
        }
        return instance;
    }

    public void initialization(Context context) {
        this.context = context.getApplicationContext();
        loadPreferences();
    }

    /**
     * Завантажує налаштування з SharedPreferences.
     */
    public void loadPreferences() {
        preferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        language = preferences.getString(language_key, "ua");
        theme = preferences.getString(theme_key, "Light");
        palette = preferences.getString(palette_key, "Brown");
        status_ing_hints = preferences.getBoolean(status_ing_hints_key, true);
        setLocale(language, context);
        Log.d(getActivityName(context), "Завантаження налаштувань: мова - " + language + ", тема - " + theme + ", палітра - " + palette);
    }

    /**
     * Встановлює налаштування для активності (тему та мову).
     *
     * @param activity Активність, для якої встановлюються налаштування.
     */
    public void setPreferencesToActivity(Activity activity) {
        activity.setTheme(getIDTheme());
        setLocale(getLanguageString(), activity);
    }

    /**
     * Зберігає налаштування (мову, тему, палітру) у SharedPreferences.
     *
     * @param language Мова.
     * @param theme    Тема.
     * @param palette  Палітра.
     */
    public void savePreferences(String language, String theme, String palette) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(language_key, language);
        editor.putString(theme_key, theme);
        editor.putString(palette_key, palette);
        editor.apply();
        loadPreferences();
        Log.d(getActivityName(context), "Збереження налаштувань: мова - " + language + ", тема - " + theme + ", палітра - " + palette);
    }

    /**
     * Зберігає налаштування підказок у SharedPreferences.
     *
     * @param status_ing_hints Статус підказок.
     */
    public void savePreferences(boolean status_ing_hints) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(status_ing_hints_key, status_ing_hints);
        editor.apply();
        loadPreferences();
        Log.d(getActivityName(context), "Збереження налаштувань: статус підказок - " + status_ing_hints);

    }

    /**
     * Встановлює локаль (мову) для додатку.
     *
     * @param lang    Мова.
     * @param context Контекст додатку.
     */
    public void setLocale(String lang, Context context) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        Log.d(getActivityName(context), "Зміна мови");
    }

    /**
     * Позначає підказку як використану.
     *
     * @param tip Підказка.
     */
    public void useTip(Tips tip) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(tip.name(), true).apply();
        Log.d(getActivityName(context), "Підказка " + tip + " використана");
    }

    /**
     * Повертає назву мови за позицією у Spinner.
     *
     * @param position Позиція у Spinner.
     * @return Назва мови.
     */
    public String getLanguageNameBySpinner(int position) {
        String[] langArray = context.getResources().getStringArray(R.array.language_values);
        return langArray[position];
    }

    /**
     * Повертає назву теми за позицією у Spinner.
     *
     * @param position Позиція у Spinner.
     * @return Назва теми.
     */
    public String getThemeNameBySpinner(int position) {
        String[] themeArray = getStringArrayForLocale(R.array.theme_options, "en");
        return themeArray[position];
    }

    /**
     * Повертає назву палітри за позицією у Spinner.
     *
     * @param position Позиція у Spinner.
     * @return Назва палітри.
     */
    public String getPaletteNameBySpinner(int position) {
        String[] paletteArray = getStringArrayForLocale(R.array.palette_options, "en");
        return paletteArray[position];
    }

    /**
     * Перевіряє, чи була використана певна підказка.
     *
     * @param tip Підказка.
     * @return true, якщо підказка була використана, інакше false.
     */
    public boolean getStatusUsedTip(Tips tip) {
        return preferences.getBoolean(tip.name(), false);
    }

    /**
     * Повертає статус підказок.
     *
     * @return true, якщо підказки увімкнені, інакше false.
     */
    public Boolean getStatus_ing_hints() {
        return status_ing_hints;
    }

    /**
     * Повертає індекс поточної мови у Spinner.
     *
     * @return Індекс мови.
     */
    public int getIndexLanguage() {
        int index = 0;
        String[] langArray = context.getResources().getStringArray(R.array.language_values);
        for (String land : langArray) {
            if (Objects.equals(land, language)) { return index; }
            else { index++; }
        }
        return 0;
    }

    /**
     * Повертає індекс поточної теми у Spinner.
     *
     * @return Індекс теми.
     */
    public int getIndexTheme() {
        int index = 0;
        String[] themeArray = getStringArrayForLocale(R.array.theme_options, "en");
        for (String th : themeArray) {
            if (Objects.equals(th, theme)) { return index; }
            else { index++; }
        }
        return 0;
    }

    /**
     * Повертає індекс поточної палітри у Spinner.
     *
     * @return Індекс палітри.
     */
    public int getIndexPalette() {
        int index = 0;
        String[] paletteArray = getStringArrayForLocale(R.array.palette_options, "en");
        for (String pal : paletteArray) {
            if (Objects.equals(pal, palette)) { return index; }
            else { index++; }
        }
        return 0;
    }

    /**
     * Повертає масив рядків для певної локалі.
     *
     * @param resId  Ідентифікатор ресурсу.
     * @param locale Локаль.
     * @return Масив рядків.
     */
    public String[] getStringArrayForLocale(int resId, String locale) {
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(new Locale(locale));
        Resources localizedResources = context.createConfigurationContext(config).getResources();
        return localizedResources.getStringArray(resId);
    }

    public String getLanguageString() {
        return language;
    }

    public String getThemeString() {
        return theme;
    }

    public String getPaletteString() {
        return palette;
    }

    /**
     * Повертає ідентифікатор теми на основі поточної палітри та теми.
     *
     * @return Ідентифікатор теми.
     */
    public int getIDTheme() {
        switch (palette) {
            case "Brown" -> {
                if (theme.equals("Light")) return R.style.AppTheme_Brown_Light;
                else if (theme.equals("Dark")) return R.style.AppTheme_Brown_Dark;
            }
            case "Blue" -> {
                if (theme.equals("Light")) return R.style.AppTheme_Blue_Light;
                else if (theme.equals("Dark")) return R.style.AppTheme_Blue_Dark;
            }
            case "Purple" -> {
                if (theme.equals("Light")) return R.style.AppTheme_Purple_Light;
                else if (theme.equals("Dark")) return R.style.AppTheme_Purple_Dark;
            }
            case "Green" -> {
                if (theme.equals("Light")) return R.style.AppTheme_Green_Light;
                else if (theme.equals("Dark")) return R.style.AppTheme_Green_Dark;
            }
            default -> {
                if (theme.equals("Light")) return R.style.AppTheme_Brown_Light;
                else if (theme.equals("Dark")) return R.style.AppTheme_Brown_Dark;
            }
        }
        return R.style.AppTheme_Brown_Light;
    }

    private String getActivityName(Context context) {
        return context.getClass().getSimpleName();
    }
}
