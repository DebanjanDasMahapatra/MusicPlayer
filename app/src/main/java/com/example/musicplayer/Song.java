package com.example.musicplayer;

class Song {
    private String songTitle, songArtist, songDuration, songLocation;
    private int actualPosition;

    public Song(String songTitle, String songArtist, String songDuration, String songLocation, int actualPosition) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songDuration = songDuration;
        this.songLocation = songLocation;
        this.actualPosition = actualPosition;
    }

    String getSongTitle() {
        return songTitle;
    }

    String getSongArtist() {
        return songArtist;
    }

    String getSongDuration() {
        return songDuration;
    }

    String getSongLocation() {
        return songLocation;
    }

    int getActualPosition() {
        return actualPosition;
    }
}
