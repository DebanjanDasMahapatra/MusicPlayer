package com.example.musicplayer;

import android.app.Activity;
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
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private ArrayList<Song> orig;
    private SongPlayer songPlayer;
    private OnSongClick onSongClick;
    private Context context;
    private Snackbar snackbar;
    private AlertDialog.Builder builder;
    
    public SongAdapter(Context context, ArrayList<Song> song, OnSongClick onSongClick, Activity activity) {
        this.context = context;
        this.orig = song;
        this.onSongClick = onSongClick;
        songPlayer = ((SongPlayer) Objects.requireNonNull(activity));
        builder = new AlertDialog.Builder(context,R.style.MyDialogTheme);
        builder.setTitle("Select Playlist Name").setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
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
        viewHolder.title.setText(orig.get(i).getSongTitle());
        viewHolder.artist.setText(orig.get(i).getSongArtist());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(orig.get(i).getSongLocation());
        byte[] albumArt = retriever.getEmbeddedPicture();
        if(albumArt != null)
            viewHolder.songLogo.setImageBitmap(BitmapFactory.decodeByteArray(albumArt,0,albumArt.length));
        else
            viewHolder.songLogo.setImageResource(R.drawable.mp_logo);
        int duration = Integer.parseInt(orig.get(i).getSongDuration())/1000;
        int mins = duration/60;
        int secs = duration%60;
        final int pos = i;
        viewHolder.duration.setText(String.format(Locale.getDefault(),"%02d:%02d",mins,secs));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSongClick.songClick(orig.get(pos).getActualPosition());
            }
        });
        viewHolder.songMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context,viewHolder.songMenu);
                popupMenu.getMenu().add(0,0,0,"Add to Playlist");
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == 0) {
                            View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.playlist_choice_layout,null);
                            final RadioGroup rg = view.findViewById(R.id.playListChoices);
                            addViews(rg);
                            builder.setView(view).setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    int id = rg.getCheckedRadioButtonId();
                                    if(id == -1)
                                        Toast.makeText(context,"No Playlist Selected", Toast.LENGTH_LONG).show();
                                    else {
                                        String pln = SongLibrary.playlistNames[Integer.parseInt(String.valueOf(id).substring(4))];
                                        if(SongLibrary.playListName.equals(pln)) {
                                            snackbar = Snackbar.make(viewHolder.itemView, "This playlist is playing. Please switch to any other playlist or to all songs list and try again.", BaseTransientBottomBar.LENGTH_LONG);
                                            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
                                            TextView textView = layout.findViewById(com.google.android.material.R.id.snackbar_text);
                                            textView.setTypeface(context.getResources().getFont(R.font.mulu_regular), Typeface.BOLD);
                                            textView.setTextColor(Color.RED);
                                            snackbar.show();
                                        }
                                        else
                                            addSongToPlaylist(pln,orig.get(pos));
                                    }
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
                    if (song != null && song.size() > 0)
                        for (Song g : song) {
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
                orig = (ArrayList<Song>) results.values;
                SongLibrary.originals = orig;
                SongLibrary.cursorCount = SongLibrary.originals.size();
                notifyDataSetChanged();
            }
        };
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

    private void addViews(RadioGroup radioGroup) {
        RadioButton radioButton;
        for(int i = 0; i < SongLibrary.playlistNames.length; i++) {
            radioButton = new RadioButton(context);
            radioButton.setText(SongLibrary.playlistNames[i]);
            radioButton.setTextColor(Color.WHITE);
            radioButton.setPadding(15,0,10,5);
            radioButton.setTextSize(18f);
            radioButton.setId(Integer.parseInt(1000+""+i));
            radioGroup.addView(radioButton);
        }
    }

    private void addSongToPlaylist(String pln, Song temp) {
        DatabaseHelper.addSongToPlaylist(context, pln, temp.getSongLocation());
        Toast.makeText(context, "Song added to Playlist " + pln, Toast.LENGTH_LONG).show();
        ArrayList<Song> songs = SongLibrary.playListSongs.get(pln);
        if(songs != null) {
            temp.setActualPosition(songs.size());
            songs.add(temp);
            SongLibrary.playListSongs.put(pln,songs);
        }
        songPlayer.playlistAdapter.notifyDataSetChanged();
    }
}
