package com.example.recipes.Controller;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.example.recipes.Enum.DishRecipeType;
import com.example.recipes.Enum.ID_System_Collection;
import com.example.recipes.Interface.ExportCallbackUri;
import com.example.recipes.Item.Collection;
import com.example.recipes.Item.Dish;
import com.example.recipes.Item.DishRecipe;
import com.example.recipes.Utils.RecipeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для імпорту та експорту даних рецептів.
 * Відповідає за збереження та відновлення даних рецептів у вигляді ZIP-архівів.
 */
public class ImportExportController {
    private static final String EXPORT_FILE = "export_recipe";
    private static final String IMPORT_FILE = "import_recipe";
    private static final String DISHES_JSON = "dish_data";
    private final Context context;
    private final ImageController imageController;

    public ImportExportController(Context context) {
        this.context = context;
        this.imageController = new ImageController(context);
    }

    /**
     * Імпортує дані рецептів з ZIP-файлу.
     *
     * @param ctx Контекст додатку.
     * @param uri     URI ZIP-файлу.
     * @return Completable, який повідомляє про успішний імпорт або помилку.
     */
    public Completable importRecipeDataToFile(Context ctx, Uri uri) {
        return Completable.fromAction(() -> {
            File importDir = new File(ctx.getExternalFilesDir(null), IMPORT_FILE);
            if (!importDir.exists()) {
                importDir.mkdirs(); // Створення папки для імпорту, якщо вона не існує
            }

            try (InputStream inputStream = ctx.getContentResolver().openInputStream(uri);
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    File outputFile = new File(importDir, entryName);

                    File parentDir = outputFile.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs(); // Створення батьківської папки, якщо вона не існує
                    }

                    if (zipEntry.isDirectory()) {
                        outputFile.mkdirs(); // Створення папки, якщо це директорія
                    } else {
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = zipInputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, length); // Запис даних у файл
                            }
                        }
                    }
                    zipInputStream.closeEntry(); // Закриття поточного запису в ZIP
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        })
                .andThen(processImportedData() // Обробка імпортованих даних
                .flatMapCompletable(dishes -> {
                    if (!dishes.isEmpty()) return saveToDataBaseDishes(dishes);
                    else return Completable.error(new Throwable("Dishes is empty"));
                })
                .andThen(Completable.fromAction(this::cleanupImportedFiles)) // Очищення тимчасових файлів
        );
    }

    /**
     * Обробляє імпортовані дані з JSON-файлу.
     *
     * @return Single<ArrayList<Dish>>, який містить список страв або помилку.
     */
    private Single<ArrayList<Dish>> processImportedData() {
        return Single.create(emitter -> {
            File importDir = new File(context.getExternalFilesDir(null), IMPORT_FILE);
            File jsonFile = new File(importDir, "dish_data.json");

            if (jsonFile.exists()) {
                ArrayList<Dish> dishes = new ArrayList<>();

                try (FileInputStream fis = new FileInputStream(jsonFile);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

                    StringBuilder jsonString = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonString.append(line); // Зчитування JSON-даних
                    }

                    Gson gson = new Gson();
                    Type dishListType = new TypeToken<ArrayList<Dish>>() { }.getType();
                    dishes = gson.fromJson(jsonString.toString(), dishListType); // Парсинг JSON у список страв

                    emitter.onSuccess(dishes);

                } catch (IOException e) {
                    emitter.onError(new Throwable(e.getMessage()));
                }
            }
        });
    }

    /**
     * Зберігає імпортовані страви до бази даних.
     *
     * @param dishes Список страв.
     * @return Completable, який повідомляє про успішне збереження або помилку.
     */
    private Completable saveToDataBaseDishes(ArrayList<Dish> dishes) {
        RecipeUtils utils = RecipeUtils.getInstance(context);
        for (Dish dish : dishes) {
            for (DishRecipe dishRecipe : dish.getRecipes()) {
                if (dishRecipe.getTypeData() == DishRecipeType.IMAGE) {
                    File dishDir = new File(context.getExternalFilesDir(null), IMPORT_FILE);
                    if (dishDir.exists()) dishRecipe.setTextData(dishDir.getAbsolutePath() + "/" + dishRecipe.getTextData());
                }
            }
        }

        return utils.ByDish().addAll(dishes, ID_System_Collection.ID_IMPORT_RECIPE.getId())
                .flatMapCompletable(status -> {
                    if (status) return Completable.complete();
                    else return Completable.error(new Throwable("Помилка додавання страв до бази даних"));
                });
    }

    /**
     * Очищує тимчасові файли після імпорту.
     */
    private void cleanupImportedFiles() {
        File importDir = new File(context.getExternalFilesDir(null), IMPORT_FILE);
        if (importDir.exists()) {
            deleteFolder(importDir);
        }
    }

    /**
     * Експортує дані рецептів з колекції у ZIP-файл.
     *
     * @param ctx    Контекст додатку.
     * @param collection Колекція страв для експорту.
     * @param callback   Callback для повернення результату.
     */
    public void exportRecipeData(Context ctx, Collection collection, ExportCallbackUri callback) {
        RecipeUtils utils = RecipeUtils.getInstance(ctx);
        Disposable disposable = utils.ByCollection().getDishes(collection.getId())
                .flatMap(dishes -> createExportDir(ctx, collection.getName(), new ArrayList<>(dishes)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zipFile  -> {
                    if (zipFile.exists()) {
                        callback.onSuccess(FileProvider.getUriForFile(ctx, "com.example.recipes.file-provider", zipFile));
                    }
                    else callback.onError(new Throwable("ZIP file do not exist"));
                });

        callback.getDisposable(disposable);
    }

    /**
     * Експортує дані рецептів у ZIP-файл.
     *
     * @param ctx  Контекст додатку.
     * @param dishes   Список страв для експорту.
     * @param callback Callback для повернення результату.
     */
    public void exportRecipeData(Context ctx, List<Dish> dishes, ExportCallbackUri callback) {
        Disposable disposable = createExportDir(ctx, EXPORT_FILE, new ArrayList<>(dishes))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zipFile  -> {
                    if (zipFile.exists()) {
                        callback.onSuccess(FileProvider.getUriForFile(ctx, "com.example.recipes.file-provider", zipFile));
                    }
                    else callback.onError(new Throwable("ZIP file do not exist"));
                });

        callback.getDisposable(disposable);
    }

    /**
     * Експортує одну страву у ZIP-файл.
     *
     * @param ctx  Контекст додатку.
     * @param dish     Страва для експорту.
     * @param callback Callback для повернення результату.
     */
    public void exportDish(Context ctx, Dish dish, ExportCallbackUri callback) {
        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(dish);

        Disposable disposable = createExportDir(ctx, dish.getName(), dishes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(zipFile  -> {
                    if (zipFile.exists()) {
                        callback.onSuccess(FileProvider.getUriForFile(ctx, "com.example.recipes.file-provider", zipFile));
                    }
                    else callback.onError(new Throwable("ZIP file do not exist"));
                });

        callback.getDisposable(disposable);
    }

    /**
     * Створює директорію для експорту даних.
     *
     * @param ctx Контекст додатку.
     * @param nameDir Назва директорії.
     * @param dishes  Список страв для експорту.
     * @return Single<File>, який містить ZIP-файл або помилку.
     */
    private Single<File> createExportDir(Context ctx, String nameDir, ArrayList<Dish> dishes) {
        return Single.create(emitter -> {
            ArrayList<Dish> exportDishes = new ArrayList<>();

            File exportDir = new File(ctx.getExternalFilesDir(null), nameDir);
            if (!exportDir.exists()) exportDir.mkdirs();

            for (Dish dish : dishes) {
                File dishDir = new File(exportDir, dish.getName());

                for (DishRecipe dishRecipe : dish.getRecipes()) {
                    if (!dishDir.exists()) dishDir.mkdirs();

                    if (dishRecipe.getTypeData() == DishRecipeType.IMAGE && !dishRecipe.getTextData().isEmpty()) {
                        File image = new File(dishRecipe.getTextData());
                        if (image.exists()) {
                            File newImage = new File(dishDir, image.getName());
                            imageController.copyFile(image, newImage);
                            dishRecipe.setTextData(dish.getName() + "/" + newImage.getName());
                        }
                    }
                }

                exportDishes.add(dish);
            }

            Gson gson = new Gson();
            String jsonString = gson.toJson(exportDishes);
            File jsonFile = new File(exportDir, DISHES_JSON + ".json");

            try (FileOutputStream fos = new FileOutputStream(jsonFile);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
                writer.write(jsonString);
            } catch (IOException e) {
                deleteFolder(exportDir);
                emitter.onError(e);
            }

            File zipFile = new File(ctx.getExternalFilesDir(null), nameDir + ".zip");
            zipFolder(exportDir, zipFile);

            deleteFolder(exportDir);
            emitter.onSuccess(zipFile);
        });
    }

    /**
     * Видаляє папку та всі її вміст.
     *
     * @param folder Папка для видалення.
     */
    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                deleteFolder(file);
            }
        }
        folder.delete();
    }

    /**
     * Архівує папку у ZIP-файл.
     *
     * @param sourceFolder Папка для архівації.
     * @param zipFile      ZIP-файл для створення.
     * @throws IOException Виникає, якщо виникла помилка під час архівації.
     */
    private void zipFolder(File sourceFolder, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipFile(sourceFolder, sourceFolder.getPath().length() + 1, zos); // Архівація папки
        }
    }

    /**
     * Архівує файл або папку у ZIP-архів.
     *
     * @param file          Файл або папка для архівації.
     * @param basePathLength Довжина базового шляху для формування відносного шляху в архіві.
     * @param zos           ZipOutputStream для запису даних.
     * @throws IOException Виникає, якщо виникла помилка під час архівації.
     */
    private void zipFile(File file, int basePathLength, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                zipFile(childFile, basePathLength, zos); // Рекурсивна архівація вмісту папки
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                String entryName = file.getPath().substring(basePathLength);
                zos.putNextEntry(new ZipEntry(entryName)); // Додавання файлу до архіву

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length); // Запис даних у архів
                }
                zos.closeEntry(); // Закриття поточного запису
            }
        }
    }

}
