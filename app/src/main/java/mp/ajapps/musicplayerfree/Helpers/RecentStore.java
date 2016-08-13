package mp.ajapps.musicplayerfree.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RecentStore extends SQLiteOpenHelper {
    public static final String DATABASENAME = "musicdb.db";
    private static RecentStore mInstance;

    public RecentStore(Context context) {
        super(context, DATABASENAME, null, 1);
    }

    public static final synchronized RecentStore getInstance(final Context context) {
        if (mInstance == null) {
            mInstance = new RecentStore(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    /*    db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL," + RecentStoreColumns.TIMEPLAYED
                + " LONG NOT NULL);");*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        onCreate(db);
    }

    public void saveSongId(final long songId) {
        Log.i("rrecive", "saveSongId: ---");
        final SQLiteDatabase database = getWritableDatabase();
        final ContentValues values = new ContentValues(2);
        database.beginTransaction();

        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", new String[]{
                String.valueOf(songId)
        });

        values.put(RecentStoreColumns.ID, songId);
        values.put(RecentStoreColumns.TIMEPLAYED, System.currentTimeMillis());
        database.insert(RecentStoreColumns.NAME, null, values);

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public Cursor queryRecentIds() {
        final SQLiteDatabase database = getReadableDatabase();
        return database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID}, null, null, null, null,
                RecentStoreColumns.TIMEPLAYED + " DESC", null);
    }

    public void removeItem(final long songId) {
        final SQLiteDatabase database = getWritableDatabase();
        String WHERE_ID_EQUALS = RecentStoreColumns.ID + "=?";
        database.delete(RecentStoreColumns.NAME, WHERE_ID_EQUALS, new String[]{String.valueOf(songId)});
    }

    public interface RecentStoreColumns {
        /* Table name */
        public static final String NAME = "recenthistory";

        /* Album IDs column */
        public static final String ID = "songid";

        /* Time played column */
        public static final String TIMEPLAYED = "timeplayed";
    }
}