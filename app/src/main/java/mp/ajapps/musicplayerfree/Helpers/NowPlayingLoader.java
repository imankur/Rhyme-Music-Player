package mp.ajapps.musicplayerfree.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.POJOS.Song;

/**
 * Created by Sharing Happiness on 12/25/2015.
 */
public class NowPlayingLoader extends AsyncTaskLoader<List<Song>> {
    private long [] mNowPlaying;
    private ArrayList<Song> mSongList;
    private final ArrayList<Long> mSongIndexList = new ArrayList<>();
    public NowPlayingLoader (Context context) {
        super(context);
    }
    private final String mSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
    private static final String[] PROJECTION = new String[] {
            BaseColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ALBUM,
    };

    @Override
    public List<Song> loadInBackground() {
        return getDataFromCursor(makeNowPlayingCursor());
    }
    private Cursor makeNowPlayingCursor() {
        mNowPlaying = null;
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        try {
            mNowPlaying = MusicUtils.mService.getQueue();
            if (mNowPlaying.length == 0) {
                return null;
            }
            mSongList = new ArrayList<>(mNowPlaying.length);
            for (long c : mNowPlaying) {
                mSongIndexList.add(c);
                mSongList.add(null);
                selection.append(c);
                selection.append(",");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        
        selection.setCharAt(selection.length() - 1,')');
        Cursor mQueueCursor = getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, selection.toString(),
                null, mSortOrder);

        if (mQueueCursor == null) {
            return null;
        }
        return mQueueCursor;
    }

    private List<Song> getDataFromCursor (Cursor mCursor) {
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String songName = mCursor.getString(1);
                final String album = mCursor.getString(2);
                final Song song = new Song(id, songName,album,0);
                int o = mSongIndexList.indexOf(id);
                mSongList.set(o, song);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }
}
