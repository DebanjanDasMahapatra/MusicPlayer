package com.thewebcoder.musicplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.MyViewHolder> {

    private final ArrayList<Song> songs;
    private final OnSongClick onSongClick;
    private final OnPlaylistSongRemove onPlaylistSongRemove;
    private final Context context;
    private final String playListName;
    private final AlertDialog.Builder builder;
    private Snackbar snackbar;

    public PlaylistSongAdapter(OnSongClick onSongClick, Context context, String playListName, OnPlaylistSongRemove onPlaylistSongRemove) {
        this.songs = SongLibrary.playListSongs.get(playListName);
        this.onSongClick = onSongClick;
        this.context = context;
        this.playListName = playListName;
        this.onPlaylistSongRemove = onPlaylistSongRemove;
        builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setMessage("Sure to remove this song from this playlist?").setTitle("Confirm").setNeutralButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).setCancelable(false);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder viewHolder, int i) {
        viewHolder.title.setText(songs.get(i).getSongTitle());
        viewHolder.artist.setText(songs.get(i).getSongArtist());
        if (songs.get(i).getImageBitmap() != null) viewHolder.songLogo.setImageBitmap(songs.get(i).getImageBitmap());
        else viewHolder.songLogo.setImageResource(R.drawable.mp_logo);
        int duration = Integer.parseInt(songs.get(i).getSongDuration()) / 1000;
        int minutes = duration / 60;
        int secs = duration % 60;
        final int pos = i;
        viewHolder.duration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, secs));
        viewHolder.itemView.setOnClickListener(view -> onSongClick.songClick(songs.get(pos).getActualPosition()));
        viewHolder.itemView.setOnLongClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, viewHolder.itemView);
            popupMenu.getMenu().add(0, 0, 0, "Remove from this Playlist");
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) {
                    if (SongLibrary.playListName.equals(playListName) && SongPlayingService.currentSongIndex == pos) {
                        snackbar = Snackbar.make(viewHolder.itemView, "This song is playing. Please change the song and try removing this song again.", BaseTransientBottomBar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        textView.setTypeface(context.getResources().getFont(R.font.mulu_regular), Typeface.BOLD);
                        textView.setTextColor(Color.RED);
                        snackbar.show();
                        return false;
                    }
                    builder.setPositiveButton("YES, REMOVE", (dialogInterface, i1) -> {
                        deleteSongFromPlaylist(pos);
                        notifyDataSetChanged();
                        dialogInterface.dismiss();
                    }).show();
                }
                return true;
            });
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    private void deleteSongFromPlaylist(int pos) {
        DatabaseHelper.deleteSongFromPlaylist(context, playListName, songs.get(pos).getSongLocation());
        Toast.makeText(context, "Song removed from the Playlist " + playListName, Toast.LENGTH_LONG).show();
        songs.remove(pos);
        for (int i = pos; i < songs.size(); i++)
            songs.get(i).setActualPosition(songs.get(i).getActualPosition() - 1);
        if (SongPlayingService.currentSongIndex > pos) SongPlayingService.currentSongIndex--;
        SongLibrary.playListSongs.put(playListName, songs);
        onPlaylistSongRemove.songRemoved();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, artist, duration;
        ImageView songLogo;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setLongClickable(true);
            title = itemView.findViewById(R.id.songTitleL);
            artist = itemView.findViewById(R.id.songArtistL);
            duration = itemView.findViewById(R.id.songDurationL);
            songLogo = itemView.findViewById(R.id.songLogo);
        }
    }
}
