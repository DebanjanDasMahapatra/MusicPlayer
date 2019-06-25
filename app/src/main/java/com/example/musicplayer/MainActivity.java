package com.example.musicplayer;

import android.app.SearchManager;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    SearchView searchView;
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    LinearLayout expanded_view;
    RelativeLayout collapsed_view;
    SlidingUpPanelLayout sliding_layout;
    MediaPlayer mp = new MediaPlayer();
    ImageView play_or_pause, play_or_pause_mini, repeater;
    static int askedPos;
    SeekBar seekBar;
    TextView current, end, title, title2;
    boolean playing = true, stopped = false;
    static  boolean started = false;
    /*NotificationChannel channel;
    NotificationCompat.Builder mBuilder;
    NotificationManager manager;
    static final int NOTIFICATION_ID = 100;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        recyclerView = findViewById(R.id.songList);
        expanded_view = (LinearLayout) findViewById(R.id.expanded_view);
        collapsed_view = (RelativeLayout) findViewById(R.id.collapsed_view);
        expanded_view.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        songAdapter = new SongAdapter(SongLibrary.songs,MainActivity.this);
        recyclerView.setAdapter(songAdapter);
        play_or_pause = findViewById(R.id.play_or_pause);
        play_or_pause_mini = findViewById(R.id.c_play_or_pause);
        repeater = findViewById(R.id.repeat);
        seekBar = findViewById(R.id.seekBar);
        askedPos = 0;
        //createNotificationChannel();
        initiateMusicPlayer(askedPos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.app_name)+" ("+SongLibrary.cursorCount+" Songs)");
        sliding_layout = (SlidingUpPanelLayout) findViewById(R.id.slide_view);
        sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    expanded_view.setVisibility(View.INVISIBLE);
                    collapsed_view.setVisibility(View.VISIBLE);
                } else {
                    expanded_view.setVisibility(View.VISIBLE);
                    collapsed_view.setVisibility(View.INVISIBLE);
                }
            }
        });
        sliding_layout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

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

    boolean isUserClickedBackButton = false;
    @Override
    public void onBackPressed() {
        if(sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else {
            if (!isUserClickedBackButton) {
                Toast.makeText(MainActivity.this, "Press Back Again to Exit", Toast.LENGTH_LONG).show();
                isUserClickedBackButton = true;
            } else {
                mp.reset();
                stopped = true;
                finishAffinity();
            }
            MainActivity.class.getDeclaredMethods();
            new CountDownTimer(3000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    isUserClickedBackButton = false;
                }
            }.start();
        }
    }

     public void initiateMusicPlayer (int asked) {
        mp.reset();
        mp = new MediaPlayer();
        title = findViewById(R.id.songTitle);
        title.setText(SongLibrary.songs.get(asked).getSongTitle());
        title.setSelected(true);
        title2 = findViewById(R.id.c_song_title);
        title2.setText(SongLibrary.songs.get(asked).getSongTitle());
        ((TextView) findViewById(R.id.songArtist)).setText(SongLibrary.songs.get(asked).getSongArtist());
        ((TextView) findViewById(R.id.c_song_artist)).setText(SongLibrary.songs.get(asked).getSongArtist());
        current = findViewById(R.id.currentDuration);
        end = findViewById(R.id.endDuration);
        /*mBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mBuilder = new NotificationCompat.Builder(MainActivity.this,channel.getId())
                    .setSmallIcon(R.drawable.mp_logo)
                    .setContentTitle(SongLibrary.songs.get(asked).getSongTitle())
                    .setContentText(SongLibrary.songs.get(asked).getSongArtist())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOngoing(true);
        }
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && mBuilder != null) {
            manager.notify(NOTIFICATION_ID, mBuilder.build());
        }*/
        playSong(asked);
    }

    /*private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_desc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            channel = new NotificationChannel(getResources().getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }*/

    public void initiateSeekBar() {
        end.setText(getTimeFromString(mp.getDuration()));
        seekBar.setMax(mp.getDuration());
        seekBar();
    }

    public void playSong(int asked) {
        if(SongLibrary.isPlaying) {
            try {
                mp.setDataSource(SongLibrary.songs.get(asked).getSongLocation());//Write your location here
                mp.prepare();
                mp.start();
                play_or_pause.setImageResource(R.drawable.pause);
                play_or_pause_mini.setImageResource(R.drawable.pause);
                initiateSeekBar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            SongLibrary.isPlaying = true;
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
        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                seekBar.setProgress(mp.getCurrentPosition());
                current.setText(getTimeFromString(mp.getCurrentPosition()));
                if(SongLibrary.currentlyPlaying != askedPos) {
                    askedPos = SongLibrary.currentlyPlaying;
                    initiateMusicPlayer(askedPos);
                }
                else {
                    if (stopped)
                        return;
                    else if (!playing && !mp.isPlaying()) {
                        play_or_pause.setImageResource(R.drawable.play);
                    } else if (!mp.isPlaying()) {
                        play_or_pause.setImageResource(R.drawable.play);
                        if (askedPos + 1 < SongLibrary.songs.size()) {
                            initiateMusicPlayer(askedPos + 1);
                            askedPos++;
                        }
                    }
                }
                mHandler.postDelayed(this, 500);
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_or_pause:
            case R.id.c_play_or_pause:
                if (mp.isPlaying()) {
                    mp.pause();
                    playing = false;
                    play_or_pause.setImageResource(R.drawable.play);
                    play_or_pause_mini.setImageResource(R.drawable.play);
                } else {
                    mp.start();
                    playing = true;
                    initiateSeekBar();
                    play_or_pause.setImageResource(R.drawable.pause);
                    play_or_pause_mini.setImageResource(R.drawable.pause);
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
            case R.id.c_previous:
                if (askedPos - 1 >= 0) {
                    initiateMusicPlayer(askedPos - 1);
                    askedPos--;
                    SongLibrary.currentlyPlaying--;
                }
                break;
            case R.id.next:
            case R.id.c_next:
                if(askedPos+1 < SongLibrary.songs.size()){
                    initiateMusicPlayer(askedPos+1);
                    askedPos++;
                    SongLibrary.currentlyPlaying++;
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                songAdapter.getFilter().filter(s);
                return true;
            }
        });
        return true;
    }
}
