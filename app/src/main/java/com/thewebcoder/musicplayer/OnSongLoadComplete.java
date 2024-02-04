package com.thewebcoder.musicplayer;

import java.util.ArrayList;

public interface OnSongLoadComplete {
    void onLoadCompleteSuccess(ArrayList<Song> songs);
    void onLoadCompleteFailure(String errorMessage);
    void onLoadStart(int songCount);
    void onLoading(int progress);
}
