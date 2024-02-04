package com.thewebcoder.musicplayer;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnPlaylistLoadComplete {
    void onLoadCompleteSuccess(ArrayList<String> playListNames, HashMap<String,ArrayList<Song>> playListSongs);
    void onLoadCompleteFailure(String errorMessage);
}
