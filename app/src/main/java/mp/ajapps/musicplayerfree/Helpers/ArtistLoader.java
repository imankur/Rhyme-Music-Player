package mp.ajapps.musicplayerfree.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.POJOS.Artist;
import mp.ajapps.musicplayerfree.POJOS.Song;

/**
 * Created by Sharing Happiness on 1/17/2016.
 */
public class ArtistLoader extends AsyncTaskLoader<List<Artist>> {

    private ArrayList<Artist> mArtistsList;
    private Cursor mCursor;
    private Context c;

    public ArtistLoader(Context context) {
        super(context);
        this.c = context;
        mArtistsList = new ArrayList<Artist>();
    }

    @Override
    public List<Artist> loadInBackground() {
        mCursor = makeArtistCursor(c);

        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final long id = mCursor.getLong(0);
                final String artistName = mCursor.getString(1);
                final int albumCount = mCursor.getInt(2);
                final int songCount = mCursor.getInt(3);
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    continue;
                }
                final Artist artist = new Artist(id, artistName, songCount, albumCount);
                mArtistsList.add(artist);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        Log.i("awwwww", "loadInBackground: " + mArtistsList.size());
        return mArtistsList;
    }

    public static final Cursor makeArtistCursor(final Context context) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[] {
                        MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.Artists.ARTIST,
                        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                }, null, null, null);
        return cursor;
    }
}
