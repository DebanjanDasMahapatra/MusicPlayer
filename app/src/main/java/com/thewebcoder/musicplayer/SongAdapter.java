package com.thewebcoder.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private final SongPlayer songPlayer;
    private final OnSongClick onSongClick;
    private final Context context;
    private final AlertDialog.Builder builder;
    private ArrayList<Song> original;
    private Snackbar snackbar;

    public SongAdapter(Context context, ArrayList<Song> song, OnSongClick onSongClick, Activity activity) {
        this.context = context;
        this.original = song;
        this.onSongClick = onSongClick;
        songPlayer = ((SongPlayer) Objects.requireNonNull(activity));
        builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        builder.setTitle("Select Playlist Name").setNeutralButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss()).setCancelable(false);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.MyViewHolder viewHolder, int i) {
        viewHolder.title.setText(original.get(i).getSongTitle());
        viewHolder.artist.setText(original.get(i).getSongArtist());
        if (original.get(i).getImageBitmap() != null)
            viewHolder.songLogo.setImageBitmap(original.get(i).getImageBitmap());
        else viewHolder.songLogo.setImageResource(R.drawable.mp_logo);
        int duration = Integer.parseInt(original.get(i).getSongDuration()) / 1000;
        int minutes = duration / 60;
        int secs = duration % 60;
        final int pos = i;
        viewHolder.duration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, secs));
        viewHolder.itemView.setOnClickListener(view -> onSongClick.songClick(original.get(pos).getActualPosition()));
        viewHolder.itemView.setOnLongClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, viewHolder.itemView);
            popupMenu.getMenu().add(0, 0, 0, "Add to Playlist");
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 0) {
                    View view1 = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.playlist_choice_layout, null);
                    final RadioGroup rg = view1.findViewById(R.id.playListChoices);
                    addViews(rg);
                    builder.setView(view1).setPositiveButton("ADD", (dialogInterface, i1) -> {
                        int id = rg.getCheckedRadioButtonId();
                        if (id == -1) Toast.makeText(context, "No Playlist Selected", Toast.LENGTH_LONG).show();
                        else {
                            String pln = SongLibrary.playlistNames[Integer.parseInt(String.valueOf(id).substring(4))];
                            if (SongLibrary.playListName.equals(pln)) {
                                snackbar = Snackbar.make(viewHolder.itemView, "This playlist is playing. Please switch to any other playlist or to all songs list and try again.", BaseTransientBottomBar.LENGTH_LONG);
                                View snackbarView = snackbar.getView();
                                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                                textView.setTypeface(context.getResources().getFont(R.font.mulu_regular), Typeface.BOLD);
                                textView.setTextColor(Color.RED);
                                snackbar.show();
                            } else addSongToPlaylist(pln, original.get(pos));
                        }
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
        return SongLibrary.cursorCount;
    }

    Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Song> results = new ArrayList<>();
                final ArrayList<Song> song = SongLibrary.songs;
                if (constraint != null) {
                    CharSequence search = constraint.toString().toLowerCase();
                    if (song != null && !song.isEmpty()) for (Song g : song) {
                        if (g.getSongTitle().toLowerCase().contains(search) || g.getSongArtist().toLowerCase().contains(search))
                            results.add(g);
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                original = (ArrayList<Song>) results.values;
                SongLibrary.originals = original;
                SongLibrary.cursorCount = SongLibrary.originals.size();
                notifyDataSetChanged();
            }
        };
    }

    private void addViews(RadioGroup radioGroup) {
        RadioButton radioButton;
        for (int i = 0; i < SongLibrary.playlistNames.length; i++) {
            radioButton = new RadioButton(context);
            radioButton.setText(SongLibrary.playlistNames[i]);
            radioButton.setTextColor(Color.WHITE);
            radioButton.setPadding(15, 0, 10, 5);
            radioButton.setTextSize(18f);
            radioButton.setId(Integer.parseInt(1000 + "" + i));
            radioGroup.addView(radioButton);
        }
    }

    private void addSongToPlaylist(String pln, Song temp) {
        DatabaseHelper.addSongToPlaylist(context, pln, temp.getSongLocation());
        Toast.makeText(context, "Song added to Playlist " + pln, Toast.LENGTH_LONG).show();
        ArrayList<Song> songs = SongLibrary.playListSongs.get(pln);
        if (songs != null) {
            temp.setActualPosition(songs.size());
            songs.add(temp);
            SongLibrary.playListSongs.put(pln, songs);
        }
        songPlayer.playlistAdapter.notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

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
