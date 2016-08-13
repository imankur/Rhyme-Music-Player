package mp.ajapps.musicplayerfree.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sharing Happiness on 8/9/2015.
 */
public class MusicPlaybackState extends SQLiteOpenHelper {
    public static final String DATABASENAME = "musicdb.db";
    Context context;

    public MusicPlaybackState(Context context) {
        super(context, DATABASENAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackQueueColumns.NAME);
        builder.append(" (");
        builder.append(PlaybackQueueColumns.TRACK_ID);
        builder.append(" INT NOT NULL);");
      /*  builder.append(PlaybackQueueColumns.SOURCE_ID);
        builder.append(" LONG NOT NULL,");
        builder.append(PlaybackQueueColumns.SOURCE_TYPE);
        builder.append(" INT NOT NULL,");
        builder.append(PlaybackQueueColumns.SOURCE_POSITION);
        builder.append(" INT NOT NULL);");*/
        db.execSQL(builder.toString());

     /*   builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackHistoryColumns.NAME);
        builder.append("(");
        builder.append(PlaybackHistoryColumns.POSITION);
        builder.append(" INT NOT NULL);");
        db.execSQL(builder.toString());*/

        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStore.RecentStoreColumns.NAME + " ("
                + RecentStore.RecentStoreColumns.ID + " LONG NOT NULL," + RecentStore.RecentStoreColumns.TIMEPLAYED
                + " LONG NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME);
        // db.execSQL("DROP TABLE IF EXISTS " + PlaybackHistoryColumns.NAME);
        onCreate(db);
    }

    public synchronized void saveState(long[] list) {
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
        database.beginTransaction();
        try {
            for (int i = 0; i < list.length; i++) {
                ContentValues cv = new ContentValues(1);
                cv.put(PlaybackQueueColumns.TRACK_ID, list[i]);
                database.insert(PlaybackQueueColumns.NAME, null, cv);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized long[] getState() {
        final SQLiteDatabase database = getReadableDatabase();
        long[] list = null;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    PlaybackQueueColumns.NAME, null, null, null, null, null, null);
            list = new long[cursor.getCount()];
            int i = 0;
            while (cursor.moveToNext()) {
                list[i] = cursor.getLong(0);
                i++;
            }
            return list;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public class PlaybackQueueColumns {
        public static final String NAME = "playbackqueue";
        public static final String TRACK_ID = "trackid";
        public static final String SOURCE_ID = "sourceid";
        public static final String SOURCE_TYPE = "sourcetype";
        public static final String SOURCE_POSITION = "sourceposition";
    }

    public class PlaybackHistoryColumns {
        public static final String NAME = "playbackhistory";
        public static final String POSITION = "position";
    }
}
