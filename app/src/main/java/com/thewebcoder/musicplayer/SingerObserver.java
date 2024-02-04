package com.thewebcoder.musicplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class SingerObserver extends Application implements LifecycleEventObserver {
    @Override
    public void onCreate() {
        super.onCreate();
        SongLibrary.channel = new NotificationChannel(getResources().getString(R.string.channel_id), getString(R.string.channel_name), NotificationManager.IMPORTANCE_MIN);
        SongLibrary.channel.setDescription(getString(R.string.channel_desc));
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(SongLibrary.channel);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_STOP:
                Toast.makeText(getApplicationContext(), "Running in background", Toast.LENGTH_LONG).show();
                break;
            case ON_START:
                Toast.makeText(getApplicationContext(), "Running in foreground", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
