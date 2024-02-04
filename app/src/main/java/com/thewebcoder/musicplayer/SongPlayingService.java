package com.thewebcoder.musicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class SongPlayingService extends Service {

    public static MediaPlayer mediaPlayer = null;
    public static int currentSongIndex = -1;
    private final TelephonyCallback.CallStateListener callStateListener = this::stateListenerAction;
    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            stateListenerAction(state);
        }
    };
    private BroadcastReceiver playActionReceiver;
    private boolean callStateListenerRegistered = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter actionFilter = new IntentFilter();
        actionFilter.addAction(SongLibrary.ACTION_MUSIC_PLAY_OR_PAUSE);
        actionFilter.addAction(SongLibrary.ACTION_MUSIC_REPEAT);
        playActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case SongLibrary.ACTION_MUSIC_PLAY_OR_PAUSE:
                            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                            else mediaPlayer.start();
                            break;
                        case SongLibrary.ACTION_MUSIC_REPEAT:
                            mediaPlayer.setLooping(!mediaPlayer.isLooping());
                            break;
                    }
                }
            }
        };
        registerReceiver(playActionReceiver, actionFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(intent);
        registerCallStateListener();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(playActionReceiver);
    }

    private void startForeground(Intent i) {
        Intent notificationIntent = new Intent(SongPlayingService.this, WelcomeScreen.class);
        PendingIntent playOrPauseIntent = PendingIntent.getBroadcast(this, 0, new Intent(SongLibrary.ACTION_MUSIC_PLAY_OR_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent repeatIntent = PendingIntent.getBroadcast(this, 0, new Intent(SongLibrary.ACTION_MUSIC_REPEAT), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent = PendingIntent.getActivity(SongPlayingService.this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        Bitmap albumArt = i.getParcelableExtra("ALBUM_ART");
        startForeground(SongLibrary.BACKGROUND_ID, new NotificationCompat.Builder(SongPlayingService.this, SongLibrary.channel.getId()).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.mp_logo).setLargeIcon(albumArt != null ? albumArt : BitmapFactory.decodeResource(getResources(), R.drawable.mp_logo)).addAction(R.drawable.play, "Play or Pause", playOrPauseIntent).addAction(R.drawable.repeat_on, "Repeat", repeatIntent).setContentTitle(i.getStringExtra("TITLE")).setContentText(i.getStringExtra("ARTIST")).setContentIntent(pendingIntent).setOngoing(true).build());
        mediaPlayer.start();

    }

    private void registerCallStateListener() {
        if (!callStateListenerRegistered) {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                telephonyManager.registerTelephonyCallback(getMainExecutor(), callStateListener);
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
            callStateListenerRegistered = true;
        }
    }

    private void stateListenerAction(int state) {
        if (state == TelephonyManager.CALL_STATE_RINGING) mediaPlayer.pause();
        else if (state == TelephonyManager.CALL_STATE_IDLE) mediaPlayer.start();
        else if (state == TelephonyManager.CALL_STATE_OFFHOOK) mediaPlayer.pause();
    }
}
