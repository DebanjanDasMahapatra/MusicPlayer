package com.example.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {

    private Context context;
    private SongPlayer songPlayer;
    private Snackbar snackbar;
    private AlertDialog.Builder builder, builder2;

    public PlaylistAdapter(Context context, Activity activity) {
        this.context = context;
        songPlayer = ((SongPlayer) Objects.requireNonNull(activity));
        builder = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        builder.setNeutralButton("CLOSE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setCancelable(false);
        builder2 = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        builder2.setTitle("Confirm").setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setCancelable(false);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_layout,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int i) {
        final String pln = SongLibrary.playlistNames[i];
        viewHolder.title.setText(pln);
        final ArrayList<Song> temp = SongLibrary.playListSongs.get(pln);
        viewHolder.count.setText(String.format(Locale.getDefault(),"%d Song(s)", temp == null ? 0 : temp.size()));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                View view1 = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.playlist_song_list_layout,null);
                RecyclerView recyclerView = view1.findViewById(R.id.playListSongList);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                PlaylistSongAdapter playlistSongAdapter = new PlaylistSongAdapter(new OnSongClick() {
                    @Override
                    public void songClick(int clickedIndex) {
                        ArrayList<Song> songs = SongLibrary.playListSongs.get(pln);
                        if (!SongLibrary.playListName.equals(pln)) {
                            SongLibrary.songs = songs;
                            SongLibrary.playListName = pln;
                        }
                        if (Singing.mediaPlayer != null && clickedIndex > -1 && songs != null && songs.size() > clickedIndex) {
                            boolean wasLooping = Singing.mediaPlayer.isLooping();
                            Singing.mediaPlayer.reset();
                            Singing.currentSongIndex = clickedIndex;
                            songPlayer.initializeSongInMediaPlayer(clickedIndex, false, songPlayer.isPlaying, wasLooping);
                        }
                    }
                }, context, pln, new OnPlaylistSongRemove() {
                    @Override
                    public void songRemoved() {
                        notifyDataSetChanged();
                    }
                });
                recyclerView.setAdapter(playlistSongAdapter);
                builder.setView(view1).setTitle(Html.fromHtml("All Songs in <b>" + pln + "</b>")).show();
            }
        });
        viewHolder.playListMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context,viewHolder.playListMenu);
                popupMenu.getMenu().add(0,0,0,"Delete Playlist");
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == 0) {
                            if(SongLibrary.playListName.equals(pln)) {
                                snackbar = Snackbar.make(viewHolder.itemView, "This playlist is playing. Please switch to any other playlist or to all songs list and try again.", BaseTransientBottomBar.LENGTH_LONG);
                                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
                                TextView textView = layout.findViewById(com.google.android.material.R.id.snackbar_text);
                                textView.setTypeface(context.getResources().getFont(R.font.mulu_regular), Typeface.BOLD);
                                textView.setTextColor(Color.RED);
                                snackbar.show();
                                return false;
                            }
                            builder2.setMessage(Html.fromHtml("<span style=\"color: white;\">Sure to delete the playlist "+pln+"?</span>"))
                                    .setPositiveButton("YES, DELETE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            deletePlaylist(pln);
                                            notifyDataSetChanged();
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return SongLibrary.playlistNames.length;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, count;
        ImageView playListMenu;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.playListTitle);
            count = itemView.findViewById(R.id.songCount);
            playListMenu = itemView.findViewById(R.id.playlistMenu);
        }
    }

    private void deletePlaylist(String pln) {
        DatabaseHelper.deletePlaylist(context, pln);
        Toast.makeText(context, "Playlist " + pln + " deleted.", Toast.LENGTH_LONG).show();
        SongLibrary.playlistNames = new String[SongLibrary.playlistNames.length - 1];
        DatabaseHelper.getAllPlaylists(context).toArray(SongLibrary.playlistNames);
        SongLibrary.playListSongs.remove(pln);
    }
}
