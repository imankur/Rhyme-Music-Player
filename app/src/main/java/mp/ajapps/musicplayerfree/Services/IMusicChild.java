package mp.ajapps.musicplayerfree.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;

import mp.ajapps.musicplayerfree.Activity.MainActivity;
import mp.ajapps.musicplayerfree.Helpers.ExeTimeCalculator;
import mp.ajapps.musicplayerfree.Helpers.MusicPlaybackState;
import mp.ajapps.musicplayerfree.Helpers.RecentStore;
import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.R;

public class IMusicChild extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String META_CHANGED = "mp.ajapps.musicplayerfree.metachanged";
    public static final String NEXT_ACTION = "mp.ajapps.musicplayerfree.next";
    public static final String PLAY_TOGGLE_ACTION = "mp.ajapps.musicplayerfree.playtoggle";
    public static final String PREV_ACTION = "mp.ajapps.musicplayerfree.prev";
    public static final String Queue_Changed = "mp.ajapps.musicplayerfree.Queue";
    public static final int SHUFFLE_AUTO = 2;
    private static final int SHUFFLE_NONE = 0;
    private static final int SHUFFLE_NORMAL = 1;
    private static final int REPEAT_NONE = 0;
    private static final int REPEAT_CURRENT = 1;
    private static final String TAG = "IMusicChildService";
    private static final String[] PROJECTION = new String[]{
            "audio._id AS _id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID
    };
    private static final String[] ALBUM_PROJECTION = new String[]{
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.LAST_YEAR, MediaStore.Audio.Albums.ALBUM_ART
    };
    private final IBinder iBinder = new ServiceStub(this);
    private final Random mRandom = new Random();
    private int mRepeatMode = REPEAT_NONE;
    private int mShuffleMode = SHUFFLE_NONE;
    private SharedPreferences mPreferences;
    private int mPlayListLen = 0;
    private Cursor mCursor, mAlbumCursor;
    private boolean mIsPlaying = false;
    private NotificationManager mNotificationManager;
    private Notification notificationCompat;
    private int mPlayPos = 0;
    private MediaPlayer mMediaPlayer, mNextMediaPlayer;
    private long[] mPlayList = null;
    private MusicPlaybackState mPlaybackState;
    private RecentStore mRecentStore;
    private int mNextPlayPos = -1;
    private int mOpenFailedCounter = 0;
    private ArrayList<String> mCurrentInfoList;
    private boolean mTelePlaying = false;
    private ServiceReciver mReciver =null;
