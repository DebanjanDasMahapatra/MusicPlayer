package com.example.musicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class Singing extends Service {

//    public static final String ACTION_PLAY = "com.example.action.PLAY";
//    public static final String ACTION_PAUSE = "com.example.action.PAUSE";
//    public static final String ACTION_RESUME = "com.example.action.PAUSE";
//    public static final String ACTION_INIT = "com.example.action.INIT";
    public static MediaPlayer mediaPlayer = null;
    public static int currentSongIndex = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (Objects.equals(intent.getAction(), ACTION_INIT)) {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setOnPreparedListener(this);
//            mediaPlayer.setOnErrorListener(this);
//        }
//        if (Objects.equals(intent.getAction(), ACTION_PLAY)) {
//            try {
//                mediaPlayer.setDataSource(intent.getStringExtra("LOCATION"));
//                mediaPlayer.prepareAsync();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (Objects.equals(intent.getAction(), ACTION_RESUME)) {
//            mediaPlayer.start();
//        }
//        if (Objects.equals(intent.getAction(), ACTION_PAUSE)) {
//            mediaPlayer.pause();
//        }
        startForeground(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground(Intent i) {
        Intent notificationIntent = new Intent(Singing.this, SongPlayer.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(Singing.this, 0,notificationIntent, 0);
        byte[] albumArt = i.getByteArrayExtra("ALBUM_ART");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(SongLibrary.BACKGROUND_ID, new NotificationCompat.Builder(Singing.this,SongLibrary.channel.getId())
                    .setSmallIcon(R.drawable.mp_logo)
                    .setLargeIcon(albumArt != null ? BitmapFactory.decodeByteArray(albumArt,0,albumArt.length) : BitmapFactory.decodeResource(getResources(),R.drawable.mp_logo))
                    .setContentTitle(i.getStringExtra("TITLE"))
                    .setContentText(i.getStringExtra("ARTIST"))
                    .setContentIntent(pendingIntent).setOngoing(true).build());
            mediaPlayer.start();
        }
    }

    //    @Override
//    public void onPrepared(MediaPlayer mediaPlayer) {
//        mediaPlayer.start();
//    }
//
//    @Override
//    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//        mediaPlayer.reset();
//        return false;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mediaPlayer != null)
//            mediaPlayer.release();
//    }
}
