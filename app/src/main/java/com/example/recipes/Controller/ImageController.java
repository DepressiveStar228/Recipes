package com.example.recipes.Controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.example.recipes.Adapter.RecipeAdapter;
import com.example.recipes.Decoration.TextLoadAnimation;
import com.example.recipes.Item.DishRecipe;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Клас для управління зображеннями в додатку.
 * Відповідає за збереження, завантаження, обробку та відображення зображень.
 */
public class ImageController {
    private final Context context;
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Uri currentPhotoUri;
    private AppCompatImageView imageView;
    private Disposable disposable;
    private File baseFolder;

    public ImageController(Context context) {
        this.context = context;
        createImageFolder(); // Створення базової папки для зображень
    }

    /**
     * Перевіряє, чи відбувається завантаження зображення.
     *
     * @return true, якщо завантаження триває, інакше false.
     */
    public boolean isLoading() {
        return isLoading.get();
    }

    /**
     * Створює базову папку для збереження зображень.
     */
    private void createImageFolder() {
        File rootDir = context.getFilesDir();

        baseFolder = new File(rootDir, "recipe_image");
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
            baseFolder = new File(rootDir, "recipe_image");
        }
    }

    /**
     * Створює папку для збереження зображень конкретної страви.
     *
     * @param dishName Назва страви.
     * @return Папка для зображень страви або null, якщо базова папка не існує.
     */
    private File createImageFolderByDish(String dishName) {
        if (baseFolder != null) {
            File imageFolder = new File(baseFolder, dishName);
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }

            return imageFolder;
        } else return null;
    }

    /**
     * Створює файл для збереження зображення.
     *
     * @return Файл для збереження зображення або null, якщо виникла помилка.
     */
    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    /**
     * Зберігає зображення у внутрішню пам'ять пристрою.
     *
     * @param dishName Назва страви.
     * @param bitmap   Зображення у вигляді Bitmap.
     * @return Single<String>, який містить шлях до збереженого зображення або помилку.
     */
    public Single<String> saveImageToInternalStorage(String dishName, Bitmap bitmap) {
        File dishFolder = createImageFolderByDish(dishName);
        String fileName = "image_" + System.currentTimeMillis() + ".png";

        return Single.create(emitter -> {
            if (dishFolder != null && bitmap != null) {
                byte[] imageData = convertBitmapToByteArray(bitmap);
                File imageFile = new File(dishFolder, fileName);

                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(imageData);
                    fos.flush();
                    emitter.onSuccess(imageFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(new Throwable(e.getMessage()));
                }
            } else emitter.onError(new Throwable("Dish folder is null"));
        });
    }

    /**
     * Видаляє файл за його URI.
     *
     * @param uri Шлях до файлу.
     */
    public void deleteFileByUri(String uri) {
        if (uri != null && !uri.isEmpty()) {
            File file = new File(uri);
            if (file.exists()) {
                if (file.delete()) {
                    Log.d("DeleteFile", "Файл видалено: " + file.getAbsolutePath());
                } else {
                    Log.e("DeleteFile", "Помилка видалення файла: " + file.getAbsolutePath());
                }

                File parentDir = file.getParentFile();
                if (parentDir != null && parentDir.isDirectory()) {
                    if (parentDir.listFiles() == null || parentDir.listFiles().length == 0) {
                        if (parentDir.delete()) {
                            Log.d("DeleteFile", "Папка видалена: " + parentDir.getAbsolutePath());
                        } else {
                            Log.e("DeleteFile", "Помилка видалення папки: " + parentDir.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

    /**
     * Відкриває галерею для вибору зображення.
     *
     * @param activity Активність, з якої викликається галерея.
     */
    public void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, 1);
    }

    /**
     * Відкриває камеру для зйомки зображення.
     *
     * @param activity Активність, з якої викликається камера.
     */
    public void openCamera(Activity activity) {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (!activities.isEmpty()) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".file-provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                activity.startActivityForResult(takePictureIntent, 2);
            }
        }
    }

    /**
     * Отримує масив байтів зображення за шляхом до файлу.
     *
     * @param filePath Шлях до файлу зображення.
     * @return Single<byte[]>, який містить масив байтів зображення або помилку.
     */
    public Single<byte[]> getBiteArrayImageFromPath(String filePath) {
        File imageFile = new File(filePath);

        return Single.create(emitter -> {
            if (imageFile.exists()) {
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    byte[] imageData = new byte[(int) imageFile.length()];
                    fis.read(imageData);
                    emitter.onSuccess(imageData);
                } catch (IOException e) {
                    emitter.onError(e);
                }
            } else {
                Log.e("ImageLoad", "Файл не найден: " + filePath);
                emitter.onError(new IOException("Файл не найден: " + filePath));
            }
        });
    }

    /**
     * Конвертує Drawable у Bitmap.
     *
     * @param drawable Drawable для конвертації.
     * @return Bitmap або null, якщо drawable дорівнює null.
     */
    public Bitmap convertDrawbleToBitmap(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return bitmap;
        } else return null;
    }

    /**
     * Конвертує Bitmap у масив байтів.
     *
     * @param bitmap Bitmap для конвертації.
     * @return Масив байтів або null, якщо bitmap дорівнює null або порожній.
     */
    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        if (bitmap != null && bitmap.getByteCount() > 0) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

    /**
     * Копіює файл з одного місця в інше.
     *
     * @param src Вихідний файл.
     * @param dst Файл призначення.
     * @throws IOException Виникає, якщо виникла помилка під час копіювання.
     */
    public void copyFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    /**
     * Декодує масив байтів у Bitmap.
     *
     * @param data Масив байтів зображення.
     * @return Single<Bitmap>, який містить Bitmap або null, якщо дані порожні.
     */
    public Single<Bitmap> decodeByteArrayToBitmap(byte[] data) {
        if (data.length > 0) {
            return Single.create(emitter -> {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);

                options.inSampleSize = calculateInSampleSize(options, 1024, 1024);
                options.inJustDecodeBounds = false;
                emitter.onSuccess(BitmapFactory.decodeByteArray(data, 0, data.length, options));
            });
        } else return Single.just(null);
    }

    /**
     * Обчислює розмір зразка для декодування зображення.
     *
     * @param options   Опції декодування.
     * @param reqWidth  Бажана ширина.
     * @param reqHeight Бажана висота.
     * @return Розмір зразка.
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Single<String> addImageToCache(Bitmap bitmap) {
        return saveImageToInternalStorage("cache", bitmap);
    }

    public Single<String> addImageToCache(String path) {
        return getBiteArrayImageFromPath(path)
                .flatMap(this::decodeByteArrayToBitmap)
                .flatMap(bitmap -> saveImageToInternalStorage("cache", bitmap));
    }

    public void clearCache() {
        File cacheDir = new File("/data/data/com.example.recipes/files/recipe_image/cache");
        deleteDirectory(cacheDir);
    }

    public void clearDishDirectory(String nameDish) {
        File dishDir = new File("/data/data/com.example.recipes/files/recipe_image/" + nameDish);
        deleteDirectory(dishDir);
    }

    public void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    public void setImageView(AppCompatImageView  imageView) {
        this.imageView = imageView;
    }

    public Uri getCurrentPhotoUri() {
        return currentPhotoUri;
    }

    public void dispose() {
        if (disposable != null) disposable.dispose();
    }
}
