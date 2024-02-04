package com.thewebcoder.musicplayer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class DatabaseHelper {

    public static void openOrCreateDatabases(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        database.execSQL(String.format(Locale.getDefault(), "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR NOT NULL UNIQUE);", context.getString(R.string.playlist_table), context.getString(R.string.playlist_name)));
        database.execSQL(String.format(Locale.getDefault(), "CREATE TABLE IF NOT EXISTS %s (%s VARCHAR NOT NULL, %s VARCHAR NOT NULL, UNIQUE(%s,%s));", context.getString(R.string.ps_table), context.getString(R.string.ps_name), context.getString(R.string.ps_location), context.getString(R.string.ps_name), context.getString(R.string.ps_location)));
        database.close();
    }

    public static ArrayList<String> getAllPlaylists(Context context) {
        ArrayList<String> names = new ArrayList<>();
        SQLiteDatabase database = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        Cursor c = database.rawQuery(String.format(Locale.getDefault(), "SELECT * FROM %s;", context.getString(R.string.playlist_table)), null);
        int col = c.getColumnIndex(context.getString(R.string.playlist_name));
        if (c.moveToFirst()) {
            do {
                names.add(c.getString(col));
            } while (c.moveToNext());
            c.close();
        }
        database.close();
        return names;
    }

    public static HashMap<String, ArrayList<Song>> getAllPlaylistSongs(Context context) {
        ArrayList<String> locations;
        HashMap<String, ArrayList<Song>> result = new HashMap<>();
        HashMap<String, ArrayList<String>> temp = new HashMap<>();
        SQLiteDatabase database = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        Cursor c = database.rawQuery(String.format(Locale.getDefault(), "SELECT * FROM %s;", context.getString(R.string.ps_table)), null);
        int col1 = c.getColumnIndex(context.getString(R.string.ps_name)), col2 = c.getColumnIndex(context.getString(R.string.ps_location));
        if (c.moveToFirst()) {
            do {
                String name = c.getString(col1), location = c.getString(col2);
                if (temp.containsKey(name)) {
                    locations = temp.get(name);
                    if (locations == null) locations = new ArrayList<>();
                } else locations = new ArrayList<>();
                locations.add(location);
                temp.put(name, locations);
            } while (c.moveToNext());
            c.close();
        }
        database.close();
        for (String key : temp.keySet())
            result.put(key, getSongList(temp.get(key)));
        return result;
    }

    public static void createPlaylist(Context context, String name) {
        SQLiteDatabase myDB = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        myDB.execSQL(String.format(Locale.getDefault(), "INSERT INTO %s (%s) VALUES ('%s');", context.getString(R.string.playlist_table), context.getString(R.string.playlist_name), name));
        myDB.close();
    }

    public static void deletePlaylist(Context context, String name) {
        SQLiteDatabase myDB = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        myDB.execSQL(String.format(Locale.getDefault(), "DELETE FROM %s WHERE %s='%s';", context.getString(R.string.playlist_table), context.getString(R.string.playlist_name), name));
        myDB.execSQL(String.format(Locale.getDefault(), "DELETE FROM %s WHERE %s='%s';", context.getString(R.string.ps_table), context.getString(R.string.ps_name), name));
        myDB.close();
    }

    public static void addSongToPlaylist(Context context, String name, String songLocation) {
        SQLiteDatabase myDB = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        myDB.execSQL(String.format(Locale.getDefault(), "INSERT INTO %s (%s,%s) VALUES ('%s','%s');", context.getString(R.string.ps_table), context.getString(R.string.ps_name), context.getString(R.string.ps_location), name, songLocation));
        myDB.close();
    }

    public static void deleteSongFromPlaylist(Context context, String name, String songLocation) {
        SQLiteDatabase myDB = context.openOrCreateDatabase(context.getString(R.string.db_name), MODE_PRIVATE, null);
        myDB.execSQL(String.format(Locale.getDefault(), "DELETE FROM %s WHERE %s='%s' AND %s='%s';", context.getString(R.string.ps_table), context.getString(R.string.ps_name), name, context.getString(R.string.ps_location), songLocation));
        myDB.close();
    }

    private static ArrayList<Song> getSongList(ArrayList<String> songLocations) {
        ArrayList<Song> songs = new ArrayList<>();
        int i = 0;
        for (Song s : SongLibrary.songs)
            if (songLocations.contains(s.getSongLocation())) {
                s.setActualPosition(i++);
                songs.add(s);
            }
        return songs;
    }

}
