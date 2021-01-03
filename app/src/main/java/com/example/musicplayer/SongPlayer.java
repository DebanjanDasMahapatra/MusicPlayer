package com.example.musicplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SongPlayer extends AppCompatActivity {

    Context context;
    RecyclerView recyclerView;
    SongAdapter songAdapter;
    LinearLayout expanded_view;
    RelativeLayout collapsed_view;
    SlidingUpPanelLayout sliding_layout;
    SeekBar seekBar;
    TextView current, end, title, title2, artist, artist2;
    private Handler myHandler;
    NotificationManager manager;
    ImageView play_or_pause, play_or_pause_mini, repeater, songLogo, previous, next, previous2, next2;
    boolean isPlaying = false, playPrevious = false, playNext = false;//, isWorking = false, isPaused = false;
    int requestedSongIndex = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = SongPlayer.this;
        myHandler = new Handler();
        loadSongs(new OnSongLoadComplete() {
            @Override
            public void onLoadCompleteSuccess(ArrayList<Song> songs) {
                SongLibrary.songs = songs;
                SongLibrary.originals = songs;
                SongLibrary.cursorCount = songs.size();
                initializeViews();
                manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if(Singing.mediaPlayer == null) {
                    Singing.mediaPlayer = new MediaPlayer();
                    //isPaused = true;
                    if(songs.size() > 0) {
                        initializeSongInMediaPlayer(0,true,false);
                    }
                } else
                    initializeFromBackground();
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
                        Singing.mediaPlayer.seekTo(seekBar.getProgress());
                    }
                });
            }

            @Override
            public void onLoadCompleteFailure(String errorMessage) {
                Toast.makeText(context,"Some error occurred: "+errorMessage,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeSongInMediaPlayer(int i, boolean isFirstTime, boolean shouldStartPlaying) {
        title.setText(SongLibrary.songs.get(i).getSongTitle());
        title2.setText(SongLibrary.songs.get(i).getSongTitle());
        artist.setText(SongLibrary.songs.get(i).getSongArtist());
        artist2.setText(SongLibrary.songs.get(i).getSongArtist());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(SongLibrary.songs.get(i).getSongLocation());
        byte[] albumArt = retriever.getEmbeddedPicture();
        if(albumArt != null)
            songLogo.setImageBitmap(BitmapFactory.decodeByteArray(albumArt,0,albumArt.length));
        else
            songLogo.setImageResource(R.drawable.mp_logo);
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
                        if(!(isPlaying && SongLibrary.songs.size() > Singing.currentSongIndex + 1))
                            return;
                        mediaPlayer.reset();
                        initializeSongInMediaPlayer(++Singing.currentSongIndex, false, true);
                    }
                });
                playMusic.run();
            }
            if(shouldStartPlaying) {
                Singing.mediaPlayer.start();
                updateNotification();
                updateSeekBar.run();
            }
        } catch (Exception e) {
            Toast.makeText(context,"Some error occurred: "+e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    public void initializeFromBackground() {
        int i = Singing.currentSongIndex;
        title.setText(SongLibrary.songs.get(i).getSongTitle());
        title2.setText(SongLibrary.songs.get(i).getSongTitle());
        artist.setText(SongLibrary.songs.get(i).getSongArtist());
        artist2.setText(SongLibrary.songs.get(i).getSongArtist());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(SongLibrary.songs.get(i).getSongLocation());
        byte[] albumArt = retriever.getEmbeddedPicture();
        if(albumArt != null)
            songLogo.setImageBitmap(BitmapFactory.decodeByteArray(albumArt,0,albumArt.length));
        else
            songLogo.setImageResource(R.drawable.mp_logo);
        seekBar.setMax(Singing.mediaPlayer.getDuration());
        current.setText(getTimeString(Singing.mediaPlayer.getCurrentPosition()));
        end.setText(getTimeString(Singing.mediaPlayer.getDuration()));
        play_or_pause.setImageResource(R.drawable.pause);
        play_or_pause_mini.setImageResource(R.drawable.pause);
        isPlaying = true;
        Singing.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(!(isPlaying && SongLibrary.songs.size() > Singing.currentSongIndex + 1))
                    return;
                mediaPlayer.reset();
                initializeSongInMediaPlayer(++Singing.currentSongIndex, false, true);
            }
        });
        playMusic.run();
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(Singing.mediaPlayer.getCurrentPosition());
            current.setText(getTimeString(Singing.mediaPlayer.getCurrentPosition()));
            if(Singing.mediaPlayer.isPlaying())
                myHandler.postDelayed(updateSeekBar,100);
