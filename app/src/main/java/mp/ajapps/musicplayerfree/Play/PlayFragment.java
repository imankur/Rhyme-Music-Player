package mp.ajapps.musicplayerfree.Play;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;

import mp.ajapps.musicplayerfree.Adapters.AlbumArt_Pager_Adapter;
import mp.ajapps.musicplayerfree.Helpers.BitmapUtils;
import mp.ajapps.musicplayerfree.Helpers.ExeTimeCalculator;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;


public class PlayFragment extends Fragment {

    private AlbumArt_Pager_Adapter mALbumArtAdapter;
    private ViewPager mViewPager;
    private long mDuration;
    private ImageView mPlayPause;
    private ImageView ll;
    private ImageView mNext;
    private ImageView mPrev;
    private ImageView mShuffle;
    private ImageView mRepeat;
    private Context mContext;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvFullTime;
    private TextView tvCurrentTime;
    DepthPageTransformer trans = new DepthPageTransformer();
    private SeekBar mSeekBar;
    private MyReceiver myf = null;
    private static final long UPDATE_FREQUENCY_MS = 500;
    private Boolean mPaused;

    private ExeTimeCalculator exm = new ExeTimeCalculator();

    private final Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private final Handler mHandler = new Handler();

    private void updateProgress() {
        if(!mPaused){
            mHandler.removeCallbacks(mUpdateProgress);
            updateAsset();
            mHandler.postDelayed(mUpdateProgress,500);
        }
    }

    private void updateAsset() {
        long pos = MusicUtils.getPosition();
        tvCurrentTime.setText(MusicUtils.makeTimeString(this.getActivity(), pos / 1000));
        if (mDuration > 0)
            mSeekBar.setProgress((int) (1000 * pos / mDuration));
    }
    private static final String TAG = PlayFragment.class.getSimpleName();

    // TODO: Rename and change types and number of parameters
    public static PlayFragment newInstance() {
        return new PlayFragment();
    }

    public PlayFragment() {
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
            intentFilter.addAction(IMusicChild.NEXT_ACTION);
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
        exm.addTimeFrame("A");
        View v =  inflater.inflate(R.layout.fragment_play, container, false);
        mViewPager = (ViewPager) v.findViewById(R.id.view2);
        mALbumArtAdapter = new AlbumArt_Pager_Adapter(getFragmentManager(), getActivity());
        mViewPager.setAdapter(mALbumArtAdapter);
      //  mViewPager.setPageTransformer(true, trans);
        ll = (ImageView) v.findViewById(R.id.myll);
        tvArtist = (TextView) v.findViewById(R.id.textViewArtist);
        tvTitle = (TextView) v.findViewById(R.id.textViewTitle);
        int den = getResources().getDisplayMetrics().densityDpi;
        Toast.makeText(getActivity(),""+den, Toast.LENGTH_LONG).show();
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Alegreya-Italic.ttf");
        //tvArtist.setTypeface(tf);
        //tvTitle.setTypeface(tf);
        exm.addTimeFrame("B");
        tvCurrentTime = (TextView) v.findViewById(R.id.textView6);
        tvFullTime = (TextView) v.findViewById(R.id.textView7);
       // AdView mAdView = (AdView) v.findViewById(R.id.adView);
       // AdRequest adRequest = new AdRequest.Builder().addTestDevice("7971949E9B14F3AB74918D51DB72B497").build();
       // mAdView.loadAd(adRequest);
        exm.addTimeFrame("1");
        mContext.startService(new Intent(mContext, IMusicChild.class));
        mPlayPause = (ImageView) v.findViewById(R.id.imageButton3);
        mNext = (ImageView) v.findViewById(R.id.imageButton4);
        mPrev = (ImageView) v.findViewById(R.id.imageButton2);
        mShuffle = (ImageView) v.findViewById(R.id.imageButton5);
        mRepeat = (ImageView) v.findViewById(R.id.imageButton1);
        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        //mContext.bindService(new Intent(mContext, IMusicChild.class), this, 0);

            exm.addTimeFrame("C");

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
        exm.addTimeFrame("D");

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
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
                  long   mPosOverride = mDuration * progress / 1000;
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
        exm.addTimeFrame("E"); exm.printDifference();
        return  v;
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
        //MusicUtils.mService = IMusicParent.Stub.asInterface(service);
        mALbumArtAdapter.setLength(MusicUtils.getQueueSize());
        this.updateBlur();
        mALbumArtAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(MusicUtils.getQueuePosition());
    }

    private void updatePlayingDraw() {
        try {
            if(MusicUtils.mService.isPlaying()) {
                mPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                mPlayPause.setImageResource(R.drawable.ic_play);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
   /* @Override
    public void onServiceDisconnected(ComponentName name) {
        //MusicUtils.mService = null;
    }*/

    private void updateNowPlayingInfo() {
        Log.i("xxxxxx", "updateNowPlayingInfo: ");
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

    private void updateBlur() {
        try {
            Log.i("xyxyxy", "updateBlur: " +  MusicUtils.mService.getTrackName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ImageLoader.getInstance().displayImage("file:///" + MusicUtils.getCurrentAlbumArt(), this.ll, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {}
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap bmp = BitmapUtils.createBlurredBitmap(loadedImage);
                ll.setImageBitmap(bmp);
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {}
        });
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(IMusicChild.META_CHANGED)) {
                mViewPager.setCurrentItem(MusicUtils.getQueuePosition(), true);
                updateNowPlayingInfo();
                updateBlur();
            } else if (action.equals(IMusicChild.NEXT_ACTION)) {

            }
        }
    }

    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
               // view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
              //  view.setAlpha(0);
            }
        }
    }
}
