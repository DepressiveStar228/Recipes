package com.example.recipes.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.util.Log;

/**
 * @author Артем Нікіфоров
 * @version 1.0
 *
 * Утилітарний клас для роботи з файлами через Uri.
 */
public class FileUtils {
    /**
     * Відправляє файл через системний інтент для відправки.
     *
     * @param context Контекст додатка
     * @param uri Uri файла для відправки
     */
    public static void sendFileByUri(Context context, Uri uri) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("application/json");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    /**
     * Видаляє файл за його Uri.
     *
     * @param context Контекст додатка
     * @param uri Uri файла для видалення
     * @return true, якщо файл успішно видалено, false - якщо ні
     */
    public static boolean deleteFileByUri(Context context, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return DocumentsContract.deleteDocument(contentResolver, uri);
            } else {
                return contentResolver.delete(uri, null, null) > 0;
            }
        } catch (Exception e) {
            Log.e("FileUtils", "Помилка при видаленні файла: " + e.getMessage(), e);
            return false;
        }
    }
}

