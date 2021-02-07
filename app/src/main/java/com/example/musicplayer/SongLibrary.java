package com.example.musicplayer;

import android.app.NotificationChannel;

import java.util.ArrayList;
import java.util.HashMap;

public class SongLibrary {
    public static ArrayList<Song> songs, originals;
    public static String[] playlistNames;
    public static HashMap<String,ArrayList<Song>> playListSongs;
    public static int cursorCount = 0;
    public static String playListName = "";
    public static NotificationChannel channel;
    public static final int BACKGROUND_ID = 1000;
    public static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
}
