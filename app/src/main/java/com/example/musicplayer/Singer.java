package com.example.musicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class Singer extends Application implements LifecycleEventObserver {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_MIN;
            SongLibrary.channel = new NotificationChannel(getResources().getString(R.string.channel_id), name, importance);
            SongLibrary.channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(SongLibrary.channel);
        }
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_STOP:
                Toast.makeText(getApplicationContext(),"Running in background",Toast.LENGTH_LONG).show();
                break;
            case ON_START:
                Toast.makeText(getApplicationContext(),"Running in foreground",Toast.LENGTH_LONG).show();
                break;
        }
    }
}
