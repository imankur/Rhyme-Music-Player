package mp.ajapps.musicplayerfree.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

import mp.ajapps.musicplayerfree.Activity.MainActivity;
import mp.ajapps.musicplayerfree.Helpers.MusicPlaybackState;
import mp.ajapps.musicplayerfree.Helpers.RecentStore;
import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.Models.AlbumArtistDetails;
import mp.ajapps.musicplayerfree.R;

public class IMusicChild extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String META_CHANGED = "mp.ajapps.musicplayerfree.metachanged";
    public static final String NEXT_ACTION = "mp.ajapps.musicplayerfree.next";
    private static final int SHUFFLE_NONE = 0;
    private static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    private static final int REPEAT_NONE = 0;
    private static final int REPEAT_CURRENT = 1;
    private int mRepeatMode = REPEAT_NONE;
    private int mShuffleMode = SHUFFLE_NONE;

    private SharedPreferences mPreferences;
    private static final String TAG = "IMusicChildService";
    private final IBinder iBinder = new ServiceStub(this);
    private int mPlayListLen = 0;
    private Cursor mCursor, mAlbumCursor;
    private boolean mIsPlaying = false;
    private NotificationManager mNotificationManager;
    private int mPlayPos = 0;
    private MediaPlayer mMediaPlayer,mNextMediaPlayer;
    private long[] mPlayList = null;
    private MusicPlaybackState mPlaybackState;
    private RecentStore mRecentStore;
    private int mNextPlayPos = -1;
    private int mOpenFailedCounter = 0;
    private ArrayList<String> mCurrentInfoList;
    private final Random mRandom = new Random();

    private static final String[] PROJECTION = new String[] {
            "audio._id AS _id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID
    };

    private static final String[] ALBUM_PROJECTION = new String[] {
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.LAST_YEAR, MediaStore.Audio.Albums.ALBUM_ART
    };

    public IMusicChild() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    private void setPlayList(long[] arr) {
        this.mPlayList = arr;
    }

    public void play() {
        if (mMediaPlayer != null) mMediaPlayer.reset();
        buildNotification();
        mIsPlaying = true;
        mMediaPlayer.start();
    }

    private long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0) {
                return mPlayList[mPlayPos];
            }
        }
        return -1;
    }

    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlaybackState = new MusicPlaybackState(this);
        mRecentStore = RecentStore.getInstance(this);
        mPreferences = getSharedPreferences("service",0);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }
        mCurrentInfoList = new ArrayList<String>();
        reloadQueue();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()));
    }

    private long getQueueItemAtPosition(int position) {
        synchronized (this) {
            if (position >= 0 && position < mPlayList.length) {
                return mPlayList[position];
            }
        }
        return -1;
    }

    private int toggleRepeat() {
        if (mRepeatMode == REPEAT_NONE) {
            mRepeatMode = REPEAT_CURRENT;
        } else {
            mRepeatMode = REPEAT_NONE;
        }
        return mRepeatMode;
    }
    private void buildNotification() {
        String strtitle = "titlll";
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("title", strtitle);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_play)
                .setTicker("sfsdfsdf")
                .setContentTitle(this.getTrackName())
                .setContentText(this.getArtistName())
                .addAction(R.drawable.icon_pause, "act", pIntent)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        ImageLoader.getInstance().loadImage("file:///" + getAlbumArt(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mBuilder.setLargeIcon(loadedImage);
                mNotificationManager.notify(0, mBuilder.build());
            }
        });
    }



    private void showNotification( boolean isPlaying ) {
        final Notification notification = new NotificationCompat.Builder( getApplicationContext() )
                .setAutoCancel( true )
                .setSmallIcon( R.drawable.ic_play )
                .setContentTitle( getString( R.string.app_name ) )
                .build();

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
            notification.bigContentView = getExpandedView(isPlaying());
        mNotificationManager.cancel(1);
        ImageLoader.getInstance().loadImage("file:///" + getAlbumArt(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                notification.bigContentView.setImageViewBitmap(R.id.large_icon, loadedImage);
                mNotificationManager.notify(1, notification);
            }
        });
    }


    private RemoteViews getExpandedView(boolean isPlaying) {
        RemoteViews customView = new RemoteViews( getPackageName(), R.layout.custom_noti);
        customView.setImageViewResource( R.id.large_icon, R.drawable.default_artwork);
        if(isPlaying)
            customView.setImageViewResource( R.id.ib_play_pause, R.drawable.ic_pause);
        else
            customView.setImageViewResource( R.id.ib_play_pause, R.drawable.ic_play);
        customView.setTextViewText(R.id.track, getTrackName());
        customView.setTextViewText(R.id.artist,getArtistName());
        //customView.setImageViewResource( R.id.ib_fast_forward, R.drawable.ic_fast_forward );

        Intent intent = new Intent( getApplicationContext(), MainActivity.class );

      /*  intent.setAction( ACTION_NOTIFICATION_PLAY_PAUSE );
        PendingIntent pendingIntent = PendingIntent.getService( getApplicationContext(), 1, intent, 0 );
        customView.setOnClickPendingIntent( R.id.ib_play_pause, pendingIntent );

        intent.setAction( ACTION_NOTIFICATION_FAST_FORWARD );
        pendingIntent = PendingIntent.getService( getApplicationContext(), 1, intent, 0 );
        customView.setOnClickPendingIntent( R.id.ib_fast_forward, pendingIntent );

        intent.setAction(ACTION_NOTIFICATION_REWIND);
        pendingIntent = PendingIntent.getService( getApplicationContext(), 1, intent, 0 );
        customView.setOnClickPendingIntent(R.id.ib_rewind, pendingIntent);*/

        return customView;
    }


