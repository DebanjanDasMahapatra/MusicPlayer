package com.example.musicplayer;

public class Song {
    private String songTitle, songArtist, songDuration, songLocation;
    private int actualPosition;

    public Song(String songTitle, String songArtist, String songDuration, String songLocation, int actualPosition) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songDuration = songDuration;
        this.songLocation = songLocation;
        this.actualPosition = actualPosition;
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

    public void setActualPosition(int actualPosition) {
        this.actualPosition = actualPosition;
    }
}
