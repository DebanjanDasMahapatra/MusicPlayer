package com.thewebcoder.musicplayer.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thewebcoder.musicplayer.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    Context context;
    int id;
    RecyclerView recyclerView, playListView;
    AlertDialog.Builder newPlayList;
    View tempView;
    SongPlayer songPlayer;

    public PlaceholderFragment(int index) {
        id = index;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        songPlayer = ((SongPlayer) requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = null;
        switch (id) {
            case R.string.tab_text_2:
                root = inflater.inflate(R.layout.fragment_playlists, container, false);
                initializePlaylistsView(root);
                break;
            case R.string.tab_text_1:
                root = inflater.inflate(R.layout.fragment_all_songs, container, false);
                initializeAllSongsView(root);
                break;
        }
        return root;
    }

    public void initializeAllSongsView(View root) {
        recyclerView = root.findViewById(R.id.songList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        songPlayer.songAdapter = new SongAdapter(context, SongLibrary.originals, clickedIndex -> {
            if (!SongLibrary.playListName.equals("")) {
                SongLibrary.songs = SongLibrary.originals;
                SongLibrary.playListName = "";
            }
            if (SongPlayingService.mediaPlayer != null && clickedIndex > -1 && SongLibrary.songs.size() > clickedIndex) {
                boolean wasLooping = SongPlayingService.mediaPlayer.isLooping();
                SongPlayingService.mediaPlayer.reset();
                SongPlayingService.currentSongIndex = clickedIndex;
                songPlayer.initializeSongInMediaPlayer(SongPlayingService.currentSongIndex, false, songPlayer.isPlaying, wasLooping);
            }
        }, getActivity());
        recyclerView.setAdapter(songPlayer.songAdapter);
    }

    public void initializePlaylistsView(final View root) {
        playListView = root.findViewById(R.id.playList);
        playListView.setLayoutManager(new LinearLayoutManager(context));
        initializePlayLists(true);
        newPlayList = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        newPlayList.setTitle("New Playlist").setCancelable(false).setPositiveButton("CREATE", (dialogInterface, i) -> {
            String pln = ((EditText) tempView.findViewById(R.id.playListName)).getText().toString();
            DatabaseHelper.createPlaylist(context, pln);
            Toast.makeText(context, String.format(Locale.getDefault(), "Playlist %s created.", pln), Toast.LENGTH_SHORT).show();
            initializePlayLists(false);
        }).setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss());
        Button btn = root.findViewById(R.id.newPlayList);
        if (btn != null) btn.setOnClickListener(view -> {
            tempView = getLayoutInflater().inflate(R.layout.plalist_name_input_layout, null);
            newPlayList.setView(tempView).show();
        });
    }

    private void initializePlayLists(final boolean isInitial) {
        loadPlayLists(new OnPlaylistLoadComplete() {
            @Override
            public void onLoadCompleteSuccess(ArrayList<String> playListNames, HashMap<String, ArrayList<Song>> playListSongs) {
                SongLibrary.playlistNames = new String[playListNames.size()];
                playListNames.toArray(SongLibrary.playlistNames);
                SongLibrary.playListSongs = playListSongs;
                if (isInitial) {
                    songPlayer.playlistAdapter = new PlaylistAdapter(context, getActivity());
                    playListView.setAdapter(songPlayer.playlistAdapter);
                } else songPlayer.playlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoadCompleteFailure(String errorMessage) {
                Toast.makeText(context, String.format(Locale.getDefault(), "Error Occurred while fetching Playlists: %s", errorMessage), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPlayLists(OnPlaylistLoadComplete onPlaylistLoadComplete) {
        try {
            onPlaylistLoadComplete.onLoadCompleteSuccess(DatabaseHelper.getAllPlaylists(context), DatabaseHelper.getAllPlaylistSongs(context));
        } catch (Exception e) {
            onPlaylistLoadComplete.onLoadCompleteFailure(e.toString());
        }
    }
}