/*private MediaSession mSession ;
    private void setUpMediaSession() {
        mSession = new MediaSession(this, "Eleven");
        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPause() {

            }
            @Override
            public void onPlay() {
                play();
            }
            @Override
            public void onSeekTo(long pos) {
                seekSong(pos);
            }
            @Override
            public void onSkipToNext() {
                gotoNext(true);
            }
            @Override
            public void onSkipToPrevious() {
                prev(false);
            }
            @Override
            public void onStop() {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                releaseServiceUiAndStop();
            }
        });
        mSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }*/


    private void setAndPlay(long[] list, int position) {

        int listlength = list.length;
        boolean newlist = true;
        if (mPlayListLen == listlength) {
            newlist = false;
            for (int i = 0; i < listlength; i++) {
                if (list[i] != mPlayList[i]) {
                    newlist = true;
                    break;
                }
            }
        }
        if (newlist) {
            this.mPlayList = list;
            this.mPlayListLen = list.length;
        }
        updateCursor(mPlayList[position]);
        if (position >= 0) {
            mPlayPos = position;
        } else {
            mPlayPos = 0;
        }

        setDataSourceInMp(mMediaPlayer, mPlayPos);
        playPlayer(mMediaPlayer, mPlayPos);
        setNextDataSource();
    }

    private void pagerNextPlay(int position) {
        mPlayPos = position;
        updateCursor(mPlayList[position]);
        setDataSourceInMp(mMediaPlayer, position);
        playPlayer(mMediaPlayer, mPlayPos);
        setNextDataSource();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsPlaying = false;
        mMediaPlayer.release();
    }

    private int toggleShuffle() {
        int status = 0;
        if (mShuffleMode == SHUFFLE_NORMAL) {
            mShuffleMode = SHUFFLE_NONE;
            status = 0;
        } else {
            mShuffleMode = SHUFFLE_NORMAL;
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("shufflemode", mShuffleMode);
        editor.apply();
        setNextDataSource();
        return mShuffleMode;
    }



    private void saveQueue() {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("curpos", mPlayPos);
        editor.putLong("seekpos", mMediaPlayer.getCurrentPosition());
        editor.putInt("shufflemode", mShuffleMode);
        editor.apply();
        mPlaybackState.saveState(mPlayList);
        Log.i("bugss", "SaveQueue: -" + "-"+ mMediaPlayer.getCurrentPosition() +"--" + mPlayList.length + "-- " + mPlayList[mPlayPos]);
    }

    private void reloadQueue() {
        mPlayList = mPlaybackState.getState();
        long seek = mPreferences.getLong("seekpos", 1);

        mPlayPos = mPreferences.getInt("curpos", 0);
        mShuffleMode = mPreferences.getInt("shufflemode", 0);

        if (mPlayList.length > 0) {
            updateCursor(mPlayList[mPlayPos]);
            try {
                setupPlayer(mMediaPlayer, mPlayPos);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()));
            setNextDataSource();
            seekSong(seek);
        } else {

        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        saveQueue();
        return true;
    }

    private boolean setDataSourceInMp(MediaPlayer player, int pos) {
        try {
            if (player != null) {
               // player = mMediaPlayer;
            }
            setupPlayer(player, pos);

        } catch (IOException ex) {
            return false;
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    private void playPlayer(MediaPlayer player, int pos) {
        player.start();
        mRecentStore.saveSongId(mPlayList[pos]);
        sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()));
    }
    private void setupPlayer(MediaPlayer player, int pos) throws IOException {
        player.reset();
        player.setOnPreparedListener(this);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mPlayList[pos]);
        player.setDataSource(getApplicationContext(), contentUri);
        player.prepare();
        player.setOnCompletionListener(this);
    }

    private void seekSong(final long whereto){
        //Log.i("seeku -",""+ (int)whereto);
        mMediaPlayer.seekTo((int)whereto);
    }

    private int getNextPlayInt() {
        if (mShuffleMode == SHUFFLE_NORMAL) {
            return mRandom.nextInt(mPlayList.length);
        } else if (mShuffleMode == SHUFFLE_NONE) {
            int temp = mPlayPos + 1;
            if (temp >= mPlayListLen) {
                return 0;
            } else {
                return temp;
            }
        }
        return 1;
    }

    private int getPrevPlayInt() {
        if (mShuffleMode == SHUFFLE_NORMAL) {
            return mRandom.nextInt(mPlayList.length);
        } else if (mShuffleMode == SHUFFLE_NONE) {
            int temp = mPlayPos - 1;
            if (temp < 0) {
                return mPlayListLen - 1;
            } else {
                return temp;
            }
        }
        return 1;
    }

    private void goToNext() {
        mPlayPos = getNextPlayInt();
        updateCursor(mPlayList[mPlayPos]);
        setDataSourceInMp(mMediaPlayer, mPlayPos);
        playPlayer(mMediaPlayer, mPlayPos);
        setNextDataSource();
    }

    private void goToPrev() {
        mPlayPos = getPrevPlayInt();
        updateCursor(mPlayList[mPlayPos]);
        setDataSourceInMp(mMediaPlayer, mPlayPos);
        playPlayer(mMediaPlayer, mPlayPos);
        setNextDataSource();
    }

    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST_ID));
        }
    }

    private long getAlbumId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID));
        }
    }

    private String getAlbumArt() {
        synchronized (this) {
            if (mAlbumCursor == null) {
                return null;
            }
            return mAlbumCursor.getString(mAlbumCursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
        }
    }

    private int getQueueSize() {
        synchronized (this) {
            return mPlayList.length;
        }
    }

    private int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    private void setNextDataSource() {
        mNextPlayPos = getNextPlayInt();
        mMediaPlayer.setNextMediaPlayer(null);
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
        if (mNextPlayPos < 0) {
            return;
        }
        mNextMediaPlayer = new MediaPlayer();
        mNextMediaPlayer.setWakeMode(IMusicChild.this, PowerManager.PARTIAL_WAKE_LOCK);
       // mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
        if (setDataSourceInMp(mNextMediaPlayer, mNextPlayPos)) {
            mMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
        } else {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
    }

    private synchronized void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mAlbumCursor != null) {
            mAlbumCursor.close();
            mAlbumCursor = null;
        }
    }

    private void updateCursor(final long trackId) {
        updateCursor("_id=" + trackId);
    }

    private void updateCursor(final String selection) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, selection, null);
            mCurrentInfoList.add(0, mPlayPos + "");
            mCurrentInfoList.add(1, mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)));
            mCurrentInfoList.add(1, mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)));
        }
        updateAlbumCursor();
    }

    private void updateCursor(final Uri uri) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(uri, PROJECTION, null, null);
        }
        updateAlbumCursor();
    }

    private void updateAlbumCursor() {
        long albumId = getAlbumId();
        if (albumId >= 0) {
            mAlbumCursor = openCursorAndGoToFirst(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    ALBUM_PROJECTION, "_id=" + albumId, null);
        } else {
            mAlbumCursor = null;
        }
    }

    private int getPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    private Cursor openCursorAndGoToFirst(Uri uri, String[] projection,
                                          String selection, String[] selectionArgs) {
        Cursor c = getContentResolver().query(uri, projection,
                selection, selectionArgs, null, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }

    private String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return " ";
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));
        }
    }

    private void togglePlay() {
        synchronized (this) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mIsPlaying = false;
            } else {
                mMediaPlayer.start();
                mIsPlaying = true;
            }
        }
    }

    private String getArtistName() {
        synchronized (this) {
            if (mCursor == null) {
                return "";
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));
        }
    }

    private long duration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //buildNotification();
      //  showNotification(false);
       // mp.start();
        //mp.start();
        //mRecentStore.saveSongId(mPlayList[mPlayPos]);
       // sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp == mMediaPlayer && mNextMediaPlayer != null) {
            mPlayPos = mNextPlayPos;
            updateCursor(mPlayList[mPlayPos]);
            mMediaPlayer.release();
            mMediaPlayer = mNextMediaPlayer;
            mNextMediaPlayer = null;
            setNextDataSource();
            sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()));
        }
    }

    static class ServiceStub extends IMusicParent.Stub {
        final WeakReference<IMusicChild> mService;
        ServiceStub(IMusicChild service) {
            mService = new WeakReference<IMusicChild>(service);
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {
        }

        @Override
        public void setAndPlay(long[] list, int position) throws RemoteException {
            mService.get().setAndPlay(list, position);
        }

        @Override
        public long getAudioId() {
            return mService.get().getAudioId();
        }

        @Override
        public void setPlayList(long[] arr) throws RemoteException {
            mService.get().setPlayList(arr);
        }

        @Override
        public void play() {
        }

        @Override
        public int getQueueSize() throws RemoteException {
            return mService.get().getQueueSize();
        }

        @Override
        public void pagerNextPlay(int position) throws RemoteException {
            mService.get().pagerNextPlay(position);
        }

        @Override
        public long[] getSongListForCursor() throws RemoteException {
            return new long[0];
        }

        @Override
        public long getAlbumId() throws RemoteException {
            return  mService.get().getAlbumId();
        }

        @Override
        public String getAlbumArt() throws RemoteException {
            return mService.get().getAlbumArt();
        }

        @Override
        public long getQueueItemAtPosition(int position) throws RemoteException {
            return mService.get().getQueueItemAtPosition(position);
        }

        @Override
        public int getQueuePosition() throws RemoteException {
            return mService.get().getQueuePosition();
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        @Override
        public long duration() throws RemoteException {
            return mService.get().duration();
        }

        @Override
        public void seekSong(long time) throws RemoteException {
            mService.get().seekSong(time);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public int getPosition() throws RemoteException {
            return mService.get().getPosition();
        }

        @Override
        public void togglePlay() throws RemoteException {
            mService.get().togglePlay();
        }

        @Override
        public void goToNext() throws RemoteException {
            mService.get().goToNext();
        }

        @Override
        public void goToPrev() throws RemoteException {
            mService.get().goToPrev();
        }

        @Override
        public int toggleShuffle() throws RemoteException {
           return mService.get().toggleShuffle();
        }

        @Override
        public int toggleRepeat() throws RemoteException {
            return this.mService.get().toggleRepeat();
        }
    }
}
