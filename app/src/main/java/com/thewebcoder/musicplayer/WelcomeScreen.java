package com.thewebcoder.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.thewebcoder.musicplayer.ui.main.SongLoader;

import java.util.ArrayList;

public class WelcomeScreen extends AppCompatActivity {

    private static final int RUNTIME_PERMISSION_CODE = 1000;
    private final Context context = WelcomeScreen.this;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        loadingProgressBar = findViewById(R.id.loadingProgress1);
        loadingProgressBar.setScaleY(3f);
        loadingProgressBar.setMin(0);
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{android.Manifest.permission.READ_MEDIA_AUDIO, android.Manifest.permission.POST_NOTIFICATIONS};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        androidRuntimePermission(permissions);
    }

    public void androidRuntimePermission(String[] permissions) {
        if (ContextCompat.checkSelfPermission(context, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeScreen.this, permissions, RUNTIME_PERMISSION_CODE);
        } else if (permissions.length > 1 && ContextCompat.checkSelfPermission(context, permissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeScreen.this, permissions, RUNTIME_PERMISSION_CODE);
        } else {
            navigateToSongPlayer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RUNTIME_PERMISSION_CODE) {
            boolean isDeniedAnyPermission = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isDeniedAnyPermission = true;
                    break;
                }
            }
            if (!isDeniedAnyPermission) {
                navigateToSongPlayer();
            } else {
                Toast.makeText(context, "One or more permission was Denied. Please clear data for this app and try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToSongPlayer() {
        Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
        try {
            DatabaseHelper.openOrCreateDatabases(context);
            new SongLoader(context, new OnSongLoadComplete() {
                @Override
                public void onLoadCompleteSuccess(ArrayList<Song> songs) {
                    SongLibrary.originals = songs;
                    startActivity(new Intent(context, SongPlayer.class));
                }

                @Override
                public void onLoadCompleteFailure(String errorMessage) {
                    finishAffinity();
                    Toast.makeText(context, "Error in Loading Music", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onLoadStart(int songCount) {
                    loadingProgressBar.setMax(songCount);
                }

                @Override
                public void onLoading(int progress) {
                    loadingProgressBar.setProgress(progress);

                }
            }).executeAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}