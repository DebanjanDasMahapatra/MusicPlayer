package com.thewebcoder.musicplayer;

import android.graphics.Bitmap;

public class Song {
    private final String songTitle, songArtist, songDuration, songLocation;
    private int actualPosition;
    private final Bitmap imageBitmap;

    public Song(String songTitle, String songArtist, String songDuration, String songLocation, int actualPosition, Bitmap imageBitmap) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songDuration = songDuration;
        this.songLocation = songLocation;
        this.actualPosition = actualPosition;
        this.imageBitmap = imageBitmap;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongDuration() {
        return songDuration;
    }

    public String getSongLocation() {
        return songLocation;
    }

    public int getActualPosition() {
        return actualPosition;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setActualPosition(int actualPosition) {
        this.actualPosition = actualPosition;
    }
}
