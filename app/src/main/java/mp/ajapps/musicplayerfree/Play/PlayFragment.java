package mp.ajapps.musicplayerfree.Play;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import mp.ajapps.musicplayerfree.Adapters.AlbumArt_Pager_Adapter;
import mp.ajapps.musicplayerfree.Helpers.BitmapUtils;
import mp.ajapps.musicplayerfree.Helpers.ExeTimeCalculator;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;


public class PlayFragment extends Fragment {

    private static final String TAG = PlayFragment.class.getSimpleName();
    private final Handler mHandler = new Handler();
    private AlbumArt_Pager_Adapter mALbumArtAdapter;
    private ViewPager mViewPager;
    private long mDuration;
    private ImageView mPlayPause;
    private ImageView mNext;
    private ImageView mPrev;
    private ImageView mShuffle;
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
        onServiceConnected();
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
        mViewPager = (ViewPager) v.findViewById(R.id.view2);
        mALbumArtAdapter = new AlbumArt_Pager_Adapter(getFragmentManager(), getActivity());
        mViewPager.setAdapter(mALbumArtAdapter);
        mViewPager.setCurrentItem(MusicUtils.getQueuePosition());
        tvArtist = (TextView) v.findViewById(R.id.textViewArtist);
        tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
        int den = getResources().getDisplayMetrics().densityDpi;
        tvCurrentTime = (TextView) v.findViewById(R.id.textView6);
        tvFullTime = (TextView) v.findViewById(R.id.textView7);
         AdView mAdView = (AdView) v.findViewById(R.id.adView);
         AdRequest adRequest = new AdRequest.Builder().addTestDevice("7971949E9B14F3AB74918D51DB72B497").build();
         mAdView.loadAd(adRequest);
        exm.addTimeFrame("1");
        mContext.startService(new Intent(mContext, IMusicChild.class));
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
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                int current = 0;
                current = MusicUtils.getQueuePosition();
                if (position - current == 1) {
                    try {
                        MusicUtils.mService.pagerNextPlay(current + 1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (position - current == -1) {
                    try {
                        MusicUtils.mService.pagerNextPlay(current - 1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
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

    private void onServiceConnected() {
        mALbumArtAdapter.setLength(MusicUtils.getQueueSize());
        mALbumArtAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(MusicUtils.getQueuePosition());
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

    private void updateNowPlayingInfo() {
        try {
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
                mViewPager.setCurrentItem(MusicUtils.getQueuePosition(), true);
                updateNowPlayingInfo();
            } else if (action.equals(IMusicChild.Queue_Changed)) {
                Toast.makeText(getActivity(), "chnged", Toast.LENGTH_SHORT).show();
                mALbumArtAdapter = new AlbumArt_Pager_Adapter(getFragmentManager(), getActivity());
                mALbumArtAdapter.setLength(MusicUtils.getQueueSize());
                mViewPager.setAdapter(mALbumArtAdapter);
                mViewPager.setCurrentItem(MusicUtils.getQueuePosition());
            }
        }
    }
}
