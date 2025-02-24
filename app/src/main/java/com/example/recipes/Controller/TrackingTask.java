package com.example.recipes.Controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class TrackingTask {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long interval;
    private Runnable task;

    public TrackingTask(long interval) {
        this.interval = interval;
    }

    public void startTracking(Runnable onCheck) {
        task = new Runnable() {
            @Override
            public void run() {
                try {
                    onCheck.run();
                } catch (Exception e) {
                    stopTracking();
                    Log.e("TrackingTask", "Error in tracking task: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    handler.postDelayed(this, interval);
                }
            }
        };
        handler.post(task);
    }

    public void stopTracking() {
        if (task != null) {
            handler.removeCallbacks(task);
        }
    }
}
