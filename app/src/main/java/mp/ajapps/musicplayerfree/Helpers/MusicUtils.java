package mp.ajapps.musicplayerfree.Helpers;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;

import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.POJOS.AlbumArtistDetails;
import mp.ajapps.musicplayerfree.POJOS.SearchPojo;
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
    public static <T> void execute(final boolean forceSerial, final AsyncTask<T, ?, ?> task,
                                   final T... args) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) {
            throw new UnsupportedOperationException(
                    "This class can only be used on API 4 and newer.");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || forceSerial) {
            task.execute(args);
        } else {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args);
        }
    }

    public static void initImageCacher(Context c) {
        ImageLoader imageLoader;
        DisplayImageOptions options;
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
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
                temp.setmId(c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID)));
               // temp.setAlbum_art(c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)));
                tracks.add(temp);
            } else if ("album".equals(type)) {
                temp.setTitle(c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                temp.setArtist(c.getString(c.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
                temp.setmType(4);
                temp.setmId(c.getString(c.getColumnIndex(MediaStore.Audio.Media._ID)));
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
        }
        Toast.makeText(context, numinserted + "Tracks Added", Toast.LENGTH_SHORT).show();
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
}