private TelephonyManager tm;
    private TelePhonyDetector td;
    public IMusicChild() {
    }

    public class TelePhonyDetector extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING :
                    mTelePlaying = isPlaying() ? true :  false;
                    mMediaPlayer.pause();
                    break;
                case TelephonyManager.CALL_STATE_IDLE :
                    if (mTelePlaying)
                    mMediaPlayer.start();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK :
                    //mMediaPlayer.start();
                    break;
            }
        }
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

    private class HeadSetReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Headset unplugged");
                        break;
                    case 1:
                        Log.d(TAG, "Headset plugged");
                        break;
                }
            }
        }
    }

    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "on create ", Toast.LENGTH_SHORT).show();
        mPlaybackState = new MusicPlaybackState(this);
        mRecentStore = RecentStore.getInstance(this);
        mPreferences = getSharedPreferences("service", 0);
        mReciver = new ServiceReciver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(PLAY_TOGGLE_ACTION);
        mFilter.addAction(PREV_ACTION);
        mFilter.addAction(NEXT_ACTION);
        mFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        mFilter.addAction(META_CHANGED);

        mFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(mReciver, mFilter);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mNextMediaPlayer = new MediaPlayer();
           // mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        td = new TelePhonyDetector();
        tm.listen(td, PhoneStateListener.LISTEN_CALL_STATE);
        mCurrentInfoList = new ArrayList<String>();
        reloadQueue();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()).putExtra("tName",this.getTrackName()).putExtra("aName",getArtistName()));
    }

    private class ServiceReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NEXT_ACTION :
                    goToNext();
                    break;
                case PREV_ACTION :
                    goToPrev();
                    break;
                case PLAY_TOGGLE_ACTION :
                    togglePlay();
                    break;
                case META_CHANGED :
                    buildNotification();
                    break;
                case Intent.ACTION_NEW_OUTGOING_CALL :
                    mMediaPlayer.pause();
                    break;
                case Intent.ACTION_HEADSET_PLUG :
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case 0:
                            mMediaPlayer.pause();
                            break;
                        case 1:
                            Log.d(TAG, "Headset plugged");
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
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
        setNextDataSource();
        return mRepeatMode;
    }

    private void buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_launcher)
                .setContentTitle(this.getTrackName())
                .setOngoing(true)
                .setContentText(this.getArtistName())
                .setContentIntent(pIntent)
                .setAutoCancel(false);

        notificationCompat = mBuilder.build();
        notificationCompat.priority = Notification.PRIORITY_HIGH;
        RemoteViews notiLayoutBig = new RemoteViews(getPackageName(),
                R.layout.notification_layout);
        RemoteViews notiCollapsedView = new RemoteViews(getPackageName(),
                R.layout.notification_small);
        notificationCompat.bigContentView = notiLayoutBig;
        notificationCompat.contentView = notiCollapsedView;

        Intent intent0 = new Intent();
        intent0.setAction( PLAY_TOGGLE_ACTION );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 21111, intent0, 0 );
        notificationCompat.bigContentView.setOnClickPendingIntent( R.id.noti_play_button, pendingIntent );
        notificationCompat.contentView.setOnClickPendingIntent( R.id.noti_play_button, pendingIntent );
        Intent intent1 = new Intent();
        intent1.setAction( NEXT_ACTION );
        pendingIntent = PendingIntent.getBroadcast(this, 22222, intent1, 0 );
        notificationCompat.bigContentView.setOnClickPendingIntent( R.id.noti_next_button, pendingIntent );
        notificationCompat.contentView.setOnClickPendingIntent( R.id.noti_next_button, pendingIntent );
