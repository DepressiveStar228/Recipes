package com.example.recipes.Controller;

import android.net.Uri;

import io.reactivex.rxjava3.disposables.Disposable;

public interface ExportCallbackUri {
    void onSuccess(Uri uri);
    void onError(Throwable throwable);
    void getDisposable(Disposable disposable);
}
