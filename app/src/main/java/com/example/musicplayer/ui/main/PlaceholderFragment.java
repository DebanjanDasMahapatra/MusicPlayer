package com.example.musicplayer.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.musicplayer.DatabaseHelper;
import com.example.musicplayer.OnPlaylistLoadComplete;
import com.example.musicplayer.OnSongClick;
import com.example.musicplayer.PlaylistAdapter;
import com.example.musicplayer.R;
import com.example.musicplayer.Singing;
import com.example.musicplayer.Song;
import com.example.musicplayer.SongAdapter;
import com.example.musicplayer.SongLibrary;
import com.example.musicplayer.SongPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        songPlayer = ((SongPlayer) Objects.requireNonNull(getActivity()));
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
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        songPlayer.songAdapter = new SongAdapter(context, SongLibrary.originals, new OnSongClick() {
            @Override
            public void songClick(int clickedIndex) {
                if(!SongLibrary.playListName.equals("")) {
                    SongLibrary.songs = SongLibrary.originals;
                    SongLibrary.playListName = "";
                }
                if (Singing.mediaPlayer != null && clickedIndex > -1 && SongLibrary.songs.size() > clickedIndex) {
                    boolean wasLooping = Singing.mediaPlayer.isLooping();
                    Singing.mediaPlayer.reset();
                    Singing.currentSongIndex = clickedIndex;
                    songPlayer.initializeSongInMediaPlayer(Singing.currentSongIndex, false, songPlayer.isPlaying, wasLooping);
                }
            }
        }, getActivity());
        recyclerView.setAdapter(songPlayer.songAdapter);
    }

    public void initializePlaylistsView(final View root) {
        playListView = root.findViewById(R.id.playList);
        playListView.setLayoutManager(new LinearLayoutManager(context));
        initializePlayLists(true);
        newPlayList = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        newPlayList.setTitle("New Playlist").setCancelable(false)
                .setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pln = ((EditText) tempView.findViewById(R.id.playListName)).getText().toString();
                        DatabaseHelper.createPlaylist(context,pln);
                        Toast.makeText(context,String.format(Locale.getDefault(),"Playlist %s created.",pln),Toast.LENGTH_SHORT).show();
                        initializePlayLists(false);
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        Button btn = root.findViewById(R.id.newPlayList);
        if(btn != null)
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tempView = getLayoutInflater().inflate(R.layout.plalist_name_input_layout,null);
                    newPlayList.setView(tempView).show();
                }
            });
    }

    private void initializePlayLists(final boolean isInitial) {
        loadPlayLists(new OnPlaylistLoadComplete() {
            @Override
            public void onLoadCompleteSuccess(ArrayList<String> playListNames, HashMap<String,ArrayList<Song>> playListSongs) {
                SongLibrary.playlistNames = new String[playListNames.size()];
                playListNames.toArray(SongLibrary.playlistNames);
                SongLibrary.playListSongs = playListSongs;
                if(isInitial) {
                    songPlayer.playlistAdapter = new PlaylistAdapter(context, getActivity());
                    playListView.setAdapter(songPlayer.playlistAdapter);
                } else
                    songPlayer.playlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLoadCompleteFailure(String errorMessage) {
                Toast.makeText(context,String.format(Locale.getDefault(),"Error Occurred while fetching Playlists: %s",errorMessage),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadPlayLists(OnPlaylistLoadComplete onPlaylistLoadComplete) {
        try {
            onPlaylistLoadComplete.onLoadCompleteSuccess(DatabaseHelper.getAllPlaylists(context),DatabaseHelper.getAllPlaylistSongs(context));
        } catch (Exception e) {
            onPlaylistLoadComplete.onLoadCompleteFailure(e.toString());
        }
    }
}