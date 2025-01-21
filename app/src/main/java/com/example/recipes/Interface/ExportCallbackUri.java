package com.example.recipes.Interface;

import android.net.Uri;

import io.reactivex.rxjava3.disposables.Disposable;

public interface ExportCallbackUri {
    void onSuccess(Uri uri);
    void onError(Throwable throwable);
    void getDisposable(Disposable disposable);
}
