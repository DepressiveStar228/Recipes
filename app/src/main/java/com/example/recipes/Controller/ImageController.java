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
import android.util.Log;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.example.recipes.Adapter.RecipeAdapter;
import com.example.recipes.Item.DishRecipe;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ImageController {
    private final Context context;
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private Uri currentPhotoUri;
    private AppCompatImageView imageView;
    private File baseFolder;

    public ImageController(Context context) {
        this.context = context;
        createImageFolder();
    }

    public boolean isLoading() {
        return isLoading.get();
    }

    private void createImageFolder() {
        File rootDir = context.getFilesDir();

        baseFolder = new File(rootDir, "recipe_image");
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
            baseFolder = new File(rootDir, "recipe_image");
        }
    }

    private File createImageFolderByDish(String dishName) {
        if (baseFolder != null) {
            File imageFolder = new File(baseFolder, dishName);
            if (!imageFolder.exists()) {
                imageFolder.mkdirs();
            }

            return imageFolder;
        } else return null;
    }

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

    public Single<String> saveImageToInternalStorage(String dishName, Bitmap bitmap) {
        File dishFolder = createImageFolderByDish(dishName);
        String fileName = "image_" + System.currentTimeMillis() + ".png";

        return Single.create(emitter -> {
            if (dishFolder != null) {
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

    public Single<byte[]> loadImageFromPath(String filePath) {
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

    public void deleteFileByUri(String uri) {
        if (uri != null && !uri.isEmpty()) {
            File file = new File(uri);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d("DeleteFile", "Файл видалено: " + file.getAbsolutePath());
                } else {
                    Log.e("DeleteFile", "Помилка видалення файла: " + file.getAbsolutePath());
                }
            }

            File parentDir = file.getParentFile();
            if (parentDir != null && parentDir.isDirectory()) {
                if (parentDir.listFiles() == null || parentDir.listFiles().length == 0) {
                    boolean dirDeleted = parentDir.delete();
                    if (dirDeleted) {
                        Log.d("DeleteFile", "Папка видалена: " + parentDir.getAbsolutePath());
                    } else {
                        Log.e("DeleteFile", "Помилка видалення папки: " + parentDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void openGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, 1);
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void openCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 100);
        }

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

    public void setImageToImageView(Uri imageUri, RecipeAdapter adapter, Runnable isLoadImage) {
        if (isLoadImage == null) {
            Log.e("SetImageController", "Runnable isLoadImage is null!");
            return;
        }

        if (imageView == null) {
            Log.e("SetImageController", "ImageView is null!");
            return;
        }

        if (imageUri == null) {
            Log.e("SetImageController", "Image URI is null!");
            return;
        }

        Glide.with(context)
                .load(imageUri)
                .into(new ImageViewTarget<Drawable>(imageView) {
                    @Override
                    protected void setResource(@Nullable Drawable resource) {
                        if (resource == null) {
                            Log.e("SetImageController", "Drawable resource is null!");
                            return;
                        }
                        isLoading.set(true);

                        imageView.setImageDrawable(resource);

                        Bitmap bitmap = convertDrawbleToBitmap(resource);
                        if (bitmap != null) {
                            DishRecipe dishRecipe = adapter.getCurrentItem();
                            dishRecipe.setBitmap(bitmap);
                            adapter.upItem(dishRecipe, dishRecipe.getPosition());
                            isLoadImage.run();
                        } else {
                            Log.e("SetImageController", "Error convert Drawable to Bitmap");
                        }

                        isLoading.set(false);
                    }
                });
    }

    public Bitmap convertDrawbleToBitmap(Drawable drawable) {
        if (drawable != null) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return bitmap;
        } else return null;
    }

    public byte[] convertBitmapToByteArray(Bitmap bitmap) {
        if (bitmap != null && bitmap.getByteCount() > 0) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
        return null;
    }

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

    public void setImageView(AppCompatImageView  imageView) {
        this.imageView = imageView;
    }

    public Uri getCurrentPhotoUri() {
        return currentPhotoUri;
    }
}
