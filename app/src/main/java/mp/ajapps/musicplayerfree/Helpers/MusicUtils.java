package mp.ajapps.musicplayerfree.Helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;
import java.util.zip.CRC32;

import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.POJOS.AlbumArtistDetails;
import mp.ajapps.musicplayerfree.POJOS.SearchPojo;
import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;

public class MusicUtils {

    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";
    private final static long[] sEmptyList = new long[0];
    private final static StringBuilder sFormatBuilder = new StringBuilder();
    private final static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];
    public static IMusicParent mService;
    private static ContentValues[] mContentValuesCache = null;

    public static void setAndPLay(long[] list, int pos) throws RemoteException {
        mService.setAndPlay(list, pos);
    }

    public static void setAndPlay(Cursor c, int pos) {
        try {
            setAndPLay(getSongListForCursor(c), pos);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<SearchPojo> getSongsListOfArtist(Context context, String artistName) {
        ArrayList<SearchPojo> songList = new ArrayList<>();
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.ARTIST + "='" + artistName.replace("'", "''") + "'";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do {
                songList.add(new SearchPojo(musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getLong(idColumn), null, 3));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
        return songList;
    }

    public static ArrayList<SearchPojo> searchSong(Context context, String sQuery) {
        ArrayList<SearchPojo> songList = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.TITLE + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst() && musicCursor.getCount() > 0 ) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            do {
                songList.add(new SearchPojo(musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getLong(idColumn), null, 3));
            }
            while (musicCursor.moveToNext());
        }
        return songList;
    }

    public static ArrayList<SearchPojo> searchAlbum(Context context, String sQuery) {
        ArrayList<SearchPojo> albumList = new ArrayList<>();
        final String where = MediaStore.Audio.Albums.ALBUM + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst() && musicCursor.getCount() > 0) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ARTIST);
            int albumArtColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new SearchPojo(musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getLong(idColumn), musicCursor.getString(albumArtColumn), 4));
            }
            while (musicCursor.moveToNext());
        }
        return albumList;
    }

    public static ArrayList<SearchPojo> getAlbumListOfArtist(Context context, long artistId) {
        final ArrayList<SearchPojo> albumList = new ArrayList<>();
        System.gc();
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                        null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
            int albumArtColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new SearchPojo(musicCursor.getString(titleColumn), musicCursor.getString(artistColumn), musicCursor.getLong(idColumn), musicCursor.getString(albumArtColumn), 4));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
        return albumList;
    }

    public static long getDuration() {
        try {
            return mService.duration();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getPosition() {
        try {
            return mService.getPosition();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long[] getSongListForCursor(Cursor cursor) {
        ExeTimeCalculator exeTimeCalculator = new ExeTimeCalculator();
        exeTimeCalculator.addTimeFrame("A");
        if (cursor == null) {
            return sEmptyList;
        }
        int len = cursor.getCount();
        long[] list = new long[len];
        cursor.moveToFirst();
        int colidx = -1;
        try {
            colidx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        } catch (IllegalArgumentException ex) {
            colidx = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(colidx);
            cursor.moveToNext();
        }
        exeTimeCalculator.addTimeFrame("B");
        exeTimeCalculator.printDifference();
        return list;
    }

    public static long getCurrentAlbumId() {
        if (mService != null) {
            try {
                return mService.getAlbumId();
            } catch (RemoteException ex) {}
        }
        return -1;
    }

    public static final long getQueueItemAtPosition(int position) {
        try {
            if (mService != null) {
                return mService.getQueueItemAtPosition(position);
            }
        } catch (final RemoteException ignored) {}
        return -1;
    }

    public static final int getQueueSize() {
        try {
            if (mService != null) {
                return mService.getQueueSize();
            }
        } catch (final RemoteException ignored) {}
        return 0;
    }

    public static String getCurrentAlbumArt() {
        try {
            if (mService != null) {
                return mService.getAlbumArt();
            }
        } catch (final RemoteException ignored) {}
        return null;
    }

    public static final int getQueuePosition() {
        try {
            if (mService != null) {
                return mService.getQueuePosition();
            }
        } catch (final RemoteException ignored) {}
        return 0;
    }

    public static void seekSong(long time) {
        try {
            mService.seekSong(time);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final AlbumArtistDetails getAlbumArtDetails(final Context context, final long trackId) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + BaseColumns._ID + " = '" + trackId + "'");

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        MediaStore.Audio.AudioColumns.ALBUM,
                        MediaStore.Audio.AlbumColumns.ARTIST,
                }, selection.toString(), null, null
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        AlbumArtistDetails result = new AlbumArtistDetails();
        result.mAudioId = trackId;
        result.mAlbumId = cursor.getLong(0);
        result.mAlbumName = cursor.getString(1);
        result.mArtistName = cursor.getString(2);
        cursor.close();

        return result;
    }

    @SuppressLint("NewApi")
    public static <T> void execute(final boolean forceSerial, final AsyncTask<T, ?, ?> task) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) {
            throw new UnsupportedOperationException(
                    "This class can only be used on API 4 and newer.");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || forceSerial) {
            task.execute();
        } else {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static void initImageCacher(Context c) {
        ImageLoader imageLoader;
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageForEmptyUri(R.drawable.default_artwork)
                .showImageOnFail(R.drawable.default_artwork)
                .cacheOnDisk(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(c)
                .defaultDisplayImageOptions(options)
                .build();
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited()) {
            imageLoader.init(config);
        }
    }

    public static String makeTimeString(Context context, long secs) {

        String durationformat = context.getString(secs < 3600 ? R.string.durationformatshort
                : R.string.durationformatlong);

        sFormatBuilder.setLength(0);
        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = secs / 60 % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;
        return sFormatter.format(durationformat, timeArgs).toString();
    }

    public static final CursorSorter makeSortedCursor(final Context context, final Cursor cursor,
                                                      final int idColumn) {
        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder selection = new StringBuilder();
            selection.append(BaseColumns._ID);
            selection.append(" IN (");

            long[] order = new long[cursor.getCount()];
            long id = cursor.getLong(idColumn);
            selection.append(id);
            order[cursor.getPosition()] = id;
            while (cursor.moveToNext()) {
                selection.append(",");
                id = cursor.getLong(idColumn);
                order[cursor.getPosition()] = id;
                selection.append(String.valueOf(id));
            }

            selection.append(")");
            Cursor songCursor = makeSongCursor(context, selection.toString(), false);
            return new CursorSorter(songCursor, order, BaseColumns._ID);
        }
        return null;
    }

    public static final Cursor makeSongCursor(final Context context, final String selection,
                                              final boolean runSort) {
        String selectionStatement = MusicUtils.MUSIC_ONLY_SELECTION;
        if (!TextUtils.isEmpty(selection)) {
            selectionStatement += " AND " + selection;
        }

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.YEAR,
                }, selectionStatement, null, null);

        return cursor;
    }

    public static final ArrayList<SearchPojo> getFormatSearchData(Cursor c) {
        ArrayList<SearchPojo> tracks = new ArrayList<SearchPojo>();
        ArrayList<SearchPojo> album = new ArrayList<SearchPojo>();

        tracks.add(new SearchPojo(1));
        album.add(new SearchPojo(2));
        c.moveToFirst();
        while (c.moveToNext()) {
            Log.i("yo", "getFormatSearchData: " + c.getColumnNames());
            SearchPojo temp = new SearchPojo();
            String type = c.getString(c.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE));
            if ("audio/mpeg".equals(type)) {
                temp.setTitle(c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                temp.setArtist(c.getString(c.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
                temp.setmType(3);
               // temp.setmId(c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID)));
               // temp.setAlbum_art(c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)));
                tracks.add(temp);
            } else if ("album".equals(type)) {
                temp.setTitle(c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                temp.setArtist(c.getString(c.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
                temp.setmType(4);
              //  temp.setmId(c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID)));
              //  temp.setAlbum_art(c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)));
                album.add(temp);
            }
        }
        tracks.addAll(album);
        return tracks;
    }

    public static final long[] shuffelLongArray(long[] arr) {
        Random random = null;

        if (random == null) random = new Random();
        int count = arr.length;
        for (int i = count; i > 1; i--) {
            swap(arr, i - 1, random.nextInt(i));
        }
        return arr;
    }

    public static String getAlbumArt(Context context, long albumdId) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumdId)},
                null);
        String imagePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        return imagePath;
    }

    private static void swap(long[] array, int i, int j) {
        long temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MusicUtils.MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }
        return 0;
    }

    public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {

        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + MediaStore.Audio.Playlists.Members.PLAY_ORDER + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
            Log.i("+-------", "addToPlaylist: " + numinserted);
        }
        Toast.makeText(context, numinserted + " Tracks Added", Toast.LENGTH_SHORT).show();
        context.sendBroadcast(new Intent().setAction("Playlist_Changed"));
    }

    public static void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }
    public static Cursor makeLastAddedCursor(final Context context) {
        long fourWeeksAgo = (System.currentTimeMillis() / 1000) - (4 * 3600 * 24 * 7);
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"); //$NON-NLS-2$
        selection.append(" AND " + MediaStore.Audio.Media.DATE_ADDED + ">"); //$NON-NLS-2$
        selection.append(fourWeeksAgo);

        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        MediaStore.Audio.AudioColumns.TITLE,
                        /* 2 */
                        MediaStore.Audio.AudioColumns.ARTIST,
                        /* 3 */
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        /* 4 */
                        MediaStore.Audio.AudioColumns.ALBUM,
                        /* 5 */
                        MediaStore.Audio.AudioColumns.DURATION,
                        /* 6 */
                        MediaStore.Audio.AudioColumns.YEAR,
                }, selection.toString(), null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
    }

    public static void setRingtone(final Context context, final long id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (final UnsupportedOperationException ingored) {
            return;
        }

        final String[] projection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.TITLE
        };

        final String selection = BaseColumns._ID + "=" + id;
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                final String message = "RingTone set : " +
                        cursor.getString(2);
                Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public static void deleteTracks(final Context context, final long list) {
        final String[] projection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " = ");
            selection.append(list);
        //selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

        final String message = "Track Deleted";

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        // We deleted a number of tracks, which could affect any number of
        // things
        // in the media content domain, so update everything.
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);

    }

    public static void PlayPlaylist(long mPlaylistId, Context v) {
        Cursor mCursor = getPlaylistTracks(mPlaylistId, v);
        try {
            setAndPLay(getSongListForCursor(mCursor), 0);
            mCursor.close();
            mCursor = null;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static Cursor getPlaylistTracks (long playlistId, Context v) {
        final StringBuilder mSelection = new StringBuilder();
        mSelection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        mSelection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''"); //$NON-NLS-2$
        return v.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[] {
                        /* 0 */
                        MediaStore.Audio.Playlists.Members._ID,
                        /* 1 */
                        MediaStore.Audio.Playlists.Members.AUDIO_ID,
                        /* 2 */
                        MediaStore.Audio.AudioColumns.TITLE,
                        /* 3 */
                        MediaStore.Audio.AudioColumns.ARTIST,
                        /* 4 */
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        /* 5 */
                        MediaStore.Audio.AudioColumns.ALBUM,
                        /* 6 */
                        MediaStore.Audio.AudioColumns.DURATION,
                        /* 7 */
                        MediaStore.Audio.AudioColumns.YEAR,
                        /* 8 */
                        MediaStore.Audio.Playlists.Members.PLAY_ORDER,
                }, mSelection.toString(), null,
                MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }


    public static void deletePlaylist(long mID, Context c) {
        ContentResolver resolver = c.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=" + mID;
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, null);
        Toast toast = Toast.makeText(c,  " Deleted", Toast.LENGTH_SHORT);
        toast.show();
        c.getContentResolver().notifyChange(Uri.parse("content://media"), null);
    }

}
