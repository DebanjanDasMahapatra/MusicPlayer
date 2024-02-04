package com.thewebcoder.musicplayer;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
    private boolean callStateListenerRegistered = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(intent);
        registerCallStateListener();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForeground(Intent i) {
        Intent notificationIntent = new Intent(SongPlayingService.this, WelcomeScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(SongPlayingService.this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        byte[] albumArt = i.getByteArrayExtra("ALBUM_ART");
        startForeground(SongLibrary.BACKGROUND_ID, new NotificationCompat.Builder(SongPlayingService.this, SongLibrary.channel.getId()).setSmallIcon(R.drawable.mp_logo).setLargeIcon(albumArt != null ? BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length) : BitmapFactory.decodeResource(getResources(), R.drawable.mp_logo)).setContentTitle(i.getStringExtra("TITLE")).setContentText(i.getStringExtra("ARTIST")).setContentIntent(pendingIntent).setOngoing(true).build());
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
