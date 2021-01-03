package com.example.musicplayer;

import java.util.ArrayList;

public interface OnSongLoadComplete {
    void onLoadCompleteSuccess(ArrayList<Song> songs);
    void onLoadCompleteFailure(String errorMessage);
}
