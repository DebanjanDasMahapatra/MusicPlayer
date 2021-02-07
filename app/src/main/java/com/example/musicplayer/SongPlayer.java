package com.example.musicplayer;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.NotificationCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;

public class SongPlayer extends AppCompatActivity {

    RelativeLayout expanded_view;
    RelativeLayout collapsed_view;
    SlidingUpPanelLayout sliding_layout;
    Context context;
    SeekBar seekBar;
    TextView current, end, title, title2, artist, artist2;
    private Handler myHandler;
    NotificationManager manager;
    ImageView play_or_pause, play_or_pause_mini, repeater, songLogo, previous, next, previous2, next2;
    public boolean isPlaying = false;
    AlertDialog.Builder builder;
    TabLayout tabs;
    public SongAdapter songAdapter;
    public PlaylistAdapter playlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);
        context = SongPlayer.this;
        myHandler = new Handler();
        loadSongs(new OnSongLoadComplete() {
            @Override
            public void onLoadCompleteSuccess(ArrayList<Song> songs) {
                String pln = SongLibrary.playListName;
                SongLibrary.songs = pln.equals("") ? songs : Objects.requireNonNull(SongLibrary.playListSongs.get(pln));
                SongLibrary.originals = songs;
                SongLibrary.cursorCount = songs.size();
                SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(context, getSupportFragmentManager());
                ViewPager viewPager = findViewById(R.id.view_pager);
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs = findViewById(R.id.tabs);
                tabs.setupWithViewPager(viewPager);
                initializeViews();
                manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(Singing.mediaPlayer == null) {
                    Singing.mediaPlayer = new MediaPlayer();
                    if((pln.equals("") && songs.size() > 0) || (!pln.equals("") && Objects.requireNonNull(SongLibrary.playListSongs.get(pln)).size() > 0)) {
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
                        Singing.mediaPlayer.seekTo(seekBar.getProgress());
                    }
                });
            }

            @Override
            public void onLoadCompleteFailure(String errorMessage) {
                Toast.makeText(context,"Error in LOADING method: "+errorMessage,Toast.LENGTH_LONG).show();
            }
        });
    }

    public void initializeSongInMediaPlayer(int i, boolean isFirstTime, boolean shouldStartPlaying, boolean wasLooping) {
        title.setText(SongLibrary.songs.get(i).getSongTitle());
        title2.setText(SongLibrary.songs.get(i).getSongTitle());
        artist.setText(SongLibrary.songs.get(i).getSongArtist());
        artist2.setText(SongLibrary.songs.get(i).getSongArtist());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(SongLibrary.songs.get(i).getSongLocation());
        songLogo.setImageBitmap(getScaledImageBitmap(retriever.getEmbeddedPicture()));
        try {
            Singing.mediaPlayer.setDataSource(SongLibrary.songs.get(i).getSongLocation());
            Singing.mediaPlayer.prepare();
            seekBar.setMax(Singing.mediaPlayer.getDuration());
            current.setText(getTimeString(Singing.mediaPlayer.getCurrentPosition()));
            end.setText(getTimeString(Singing.mediaPlayer.getDuration()));
            Singing.currentSongIndex = i;
            if(isFirstTime) {
                Singing.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if(mediaPlayer.isLooping())
                            return;
                        if(!(isPlaying && SongLibrary.songs.size() > Singing.currentSongIndex + 1)) {
                            isPlaying = false;
                            Intent i = new Intent(context, Singing.class);
                            stopService(i);
                            play_or_pause.setImageResource(R.drawable.play);
                            play_or_pause_mini.setImageResource(R.drawable.play);
                            return;
                        }
                        mediaPlayer.reset();
                        initializeSongInMediaPlayer(++Singing.currentSongIndex, false, true, false);
                    }
                });
            }
            if(shouldStartPlaying) {
                Singing.mediaPlayer.start();
                Singing.mediaPlayer.setLooping(wasLooping);
                updateNotification();
                updateSeekBar.run();
            }
        } catch (Exception e) {
            Toast.makeText(context,"Error in initializeSongInMediaPlayer() method: "+e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void initializeFromBackground() {
        try {
            int i = Singing.currentSongIndex;
            title.setText(SongLibrary.songs.get(i).getSongTitle());
            title2.setText(SongLibrary.songs.get(i).getSongTitle());
            artist.setText(SongLibrary.songs.get(i).getSongArtist());
            artist2.setText(SongLibrary.songs.get(i).getSongArtist());
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(SongLibrary.songs.get(i).getSongLocation());
            songLogo.setImageBitmap(getScaledImageBitmap(retriever.getEmbeddedPicture()));
            seekBar.setMax(Singing.mediaPlayer.getDuration());
            current.setText(getTimeString(Singing.mediaPlayer.getCurrentPosition()));
            end.setText(getTimeString(Singing.mediaPlayer.getDuration()));
            play_or_pause.setImageResource(R.drawable.pause);
            play_or_pause_mini.setImageResource(R.drawable.pause);
            isPlaying = true;
            if (Singing.mediaPlayer.isLooping())
                repeater.setBackgroundResource(R.drawable.repeat_on);
            Singing.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(mediaPlayer.isLooping())
                        return;
                    if(!(isPlaying && SongLibrary.songs.size() > Singing.currentSongIndex + 1)) {
                        isPlaying = false;
                        Intent i = new Intent(context, Singing.class);
                        stopService(i);
                        play_or_pause.setImageResource(R.drawable.play);
                        play_or_pause_mini.setImageResource(R.drawable.play);
                        return;
                    }
                    mediaPlayer.reset();
                    initializeSongInMediaPlayer(++Singing.currentSongIndex, false, true, false);
                }
            });
            updateSeekBar.run();
        } catch (Exception e) {
            Toast.makeText(context,"Error in initializeFromBackground() method: "+e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(Singing.mediaPlayer.getCurrentPosition());
            current.setText(getTimeString(Singing.mediaPlayer.getCurrentPosition()));
            if(isPlaying)
                myHandler.postDelayed(updateSeekBar,100);
        }
    };

    public String getTimeString(int time){
        int duration = time/1000;
        int mins = duration/60;
        int secs = duration%60;
        return String.format(Locale.getDefault(),"%02d:%02d",mins,secs);
    }

    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.play_or_pause:
                case R.id.c_play_or_pause:
                    if (Singing.mediaPlayer.isPlaying()) {
                        Singing.mediaPlayer.pause();
                        isPlaying = false;
                        Intent i = new Intent(context, Singing.class);
                        stopService(i);
                        play_or_pause.setImageResource(R.drawable.play);
                        play_or_pause_mini.setImageResource(R.drawable.play);
                    } else {
                        isPlaying = true;
                        updateSeekBar.run();
                        Intent i = new Intent(context, Singing.class);
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        i.putExtra("TITLE", SongLibrary.songs.get(Singing.currentSongIndex).getSongTitle());
                        i.putExtra("ARTIST", SongLibrary.songs.get(Singing.currentSongIndex).getSongArtist());
                        i.putExtra("LOCATION", SongLibrary.songs.get(Singing.currentSongIndex).getSongLocation());
                        retriever.setDataSource(SongLibrary.songs.get(Singing.currentSongIndex).getSongLocation());
                        i.putExtra("ALBUM_ART",retriever.getEmbeddedPicture());
                        startService(i);
                        play_or_pause.setImageResource(R.drawable.pause);
                        play_or_pause_mini.setImageResource(R.drawable.pause);
                    }
                    break;
                case R.id.repeat:
                    if (Singing.mediaPlayer.isLooping()) {
                        Singing.mediaPlayer.setLooping(false);
                        repeater.setBackgroundResource(R.drawable.repeat_off);
                    } else {
                        Singing.mediaPlayer.setLooping(true);
                        repeater.setBackgroundResource(R.drawable.repeat_on);
                    }
                    break;
                case R.id.previous:
                case R.id.c_previous:
                    if (Singing.mediaPlayer != null && SongLibrary.songs.size() > Singing.currentSongIndex - 1) {
                        boolean wasLooping = Singing.mediaPlayer.isLooping();
                        Singing.mediaPlayer.stop();
                        Singing.mediaPlayer.reset();
                        initializeSongInMediaPlayer(--Singing.currentSongIndex, false, isPlaying, wasLooping);
                    }
                    break;
                case R.id.next:
                case R.id.c_next:
                    if (Singing.mediaPlayer != null && SongLibrary.songs.size() > Singing.currentSongIndex + 1) {
                        boolean wasLooping = Singing.mediaPlayer.isLooping();
                        Singing.mediaPlayer.stop();
                        Singing.mediaPlayer.reset();
                        initializeSongInMediaPlayer(++Singing.currentSongIndex, false, isPlaying, wasLooping);
                    }
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(context,"Error in onClick() method: "+e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    private void updateNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(SongLibrary.songs.get(Singing.currentSongIndex).getSongLocation());
            byte[] albumArt = retriever.getEmbeddedPicture();
            Intent notificationIntent = new Intent(context, WelcomeScreen.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,notificationIntent, 0);
            manager.notify(SongLibrary.BACKGROUND_ID,new NotificationCompat.Builder(context,SongLibrary.channel.getId())
                    .setSmallIcon(R.drawable.mp_logo)
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(albumArt != null ? BitmapFactory.decodeByteArray(albumArt,0,albumArt.length) : BitmapFactory.decodeResource(getResources(),R.drawable.mp_logo))
                    .setContentTitle(SongLibrary.songs.get(Singing.currentSongIndex).getSongTitle())
                    .setContentText(SongLibrary.songs.get(Singing.currentSongIndex).getSongArtist())
                    .setOngoing(true).build());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_info:
                builder.show();
                break;
            case R.id.action_search:
                if(tabs.getSelectedTabPosition() != 0 || !SongLibrary.playListName.equals(""))
                    return false;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (Singing.mediaPlayer != null && !Singing.mediaPlayer.isPlaying()) {
                Singing.mediaPlayer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isUserClickedBackButton = false;
    @Override
    public void onBackPressed() {
        if(sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
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

    private Bitmap getScaledImageBitmap(byte[] resource) {
        if(resource == null)
            return scaleImage(BitmapFactory.decodeResource(getResources(), R.drawable.mp_logo));
        else
            return scaleImage(BitmapFactory.decodeByteArray(resource, 0, resource.length));
    }

    private Bitmap scaleImage(Bitmap source) {
        int imageWidth = source.getWidth();
        int imageHeight = source.getHeight();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int newWidth = displayMetrics.widthPixels;
        float scaleFactor = (float)newWidth/(float)imageWidth;
        int newHeight = (int)(imageHeight * scaleFactor);
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
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        title = findViewById(R.id.songTitle);
        play_or_pause_mini = findViewById(R.id.c_play_or_pause);
        previous2 = findViewById(R.id.c_previous);
        next2 = findViewById(R.id.c_next);
        title2 = findViewById(R.id.c_song_title);
        artist = findViewById(R.id.c_song_artist);
        artist2 = findViewById(R.id.songArtist);
        current = findViewById(R.id.currentDuration);
        end = findViewById(R.id.endDuration);
        builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setNeutralButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        })
                .setCancelable(false).setTitle("App Info")
                .setMessage(getString(R.string.app_info));
    }

    public void loadSongs(OnSongLoadComplete loadComplete) {
        try {
            ArrayList<Song> temp = new ArrayList<>();
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Toast.makeText(context, "Something Went Wrong.", Toast.LENGTH_LONG).show();
            } else if (!cursor.moveToFirst()) {
                Toast.makeText(context, "No Music Found on SD Card.", Toast.LENGTH_LONG).show();
            } else {
                int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int location = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int i = 0, count = cursor.getCount();
                do {
                    Song song = new Song(cursor.getString(title), cursor.getString(artist), cursor.getString(duration), cursor.getString(location), i);
                    temp.add(song);
                    i++;
                    if (temp.size() == count)
                        break;
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            loadComplete.onLoadCompleteSuccess(temp);
        } catch (Exception e) {
            loadComplete.onLoadCompleteFailure(e.toString());
        }
    }
}