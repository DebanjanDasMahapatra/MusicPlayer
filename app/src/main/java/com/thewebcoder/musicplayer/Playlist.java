package com.thewebcoder.musicplayer;

import java.util.ArrayList;

public class Playlist {
    String id, name;
    ArrayList<Song> songs;

    public Playlist(String id, String name) {
        this.id = id;
        this.name = name;
        songs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSongCount() {
        return songs.size();
    }
}
