package com.example.musicplayer;

public class Song {
    private String songTitle, songArtist, songDuration, songLocation;

    public Song(String songTitle, String songArtist, String songDuration, String songLocation) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.songDuration = songDuration;
        this.songLocation = songLocation;
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
}
