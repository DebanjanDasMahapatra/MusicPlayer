package com.example.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeScreen extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    public static final int RUNTIME_PERMISSION_CODE = 7;
    ContentResolver contentResolver;
    Cursor cursor;
    Uri uri;
    boolean permitted = true;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome_screen);
        AndroidRuntimePermission();
        GetAllMediaMp3Files();

        if(ActivityCompat.checkSelfPermission(WelcomeScreen.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            GetAllMediaMp3Files();
        else if(!permitted)
            Toast.makeText(WelcomeScreen.this,"Grant Permission for Reading External Storage",Toast.LENGTH_SHORT).show();

        mContentView = findViewById(R.id.fullscreen_content);
        hide();
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void GetAllMediaMp3Files(){
        contentResolver = WelcomeScreen.this.getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        cursor = contentResolver.query(uri,null,null,null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        if (cursor == null) {
            Toast.makeText(WelcomeScreen.this,"Something Went Wrong.", Toast.LENGTH_LONG).show();
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(WelcomeScreen.this,"No Music Found on SD Card.", Toast.LENGTH_LONG).show();
            ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
        }
        else {
            int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int location = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int i = 0;
            SongLibrary.cursorCount = cursor.getCount();
            do {
                Song song = new Song(cursor.getString(title),cursor.getString(artist),cursor.getString(duration),cursor.getString(location),i);
                SongLibrary.songs.add(song);
                SongLibrary.originals.add(song);
                i++;
                if(SongLibrary.songs.size() == SongLibrary.cursorCount)
                    break;
            } while (cursor.moveToNext());
            new CountDownTimer(1000, 10) {
                @Override
                public void onTick(long millisUntilFinished) {
                }
                @Override
                public void onFinish() {
                    ((ProgressBar) findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                    startActivity(new Intent(WelcomeScreen.this,MainActivity.class));
                }
            }.start();

        }
    }

    public void AndroidRuntimePermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(WelcomeScreen.this);
                    alert_builder.setMessage("External Storage Permission is Required.");
                    alert_builder.setTitle("Please Grant Permission.");
                    alert_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(WelcomeScreen.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},RUNTIME_PERMISSION_CODE);
                        }
                    });
                    alert_builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = alert_builder.create();
                    dialog.show();
                }
                else {
                    ActivityCompat.requestPermissions(WelcomeScreen.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},RUNTIME_PERMISSION_CODE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(requestCode){
            case RUNTIME_PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    GetAllMediaMp3Files();
                }
                else {
                    permitted = false;
                }
            }
        }
    }
}
