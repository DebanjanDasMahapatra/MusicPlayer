package com.example.musicplayer;

import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private ArrayList<Song> orig;
    private OnSongClick onSongClick;
    
    SongAdapter(ArrayList<Song> song, OnSongClick onSongClick) {
        this.orig = song;
        this.onSongClick = onSongClick;
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
                SongLibrary.cursorCount = SongLibrary.originals.size()/2;
                notifyDataSetChanged();
            }
        };
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, artist, duration;
        ImageView songLogo;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitleL);
            artist = itemView.findViewById(R.id.songArtistL);
            duration = itemView.findViewById(R.id.songDurationL);
            songLogo = itemView.findViewById(R.id.songLogo);
        }
    }
}
