package mp.ajapps.musicplayerfree.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.POJOS.Playlist;

public class PlaylistLoader extends AsyncTaskLoader<List<Playlist>> {

    private final ArrayList<Playlist> mPlaylistList = new ArrayList<Playlist>();
    private Cursor mCursor;

    public PlaylistLoader(final Context context) {
        super(context);
    }

    public static final Cursor makePlaylistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID,
                        PlaylistsColumns.NAME
                }, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }

    @Override
    public List<Playlist> loadInBackground() {
        mCursor = makePlaylistCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String name = mCursor.getString(1);
                final int songCount = MusicUtils.getSongCountForPlaylist(getContext(), id);
                final Playlist playlist = new Playlist(id, name, songCount);
                mPlaylistList.add(playlist);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mPlaylistList;
    }
}
