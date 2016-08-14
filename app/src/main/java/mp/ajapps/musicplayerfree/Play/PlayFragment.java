package mp.ajapps.musicplayerfree.Play;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;

import mp.ajapps.musicplayerfree.Helpers.ExeTimeCalculator;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;


public class PlayFragment extends Fragment {

    private static final String TAG = PlayFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private long mDuration;
    private ImageView mPlayPause;
    private ImageView mNext;
    private ImageView mPrev;
    private ImageView mShuffle, mViewPagerImage;
    private ImageView mRepeat;
    private Context mContext;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvFullTime;
    private TextView tvCurrentTime;
    private SeekBar mSeekBar;
    private MyReceiver myf = null;
    private Boolean mPaused;
    private final Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private ExeTimeCalculator exm = new ExeTimeCalculator();

    public PlayFragment() {}

    public static PlayFragment newInstance() {
        return new PlayFragment();
    }

    private void updateProgress() {
        if (!mPaused) {
            mHandler.removeCallbacks(mUpdateProgress);
            updateAsset();
            mHandler.postDelayed(mUpdateProgress, 500);
        }
    }

    private void updateAsset() {
        long pos = MusicUtils.getPosition();
        tvCurrentTime.setText(MusicUtils.makeTimeString(this.getActivity(), pos / 1000));
        if (mDuration > 0)
            mSeekBar.setProgress((int) (1000 * pos / mDuration));
    }

    @Override
    public void onStart() {
        super.onStart();
        mPaused = false;
        updateNowPlayingInfo();
        updateProgress();
    }

    @Override
    public void onStop() {
        mPaused = true;
        mHandler.removeCallbacks(mUpdateProgress);
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNowPlayingInfo();
        if (myf == null) {
            myf = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IMusicChild.META_CHANGED);
            intentFilter.addAction(IMusicChild.Queue_Changed);
            getActivity().registerReceiver(myf, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myf != null) {
            getActivity().unregisterReceiver(myf);
            myf = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_play, container, false);
        mContext.startService(new Intent(mContext, IMusicChild.class));
        mViewPagerImage = (ImageView) v.findViewById(R.id.view2);
        tvArtist = (TextView) v.findViewById(R.id.textViewArtist);
        tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
        tvCurrentTime = (TextView) v.findViewById(R.id.textView6);
        tvFullTime = (TextView) v.findViewById(R.id.textView7);
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        mPlayPause = (ImageView) v.findViewById(R.id.imageButton3);
        mNext = (ImageView) v.findViewById(R.id.imageButton4);
        mPrev = (ImageView) v.findViewById(R.id.imageButton2);
        mShuffle = (ImageView) v.findViewById(R.id.imageButton5);
        mRepeat = (ImageView) v.findViewById(R.id.imageButton1);
        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.togglePlay();
                    updatePlayingDraw();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.goToNext();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.goToPrev();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.toggleShuffle();
                    drawRepeats();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.toggleRepeat();
                    drawRepeats();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (userTouch) {
                    long mPosOverride = mDuration * progress / 1000;
                    MusicUtils.seekSong(mPosOverride);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userTouch = false;
            }
        });
        tvFullTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAsset();
            }
        });
        updateNowPlayingInfo();
        drawRepeats();

        FrameLayout mLl = (FrameLayout) v.findViewById(R.id.mGesture);
        mLl.setOnTouchListener(new OnSwipeTouchListener(getActivity(), metrics.density));

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void updatePlayingDraw() {
        try {
            if (MusicUtils.mService.isPlaying()) {
                mPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                mPlayPause.setImageResource(R.drawable.ic_play);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void drawRepeats(){
        try {
            mRepeat.setImageResource(MusicUtils.mService.getRepeatMode() == 1 ?  R.drawable.ic_repeat : R.drawable.ic_repea_stop);
            mShuffle.setImageResource(MusicUtils.mService.getShuffleMode() == 0 ? R.drawable.shuffle_stop : R.drawable.ic_shufeel);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateNowPlayingInfo() {
        try {
            String path = MusicUtils.mService.getAlbumArt();
            ImageLoader.getInstance().displayImage("file:///" + path, mViewPagerImage);
            tvTitle.setText(MusicUtils.mService.getTrackName());
            tvArtist.setText(MusicUtils.mService.getArtistName());
            mDuration = MusicUtils.mService.duration();
            tvFullTime.setText(MusicUtils.makeTimeString(mContext, mDuration / 1000));
            updatePlayingDraw();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {}

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(IMusicChild.META_CHANGED)) {
                String path = null;
                try {
                    path = "file:///" + MusicUtils.mService.getAlbumArt();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                ImageLoader.getInstance().displayImage(path, mViewPagerImage);
                updateNowPlayingInfo();
            } else if (action.equals(IMusicChild.Queue_Changed)) {
                String path = null;
                try {
                    path = "file:///" + MusicUtils.mService.getAlbumArt();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                ImageLoader.getInstance().displayImage(path, mViewPagerImage);
            }
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;
        private int SWIPE_THRESHOLD = 100;

        public OnSwipeTouchListener (Context ctx, float pix){
            SWIPE_THRESHOLD = ((int) Math.round(pix * 25));
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_VELOCITY_THRESHOLD = 800;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //onSingle();
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;

                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                        result = true;
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                    result = true;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight() {
            Toast.makeText(getActivity(), "right", Toast.LENGTH_LONG).show();
            try {
                MusicUtils.mService.goToPrev();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onSwipeLeft() {
            Toast.makeText(getActivity(), "right", Toast.LENGTH_LONG).show();

            try {
                MusicUtils.mService.goToNext();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
}
