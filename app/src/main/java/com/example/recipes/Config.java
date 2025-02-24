package com.example.recipes;

public class Config {
    public static final int CHAR_LIMIT_NAME_DISH = 50;
    public static final int CHAR_LIMIT_RECIPE_DISH = 7500;
    public static final int CHAR_LIMIT_PORTION_DISH = 5;

    public static final int CHAR_LIMIT_NAME_INGREDIENT = 50;
    public static final int CHAR_LIMIT_AMOUNT_INGREDIENT = 12;

    public static final int CHAR_LIMIT_NAME_COLLECTION = 20;

    public static final int CHAR_LIMIT_GPT_PROMPT = 300;

    public static final int COUNT_LIMIT_INGREDIENT = 50;
    public static final int COUNT_LIMIT_RECIPE_ITEM = 10;
    public static final int COUNT_LIMIT_SHOP_LIST = 10;

    public static final int ID_MY_RECIPE_COLLECTION = 2;
    public static final int ID_GPT_RECIPE_COLLECTION = 3;
    public static final int ID_IMPORT_RECIPE_COLLECTION = 4;
    public static final Long ID_BLACK_LIST = 5L;

    public static final int VOID = 9;

    public static final String COLLECTION_TYPE = "C";
    public static final String SHOP_LIST_TYPE = "S";
    public static final String MENU_TYPE = "M";
    public static final String BLACK_LIST_TYPE = "B";

    public static final String KEY_DISH = "DISH_ID";
    public static final String KEY_COLLECTION = "COLLECTION_ID";
    public static final String KEY_SHOP_LIST = "SHOP_LIST_ID";

    public static final int ADD_MODE = -1;
    public static final int EDIT_MODE = -2;
    public static final int READ_MODE = -3;

    public static final String TIP_SHOP_LIST_BUTTONS_KEY = "tip_shop_list_buttons";
    public static final String SYSTEM_COLLECTION_TAG = "#%$*@";
}