Intent intent2 = new Intent();
        intent2.setAction(PREV_ACTION);
        pendingIntent = PendingIntent.getBroadcast(this, 39222, intent2, 0);
        notificationCompat.bigContentView.setOnClickPendingIntent( R.id.noti_prev_button, pendingIntent );
        notificationCompat.contentView.setOnClickPendingIntent( R.id.noti_prev_button, pendingIntent );
        ImageLoader.getInstance().loadImage("file:///" + getAlbumArt(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                notificationCompat.bigContentView.setTextViewText(R.id.noti_name, getTrackName());
                notificationCompat.bigContentView.setTextViewText(R.id.noti_artist, getArtistName());
                notificationCompat.contentView.setTextViewText(R.id.noti_name, getTrackName());
                notificationCompat.contentView.setTextViewText(R.id.noti_artist, getArtistName());
                notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art, loadedImage);
                notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art, loadedImage);
                if (isPlaying()) {
                    notificationCompat.contentView.setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause);
                    notificationCompat.bigContentView.setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause);
                } else {
                    notificationCompat.contentView.setImageViewResource(R.id.noti_play_button, R.drawable.ic_play);
                    notificationCompat.bigContentView.setImageViewResource(R.id.noti_play_button, R.drawable.ic_play);
                }

                mNotificationManager.notify(4, notificationCompat);
            }
        });

    }

    public long[] getQueue() {
        synchronized (this) {
            return mPlayList;
        }
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
        ExeTimeCalculator exeTimeCalculator = new ExeTimeCalculator();
        exeTimeCalculator.addTimeFrame("x");
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
        exeTimeCalculator.addTimeFrame("o");

        if (position >= 0) {
            mPlayPos = position;
        } else {
            mPlayPos = 0;
        }
        updateCursor(mPlayList[position]);
        setDataSourceInMp(mMediaPlayer, mPlayPos);
        playPlayer(mMediaPlayer, mPlayPos);
        setNextDataSource();
    }

    void setAndPlayQueue(int pos) {
        updateCursor(mPlayList[pos]);
        if (pos >= 0) {
            mPlayPos = pos;
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
        Toast.makeText(this, "destroy " + mMediaPlayer.getCurrentPosition(), Toast.LENGTH_SHORT).show();

        mIsPlaying = false;
        mNotificationManager.cancel(4);
        if (mReciver != null) {
            unregisterReceiver(mReciver);
            mReciver = null;
        }
        mMediaPlayer.release();
        tm.listen(td, PhoneStateListener.LISTEN_NONE);
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
        Log.i("rickey", "saveQueue: " + mPlayPos + "--" + mPlayList[mPlayPos]);
        editor.putInt("seekpos", mMediaPlayer.getCurrentPosition());
        Toast.makeText(this, "savve " + mMediaPlayer.getCurrentPosition(), Toast.LENGTH_SHORT).show();
        editor.putInt("shufflemode", mShuffleMode);
        editor.apply();
        mPlaybackState.saveState(mPlayList);
    }

    private void reloadQueue() {
        mPlayList = mPlaybackState.getState();
        int seek = mPreferences.getInt("seekpos", 1000);
        mPlayPos = mPreferences.getInt("curpos", 0);
        Toast.makeText(this, "reload " + seek, Toast.LENGTH_SHORT).show();
        Log.i("rickey", "reloadQueue: " + seek);
        mShuffleMode = mPreferences.getInt("shufflemode", 0);
        if (mPlayList.length > 0) {
            updateCursor(mPlayList[mPlayPos]);
            try {
                setupPlayer(mMediaPlayer, mPlayPos);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setNextDataSource();
            seekSong(seek);
            mNextMediaPlayer.seekTo(seek);
        } else {

        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mPlayList.length > 0) {
            saveQueue();
        }
        return true;
    }

    private boolean setDataSourceInMp(MediaPlayer player, int pos) {
        try {
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
        buildNotification();
        sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()).putExtra("tName",this.getTrackName()).putExtra("aName",getArtistName()));
    }

    private void setupPlayer(MediaPlayer player, int pos) throws IOException {
        player.reset();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mPlayList[pos]);
        player.setDataSource(getApplicationContext(), contentUri);
        player.prepare();
        player.setOnCompletionListener(this);
    }

    private void seekSong(final long whereto) {
        mMediaPlayer.seekTo((int) whereto);
    }

    private int getNextPlayInt() {
        if (mShuffleMode == SHUFFLE_NORMAL) {
            return mRandom.nextInt(mPlayList.length);
        } else if (mShuffleMode == SHUFFLE_NONE) {
            int temp = mPlayPos + 1;
            if (temp >= mPlayList.length) {
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
        if (mRepeatMode == REPEAT_CURRENT) {
            mNextPlayPos = mPlayPos;
        } else {
            mNextPlayPos = getNextPlayInt();
        }
        mMediaPlayer.setNextMediaPlayer(null);
        if (mNextPlayPos < 0) {
            return;
        }
        if (mNextMediaPlayer == null) {
            mNextMediaPlayer = new MediaPlayer();
        }
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
        Log.i("rickey", "updateCursor: " );
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

    private int getPosition() {
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
            sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()).putExtra("tName",this.getTrackName()).putExtra("aName",getArtistName()));
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

    private void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            mPlayListLen = mPlayList.length;

            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
                final long tmp = mPlayList[index1];
                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i + 1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                    mPlayPos--;
                }
            } else if (index2 < index1) {
                final long tmp = mPlayList[index1];
                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i - 1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                    mPlayPos++;
                }
            }
            sendBroadcast(new Intent().setAction(Queue_Changed));
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
            sendBroadcast(new Intent().setAction(IMusicChild.META_CHANGED).putExtra("albumArt", this.getAlbumArt()).putExtra("tName",this.getTrackName()).putExtra("aName",getArtistName()));
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
            return mService.get().getAlbumId();
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

        @Override
        public long[] getQueue() throws RemoteException {
            return this.mService.get().getQueue();
        }

        @Override
        public void moveQueueItem(int index1, int index2) throws RemoteException {
            mService.get().moveQueueItem(index1, index2);
        }

        @Override
        public void setAndPlayQueue(int pos) throws RemoteException {
            mService.get().setAndPlayQueue(pos);
        }

        @Override
        public int getShuffleMode() throws RemoteException {
            return mService.get().mShuffleMode;
        }

        @Override
        public int getRepeatMode() throws RemoteException {
            return mService.get().mRepeatMode;
        }
    }
}
