package com.example.recipes;

public class Config {
    public static final int CHAR_LIMIT_NAME_DISH = 50;
    public static final int CHAR_LIMIT_RECIPE_DISH = 7500;

    public static final int CHAR_LIMIT_NAME_INGREDIENT = 50;
    public static final int CHAR_LIMIT_AMOUNT_INGREDIENT = 12;

    public static final int CHAR_LIMIT_NAME_COLLECTION = 20;

    public static final int CHAR_LIMIT_GPT_PROMPT = 300;

    public static final int COUNT_LIMIT_INGREDIENT = 50;
    public static final int COUNT_LIMIT_SHOP_LIST = 10;

    public static final int ID_MY_RECIPE_COLLECTION = 2;
    public static final int ID_GPT_RECIPE_COLLECTION = 3;
    public static final int ID_IMPORT_RECIPE_COLLECTION = 4;

    public static final int GRAM = 0;
    public static final int KILOGRAM = 1;
    public static final int MILLIGRAM = 2;
    public static final int PIECE = 3;
    public static final int TEASPOON = 4;
    public static final int TABLESPOON = 5;
    public static final int PINCH = 6;
    public static final int GLASS = 7;
    public static final int VOID = 9;

    public static final String COLLECTION_TYPE = "C";
    public static final String SHOP_LIST_TYPE = "S";
    public static final String MENU_TYPE = "M";

    public static final String KEY_DISH = "DISH_ID";

    public static final boolean ADD_MODE = true;
    public static final boolean EDIT_MODE = false;
}
