package com.example.musicplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.MyViewHolder> {

    private ArrayList<Song> songs;
    private OnSongClick onSongClick;
    private OnPlaylistSongRemove onPlaylistSongRemove;
    private Context context;
    private String playListName;
    private Snackbar snackbar;
    private AlertDialog.Builder builder;

    public PlaylistSongAdapter(OnSongClick onSongClick, Context context, String playListName, OnPlaylistSongRemove onPlaylistSongRemove) {
        this.songs = SongLibrary.playListSongs.get(playListName);
        this.onSongClick = onSongClick;
        this.context = context;
        this.playListName = playListName;
        this.onPlaylistSongRemove = onPlaylistSongRemove;
        builder = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        builder.setMessage("Sure to remove this song from this playlist?").setTitle("Confirm")
                .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setCancelable(false);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_layout,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int i) {
        viewHolder.title.setText(songs.get(i).getSongTitle());
        viewHolder.artist.setText(songs.get(i).getSongArtist());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(songs.get(i).getSongLocation());
        byte[] albumArt = retriever.getEmbeddedPicture();
        if(albumArt != null)
            viewHolder.songLogo.setImageBitmap(BitmapFactory.decodeByteArray(albumArt,0,albumArt.length));
        else
            viewHolder.songLogo.setImageResource(R.drawable.mp_logo);
        int duration = Integer.parseInt(songs.get(i).getSongDuration())/1000;
        int mins = duration/60;
        int secs = duration%60;
        final int pos = i;
        viewHolder.duration.setText(String.format(Locale.getDefault(),"%02d:%02d",mins,secs));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSongClick.songClick(songs.get(pos).getActualPosition());
            }
        });
        viewHolder.songMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(context,viewHolder.songMenu);
                popupMenu.getMenu().add(0,0,0,"Remove from this Playlist");
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == 0) {
                            if(SongLibrary.playListName.equals(playListName) && Singing.currentSongIndex == pos) {
                                snackbar = Snackbar.make(viewHolder.itemView, "This song is playing. Please change the song and try removing this song again.", BaseTransientBottomBar.LENGTH_LONG);
                                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
                                TextView textView = layout.findViewById(com.google.android.material.R.id.snackbar_text);
                                textView.setTypeface(context.getResources().getFont(R.font.mulu_regular), Typeface.BOLD);
                                textView.setTextColor(Color.RED);
                                snackbar.show();
                                return false;
                            }
                            builder.setPositiveButton("YES, REMOVE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteSongFromPlaylist(pos);
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
        return songs.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, artist, duration;
        ImageView songLogo, songMenu;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitleL);
            artist = itemView.findViewById(R.id.songArtistL);
            duration = itemView.findViewById(R.id.songDurationL);
            songLogo = itemView.findViewById(R.id.songLogo);
            songMenu = itemView.findViewById(R.id.songMenu);
        }
    }

    private void deleteSongFromPlaylist(int pos) {
        DatabaseHelper.deleteSongFromPlaylist(context, playListName, songs.get(pos).getSongLocation());
        Toast.makeText(context, "Song removed from the Playlist " + playListName, Toast.LENGTH_LONG).show();
        songs.remove(pos);
        for(int i = pos; i < songs.size(); i++)
            songs.get(i).setActualPosition(songs.get(i).getActualPosition()-1);
        if(Singing.currentSongIndex > pos)
            Singing.currentSongIndex--;
        SongLibrary.playListSongs.put(playListName,songs);
        onPlaylistSongRemove.songRemoved();
    }
}
