package com.thewebcoder.musicplayer;

import android.app.NotificationChannel;

import java.util.ArrayList;
import java.util.HashMap;

public class SongLibrary {
    public static final String ACTION_MUSIC_PLAY_OR_PAUSE = "com.thewebcoder.musicplayer.PLAY_OR_PAUSE", ACTION_MUSIC_REPEAT = "com.thewebcoder.musicplayer.REPEAT";
    public static final int BACKGROUND_ID = 1000;
    public static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    public static ArrayList<Song> songs, originals;
    public static String[] playlistNames;
    public static HashMap<String, ArrayList<Song>> playListSongs;
    public static int cursorCount = 0;
    public static String playListName = "";
    public static NotificationChannel channel;
}
