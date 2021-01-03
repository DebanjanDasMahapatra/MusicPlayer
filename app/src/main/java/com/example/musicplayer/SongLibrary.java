package com.example.musicplayer;

import android.app.NotificationChannel;

import java.util.ArrayList;

class SongLibrary {
    static ArrayList<Song> songs, originals;
    static int cursorCount = 0;
    static NotificationChannel channel;
    static final int BACKGROUND_ID = 1000;
}
