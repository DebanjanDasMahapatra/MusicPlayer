package com.thewebcoder.musicplayer;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.thewebcoder.musicplayer.ui.main.SectionsPagerAdapter;

import java.util.Locale;
import java.util.Objects;

public class SongPlayer extends AppCompatActivity {

    public boolean isPlaying = false;
    public SongAdapter songAdapter;
    public PlaylistAdapter playlistAdapter;
    private RelativeLayout expanded_view, collapsed_view;
    private SlidingUpPanelLayout sliding_layout;
    private Context context;
    private SeekBar seekBar;
    private TextView current, end, title, title2, artist, artist2;
    private NotificationManager manager;
    private ImageView play_or_pause, play_or_pause_mini, repeater, songLogo;
    private AlertDialog.Builder builder;
    private TabLayout tabs;
    private boolean isUserClickedBackButton = false;
    private Handler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);
        context = SongPlayer.this;
        myHandler = new Handler();
        String pln = SongLibrary.playListName;
        SongLibrary.songs = pln.isEmpty() ? SongLibrary.originals : Objects.requireNonNull(SongLibrary.playListSongs.get(pln));
        SongLibrary.cursorCount = SongLibrary.originals.size();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(context, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        initializeViews();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (SongPlayingService.mediaPlayer == null) {
            SongPlayingService.mediaPlayer = new MediaPlayer();
            if ((pln.isEmpty() && !SongLibrary.originals.isEmpty()) || (!pln.isEmpty() && !Objects.requireNonNull(SongLibrary.playListSongs.get(pln)).isEmpty())) {
                initializeSongInMediaPlayer(0, true, false, false);
            }
        } else {
            initializeFromBackground();
        }
        sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    expanded_view.setVisibility(View.INVISIBLE);
                    collapsed_view.setVisibility(View.VISIBLE);
                } else {
                    expanded_view.setVisibility(View.VISIBLE);
                    collapsed_view.setVisibility(View.INVISIBLE);
                }
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
                SongPlayingService.mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    public void initializeSongInMediaPlayer(int i, boolean isFirstTime, boolean shouldStartPlaying, boolean wasLooping) {
        title.setText(SongLibrary.songs.get(i).getSongTitle());
        title2.setText(SongLibrary.songs.get(i).getSongTitle());
        artist.setText(SongLibrary.songs.get(i).getSongArtist());
        artist2.setText(SongLibrary.songs.get(i).getSongArtist());
        songLogo.setImageBitmap(getScaledImageBitmap(SongLibrary.songs.get(i).getImageBitmap()));
        try {
            SongPlayingService.mediaPlayer.setDataSource(SongLibrary.songs.get(i).getSongLocation());
            SongPlayingService.mediaPlayer.prepare();
            seekBar.setMax(SongPlayingService.mediaPlayer.getDuration());
            current.setText(getTimeString(SongPlayingService.mediaPlayer.getCurrentPosition()));
            end.setText(getTimeString(SongPlayingService.mediaPlayer.getDuration()));
            SongPlayingService.currentSongIndex = i;
            if (isFirstTime) {
                SongPlayingService.mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    if (mediaPlayer.isLooping()) return;
                    if (!(isPlaying && SongLibrary.songs.size() > SongPlayingService.currentSongIndex + 1)) {
                        isPlaying = false;
                        Intent i1 = new Intent(context, SongPlayingService.class);
                        stopService(i1);
                        play_or_pause.setImageResource(R.drawable.play);
                        play_or_pause_mini.setImageResource(R.drawable.play);
                        return;
                    }
                    mediaPlayer.reset();
                    initializeSongInMediaPlayer(++SongPlayingService.currentSongIndex, false, true, false);
                });
            }
            if (shouldStartPlaying) {
                SongPlayingService.mediaPlayer.start();
                SongPlayingService.mediaPlayer.setLooping(wasLooping);
                updateNotification();
                updateSeekBar.run();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error in initializeSongInMediaPlayer() method: " + e, Toast.LENGTH_LONG).show();
        }
    }

    public void initializeFromBackground() {
        try {
            int i = SongPlayingService.currentSongIndex;
            title.setText(SongLibrary.songs.get(i).getSongTitle());
            title2.setText(SongLibrary.songs.get(i).getSongTitle());
            artist.setText(SongLibrary.songs.get(i).getSongArtist());
            artist2.setText(SongLibrary.songs.get(i).getSongArtist());
            songLogo.setImageBitmap(SongLibrary.songs.get(i).getImageBitmap());
            seekBar.setMax(SongPlayingService.mediaPlayer.getDuration());
            current.setText(getTimeString(SongPlayingService.mediaPlayer.getCurrentPosition()));
            end.setText(getTimeString(SongPlayingService.mediaPlayer.getDuration()));
            play_or_pause.setImageResource(R.drawable.pause);
            play_or_pause_mini.setImageResource(R.drawable.pause);
            isPlaying = true;
            if (SongPlayingService.mediaPlayer.isLooping()) repeater.setBackgroundResource(R.drawable.repeat_on);
            SongPlayingService.mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                if (mediaPlayer.isLooping()) return;
                if (!(isPlaying && SongLibrary.songs.size() > SongPlayingService.currentSongIndex + 1)) {
                    isPlaying = false;
                    Intent i1 = new Intent(context, SongPlayingService.class);
                    stopService(i1);
                    play_or_pause.setImageResource(R.drawable.play);
                    play_or_pause_mini.setImageResource(R.drawable.play);
                    return;
                }
                mediaPlayer.reset();
                initializeSongInMediaPlayer(++SongPlayingService.currentSongIndex, false, true, false);
            });
            updateSeekBar.run();
        } catch (Exception e) {
            Toast.makeText(context, "Error in initializeFromBackground() method: " + e, Toast.LENGTH_LONG).show();
        }
    }

    public String getTimeString(int time) {
        int duration = time / 1000;
        int mins = duration / 60;
        int secs = duration % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs);
    }

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.play_or_pause:
                case R.id.c_play_or_pause:
                    if (SongPlayingService.mediaPlayer.isPlaying()) {
                        SongPlayingService.mediaPlayer.pause();
                        isPlaying = false;
                        Intent i = new Intent(context, SongPlayingService.class);
                        stopService(i);
                        play_or_pause.setImageResource(R.drawable.play);
                        play_or_pause_mini.setImageResource(R.drawable.play);
                    } else {
                        isPlaying = true;
                        updateSeekBar.run();
                        Intent i = new Intent(context, SongPlayingService.class);
                        i.putExtra("TITLE", SongLibrary.songs.get(SongPlayingService.currentSongIndex).getSongTitle());
                        i.putExtra("ARTIST", SongLibrary.songs.get(SongPlayingService.currentSongIndex).getSongArtist());
                        i.putExtra("LOCATION", SongLibrary.songs.get(SongPlayingService.currentSongIndex).getSongLocation());
                        i.putExtra("ALBUM_ART", SongLibrary.songs.get(SongPlayingService.currentSongIndex).getImageBitmap());
                        startService(i);
                        play_or_pause.setImageResource(R.drawable.pause);
                        play_or_pause_mini.setImageResource(R.drawable.pause);
                    }
                    break;
                case R.id.repeat:
                    if (SongPlayingService.mediaPlayer.isLooping()) {
                        SongPlayingService.mediaPlayer.setLooping(false);
                        repeater.setBackgroundResource(R.drawable.repeat_off);
                    } else {
                        SongPlayingService.mediaPlayer.setLooping(true);
                        repeater.setBackgroundResource(R.drawable.repeat_on);
                    }
                    break;
                case R.id.previous:
                case R.id.c_previous:
                    if (SongPlayingService.mediaPlayer != null && SongLibrary.songs.size() > SongPlayingService.currentSongIndex - 1) {
                        boolean wasLooping = SongPlayingService.mediaPlayer.isLooping();
                        SongPlayingService.mediaPlayer.stop();
                        SongPlayingService.mediaPlayer.reset();
                        initializeSongInMediaPlayer(--SongPlayingService.currentSongIndex, false, isPlaying, wasLooping);
                    }
                    break;
                case R.id.next:
                case R.id.c_next:
                    if (SongPlayingService.mediaPlayer != null && SongLibrary.songs.size() > SongPlayingService.currentSongIndex + 1) {
                        boolean wasLooping = SongPlayingService.mediaPlayer.isLooping();
                        SongPlayingService.mediaPlayer.stop();
                        SongPlayingService.mediaPlayer.reset();
                        initializeSongInMediaPlayer(++SongPlayingService.currentSongIndex, false, isPlaying, wasLooping);
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error in onClick() method: " + e, Toast.LENGTH_LONG).show();
        }
    }

    private void updateNotification() {
        Bitmap albumArt = SongLibrary.songs.get(SongPlayingService.currentSongIndex).getImageBitmap();
        Intent notificationIntent = new Intent(context, WelcomeScreen.class);
        PendingIntent playOrPauseIntent = PendingIntent.getBroadcast(this, 0, new Intent(SongLibrary.ACTION_MUSIC_PLAY_OR_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent repeatIntent = PendingIntent.getBroadcast(this, 0, new Intent(SongLibrary.ACTION_MUSIC_REPEAT), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        manager.notify(SongLibrary.BACKGROUND_ID, new NotificationCompat.Builder(context, SongLibrary.channel.getId()).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setSmallIcon(R.drawable.mp_logo).setContentIntent(pendingIntent).setLargeIcon(albumArt != null ? albumArt : BitmapFactory.decodeResource(getResources(), R.drawable.mp_logo)).addAction(R.drawable.play, "Play or Pause", playOrPauseIntent).addAction(R.drawable.repeat_on, "Repeat", repeatIntent).setContentTitle(SongLibrary.songs.get(SongPlayingService.currentSongIndex).getSongTitle()).setContentText(SongLibrary.songs.get(SongPlayingService.currentSongIndex).getSongArtist()).setOngoing(true).build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_info:
                builder.show();
                break;
            case R.id.action_search:
                if (tabs.getSelectedTabPosition() != 0 || !SongLibrary.playListName.isEmpty()) return false;
                SearchView searchView = MenuItemCompat.getActionView(item).findViewById(item.getItemId());
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
        }
        return true;
    }    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(SongPlayingService.mediaPlayer.getCurrentPosition());
            current.setText(getTimeString(SongPlayingService.mediaPlayer.getCurrentPosition()));
            if (isPlaying) myHandler.postDelayed(updateSeekBar, 100);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (SongPlayingService.mediaPlayer != null && !SongPlayingService.mediaPlayer.isPlaying()) {
                SongPlayingService.mediaPlayer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else {
            if (!isUserClickedBackButton) {
                Toast.makeText(context, "Press Back Again to Exit", Toast.LENGTH_LONG).show();
                isUserClickedBackButton = true;
            } else {
                finishAffinity();
            }
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

    private Bitmap getScaledImageBitmap(Bitmap source) {
        if (source == null) {
            source = BitmapFactory.decodeResource(getResources(), R.drawable.mp_logo);
        }
        int imageWidth = source.getWidth();
        int imageHeight = source.getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getDisplay().getMetrics(displayMetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }
        int newWidth = displayMetrics.widthPixels;
        float scaleFactor = (float) newWidth / (float) imageWidth;
        int newHeight = (int) (imageHeight * scaleFactor);
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
    }

    public void initializeViews() {
        expanded_view = findViewById(R.id.expanded_view);
        collapsed_view = findViewById(R.id.collapsed_view);
        expanded_view.setVisibility(View.INVISIBLE);
        sliding_layout = findViewById(R.id.slide_view);
        play_or_pause = findViewById(R.id.play_or_pause);
        repeater = findViewById(R.id.repeat);
        songLogo = findViewById(R.id.songImage);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.songTitle);
        play_or_pause_mini = findViewById(R.id.c_play_or_pause);
        title2 = findViewById(R.id.c_song_title);
        artist2 = findViewById(R.id.c_song_artist);
        artist = findViewById(R.id.songArtist);
        current = findViewById(R.id.currentDuration);
        end = findViewById(R.id.endDuration);
        builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setNeutralButton("CLOSE", (dialogInterface, i) -> dialogInterface.dismiss()).setCancelable(false).setTitle("App Info").setMessage(getString(R.string.app_info));
    }



}
