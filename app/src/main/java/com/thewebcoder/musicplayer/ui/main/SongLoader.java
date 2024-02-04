package com.thewebcoder.musicplayer.ui.main;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import com.thewebcoder.musicplayer.OnSongLoadComplete;
import com.thewebcoder.musicplayer.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SongLoader implements Callable<ArrayList<Song>> {

    private final Context context;
    private final Executor executor;
    private final Handler handler;
    private final OnSongLoadComplete loadComplete;

    public SongLoader(Context context, OnSongLoadComplete loadComplete) {
        this.context = context;
        this.loadComplete = loadComplete;
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public ArrayList<Song> call() {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, MediaStore.Audio.Media.TITLE);
        ArrayList<Song> songList = new ArrayList<>();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            return null;
        } else {
            int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int location = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int position = 0;
            byte[] albumArt;
            loadComplete.onLoadStart(cursor.getCount());
            do {
                try {
                    retriever.setDataSource(cursor.getString(location));
                    albumArt = retriever.getEmbeddedPicture();
                    Bitmap imageBitmap = albumArt != null ? BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length) : null;
                    Song song = new Song(cursor.getString(title), cursor.getString(artist), cursor.getString(duration), cursor.getString(location), position, imageBitmap);
                    songList.add(song);
                    position++;
                    loadComplete.onLoading(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        try {
            retriever.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cursor.close();
        return songList;
    }

    public void executeAsync() {
        executor.execute(() -> {
            Looper.prepare();
            try {
                ArrayList<Song> songList = call();
                handler.post(() -> {
                    if (songList != null) {
                        loadComplete.onLoadCompleteSuccess(songList);
                    } else {
                        loadComplete.onLoadCompleteFailure("Something Went Wrong.");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                loadComplete.onLoadCompleteFailure(e.toString());
            }
        });
    }
}
