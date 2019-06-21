package com.example.musicplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class SongPlayer extends AppCompatActivity {

    MediaPlayer mp = new MediaPlayer();
    ImageView play_or_pause, repeater;
    int askedPos;
    SeekBar seekBar;
    TextView current, end, title;
    boolean playing = true;
    boolean stopped = false;
    NotificationChannel channel;
    NotificationCompat.Builder mBuilder;
    NotificationManager manager;
    static final int NOTIFICATION_ID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        play_or_pause = findViewById(R.id.play_or_pause);
        repeater = findViewById(R.id.repeat);
        seekBar = findViewById(R.id.seekBar);
        askedPos = getIntent().getIntExtra("POS",0);
        createNotificationChannel();
        initiateMusicPlayer(askedPos);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
        });
    }

    public void initiateMusicPlayer (int asked) {
        mp.reset();
        mp = new MediaPlayer();
        title = findViewById(R.id.songTitle);
        title.setText(SongLibrary.songs.get(asked).getSongTitle());
        title.setSelected(true);
        ((TextView) findViewById(R.id.songArtist)).setText(SongLibrary.songs.get(asked).getSongArtist());
        current = findViewById(R.id.currentDuration);
        end = findViewById(R.id.endDuration);
        mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(SongPlayer.this,channel.getId())
                    .setSmallIcon(R.drawable.mp_logo)
                    .setContentTitle(SongLibrary.songs.get(asked).getSongTitle())
                    .setContentText(SongLibrary.songs.get(asked).getSongArtist())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true);
        }
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && mBuilder != null) {
            manager.notify(NOTIFICATION_ID, mBuilder.build());
        }
        playSong(asked);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel(getResources().getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void initiateSeekBar() {
        end.setText(getTimeFromString(mp.getDuration()));
        seekBar.setMax(mp.getDuration());
        seekBar();
    }

    public void playSong(int asked) {
        try {
            mp.setDataSource(SongLibrary.songs.get(asked).getSongLocation());//Write your location here
            mp.prepare();
            mp.start();
            play_or_pause.setImageResource(R.drawable.pause);
            initiateSeekBar();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getTimeFromString(int pos){

        int duration = pos/1000;
        int mins = duration/60;
        int secs = duration%60;
        return (String.format(Locale.getDefault(),"%02d:%02d",mins,secs));
    }

    public void seekBar() {
        final Handler mHandler = new Handler();
        SongPlayer.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                seekBar.setProgress(mp.getCurrentPosition());
                current.setText(getTimeFromString(mp.getCurrentPosition()));
                if(stopped)
                    return;
                else if (!playing && !mp.isPlaying()){
                    play_or_pause.setImageResource(R.drawable.play);
                }
                else if(!mp.isPlaying()) {
                    play_or_pause.setImageResource(R.drawable.play);
                    if(askedPos+1 < SongLibrary.songs.size()){
                        initiateMusicPlayer(askedPos+1);
                        askedPos++;
                    }
                }
                mHandler.postDelayed(this, 500);
            }
        });
    }

    public void onBackPressed() {
        mp.reset();
        stopped = true;
        manager.cancel(NOTIFICATION_ID);
        super.onBackPressed();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_or_pause:
                if (mp.isPlaying()) {
                    mp.pause();
                    playing = false;
                    play_or_pause.setImageResource(R.drawable.play);
                } else {
                    mp.start();
                    playing = true;
                    initiateSeekBar();
                    play_or_pause.setImageResource(R.drawable.pause);
                }
                break;
            case R.id.rewind:
                mp.seekTo(mp.getCurrentPosition() - 5000);
                break;
            case R.id.forward:
                mp.seekTo(mp.getCurrentPosition() + 5000);
                break;
            case R.id.repeat:
                if (mp.isLooping()) {
                    mp.setLooping(false);
                    repeater.setBackgroundResource(R.drawable.controls);
                } else {
                    mp.setLooping(true);
                    repeater.setBackgroundResource(R.drawable.controls_active);
                }
                break;
            case R.id.previous:
                if (askedPos - 1 >= 0) {
                    initiateMusicPlayer(askedPos - 1);
                    askedPos--;
                }
                break;
            case R.id.next:
                if(askedPos+1 < SongLibrary.songs.size()){
                    initiateMusicPlayer(askedPos+1);
                    askedPos++;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        manager.cancelAll();
        super.onDestroy();
    }
}
