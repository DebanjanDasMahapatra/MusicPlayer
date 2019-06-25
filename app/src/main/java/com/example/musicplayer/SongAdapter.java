package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private ArrayList<Song> song;
    private ArrayList<Song> orig;
    private Context context;
    
    SongAdapter(ArrayList<Song> song, Context context) {
        this.song = song;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.song_layout,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        viewHolder.title.setText(song.get(i).getSongTitle());
        viewHolder.artist.setText(song.get(i).getSongArtist());
        int duration = Integer.parseInt(song.get(i).getSongDuration())/1000;
        int mins = duration/60;
        int secs = duration%60;
        final int pos = i;
        viewHolder.duration.setText(String.format(Locale.getDefault(),"%02d:%02d",mins,secs));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongLibrary.currentlyPlaying = pos;
                MainActivity.started = false;
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
                if (orig == null) {
                    orig = song;
                }
                if (constraint != null) {
                    if (orig != null && orig.size() > 0)
                        for (final Song g : orig)
                            if (g.getSongTitle().toLowerCase().contains(constraint.toString()))
                                results.add(g);
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                song = (ArrayList<Song>) results.values;
                SongLibrary.songs = (ArrayList<Song>) results.values;
                SongLibrary.cursorCount = SongLibrary.songs.size()/2;
                notifyDataSetChanged();
            }
        };
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, artist, duration;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitleL);
            artist = itemView.findViewById(R.id.songArtistL);
            duration = itemView.findViewById(R.id.songDurationL);
        }
    }
}
