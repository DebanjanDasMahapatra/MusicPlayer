package com.example.musicplayer;

import android.content.Context;
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

    private ArrayList<Song> song;
    private ArrayList<Song> orig;
    private Context context;
    private OnSongClick onSongClick;
    
    SongAdapter(ArrayList<Song> song, Context context, OnSongClick onSongClick) {
        this.song = song;
        this.context = context;
        this.onSongClick = onSongClick;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.song_layout,viewGroup,false);
        view.setBackgroundColor(context.getResources().getColor(SongLibrary.darkTheme ? R.color.black : R.color.white));
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        viewHolder.title.setText(song.get(i).getSongTitle());
        viewHolder.artist.setText(song.get(i).getSongArtist());
        viewHolder.title.setTextColor(context.getResources().getColor(SongLibrary.darkTheme ? R.color.white : R.color.black));
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(song.get(i).getSongLocation());
        byte[] albumArt = retriever.getEmbeddedPicture();
        if(albumArt != null)
            viewHolder.songLogo.setImageBitmap(BitmapFactory.decodeByteArray(albumArt,0,albumArt.length));
        else
            viewHolder.songLogo.setImageResource(R.drawable.mp_logo);
        int duration = Integer.parseInt(song.get(i).getSongDuration())/1000;
        int mins = duration/60;
        int secs = duration%60;
        final int pos = i;
        viewHolder.duration.setText(String.format(Locale.getDefault(),"%02d:%02d",mins,secs));
//        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SongLibrary.currentlyPlaying = song.get(pos).getActualPosition();
//                MainActivity.started = false;
//            }
//        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSongClick.songClick(pos);
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