//            else if(!isPlaying)
//                isWorking = false;
        }
    };

    private Runnable playMusic = new Runnable() {
        @Override
        public void run() {
//            if(isPaused && Singing.mediaPlayer.isPlaying()) {
//                Singing.mediaPlayer.pause();
//                isPlaying = false;
//            }
            if (Singing.mediaPlayer != null && playPrevious && SongLibrary.songs.size() > Singing.currentSongIndex - 1) {
                Singing.mediaPlayer.stop();
                Singing.mediaPlayer.reset();
                playPrevious = false;
                initializeSongInMediaPlayer(--Singing.currentSongIndex, false, isPlaying);
            } else if (Singing.mediaPlayer != null && playNext && SongLibrary.songs.size() > Singing.currentSongIndex + 1) {
                Singing.mediaPlayer.stop();
                Singing.mediaPlayer.reset();
                playNext = false;
                initializeSongInMediaPlayer(++Singing.currentSongIndex, false, isPlaying);
            } else if (Singing.mediaPlayer != null && requestedSongIndex > -1 && SongLibrary.songs.size() > requestedSongIndex) {
                Singing.mediaPlayer.reset();
                Singing.currentSongIndex = requestedSongIndex;
                requestedSongIndex = -1;
                initializeSongInMediaPlayer(Singing.currentSongIndex, false, isPlaying);
            }
//            if (Singing.mediaPlayer != null && Singing.mediaPlayer.isPlaying() && isPlaying && !isWorking) {
//                updateSeekBar.run();
//                isWorking = true;
//            }
            myHandler.postDelayed(playMusic, 100);
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
//                        isPaused = true;
                        isPlaying = false;
                        Intent i = new Intent(context, Singing.class);
                        stopService(i);
                        play_or_pause.setImageResource(R.drawable.play);
                        play_or_pause_mini.setImageResource(R.drawable.play);
                    } else {
//                        isPaused = false;
                        isPlaying = true;
                        updateSeekBar.run();
                        Intent i = new Intent(context, Singing.class);
                        i.putExtra("TITLE",SongLibrary.songs.get(Singing.currentSongIndex).getSongTitle());
                        i.putExtra("ARTIST",SongLibrary.songs.get(Singing.currentSongIndex).getSongTitle());
                        i.putExtra("LOCATION",SongLibrary.songs.get(Singing.currentSongIndex).getSongLocation());
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
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
                        repeater.setBackgroundResource(R.drawable.controls);
                    } else {
                        Singing.mediaPlayer.setLooping(true);
                        repeater.setBackgroundResource(R.drawable.controls_active);
                    }
                    break;
                case R.id.previous:
                case R.id.c_previous:
                    playPrevious = true;
                    break;
                case R.id.next:
                case R.id.c_next:
                    playNext = true;
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(context,"An error occurred: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
    
    private void updateNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(SongLibrary.songs.get(Singing.currentSongIndex).getSongLocation());
            byte[] albumArt = retriever.getEmbeddedPicture();
            Intent notificationIntent = new Intent(context, SongPlayer.class);
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
    public void onDestroy() {
        super.onDestroy();
        if (Singing.mediaPlayer != null && !Singing.mediaPlayer.isPlaying())
            Singing.mediaPlayer.release();
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

    public void initializeViews() {
        recyclerView = findViewById(R.id.songList);
        expanded_view = findViewById(R.id.expanded_view);
        collapsed_view = findViewById(R.id.collapsed_view);
        expanded_view.setVisibility(View.INVISIBLE);
        sliding_layout = findViewById(R.id.slide_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        songAdapter = new SongAdapter(SongLibrary.songs,context, new OnSongClick() {
            @Override
            public void songClick(int clickedIndex) {
                requestedSongIndex = clickedIndex;
            }
        });
        recyclerView.setAdapter(songAdapter);
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
            loadComplete.onLoadCompleteFailure(e.getMessage());
        }
    }
}