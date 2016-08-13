package mp.ajapps.musicplayerfree.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Adapters.TrackPagerAdap;
import mp.ajapps.musicplayerfree.Fragments.AlbumFragment;
import mp.ajapps.musicplayerfree.Fragments.ArtistFragment;
import mp.ajapps.musicplayerfree.Fragments.PlaylistFragment;
import mp.ajapps.musicplayerfree.Fragments.SuggestionFragment;
import mp.ajapps.musicplayerfree.Fragments.TrackFragment;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.Play.Play_Activity;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;
import mp.ajapps.musicplayerfree.Widgets.NewPlaylistDialog;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    ViewPager mPager;
    TrackPagerAdap mPagerAdap;
    RelativeLayout ll;
    MyReceiver myf = null;
    TextView mTrack, mArtist;
    ImageView im, mPlayim;
    Menu mMenu;
    String TAG = "debuggggg----";
    private GestureDetector gestureDetector;
    @Override
    protected void onStart() {
        Log.i(TAG, "onStart: ");
        startService(new Intent(this, IMusicChild.class));
        Boolean b = bindService(new Intent(this, IMusicChild.class), this, 0);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Log.i(TAG, "onBackPressed: ");
        moveTaskToBack(true);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop: ");
        ImageLoader.getInstance().clearMemoryCache();
        unbindService(this);
       // stopService(new Intent(this, IMusicChild.class));

        super.onStop();
    }

    private void getDensityName(Context context) {
     /*   float density = context.getResources().getDisplayMetrics().density;
        if (density >= 4.0) {
            return "xxxhdpi";
        }
        if (density >= 3.0) {
            return "xxhdpi";
        }
        if (density >= 2.0) {
            return "xhdpi";
        }
        if (density >= 1.5) {
            return "hdpi";
        }
        if (density >= 1.0) {
            return "mdpi";
        }
        return "ldpi";*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.vpView);
        MusicUtils.initImageCacher(this);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("7971949E9B14F3AB74918D51DB72B497").build();
        //mAdView.loadAd(adRequest);
       // Toast.makeText(this, "Screen density is : " + getDensityName(this) +"--"+ getResources().getDimension(R.dimen.up_dimen) , Toast.LENGTH_LONG).show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //  Log.i("dentis", "onCreate:  cff555fff " + metrics.heightPixels/metrics.density +"  "+ metrics.widthPixels/metrics.density +" " + metrics.densityDpi)  ;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar_main);
        setSupportActionBar(toolbar);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_view);
//BugManager.getInstance(this,"");
        ll = (RelativeLayout) findViewById(R.id.clickLayout);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Play_Activity.class));
            }
        });
        mTrack = (TextView) findViewById(R.id.textView4);
        mArtist = (TextView) findViewById(R.id.textView5);
        im = (ImageView) findViewById(R.id.image);
        mPlayim = (ImageView) findViewById(R.id.image1);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        //  fragments.add(SuggestFragment.newInstance());
        fragments.add(new SuggestionFragment());
        //fragments.add(RecentFragment.newInstance());
        fragments.add(TrackFragment.newInstance());
        fragments.add(AlbumFragment.newInstance());
        fragments.add(new ArtistFragment());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       // fragments.add(new PlaylistFragment());
        mPagerAdap = new TrackPagerAdap(getSupportFragmentManager(), fragments);
        mPager.setAdapter(mPagerAdap);
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(3);

        tabLayout.setupWithViewPager(mPager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
             //   invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPlayim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.togglePlay();
                    if (MusicUtils.mService.isPlaying()) {
                        mPlayim.setImageResource(R.drawable.ic_red_pause);
                    } else {
                        mPlayim.setImageResource(R.drawable.ic_red_play);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        ll.setOnTouchListener(new OnSwipeTouchListener(this, metrics.density){
            @Override
            public void onSwipeLeft() {
                Toast.makeText(MainActivity.this, "top", Toast.LENGTH_SHORT).show();
                try {
                    MusicUtils.mService.goToPrev();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSwipeRight() {
                Toast.makeText(MainActivity.this, "Next", Toast.LENGTH_SHORT).show();
                try {
                    MusicUtils.mService.goToNext();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSingle() {
                Toast.makeText(MainActivity.this, "single", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), Play_Activity.class));
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            finish();
            return true;
        } else if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        } else if (id == R.id.action_shuffle) {
            //new ListPlaylistDailog().show(getFragmentManager(),"5");
        } else if (id == R.id.action_playlist) {
            new NewPlaylistDialog().show( getSupportFragmentManager(), "yo");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicUtils.mService = IMusicParent.Stub.asInterface(service);
        Log.i(TAG, "onServiceConnected: ");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        MusicUtils.mService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        if (myf == null) {
            myf = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IMusicChild.META_CHANGED);
            intentFilter.addAction(IMusicChild.NEXT_ACTION);
            registerReceiver(myf, intentFilter);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       // gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
        if (myf != null) {
            unregisterReceiver(myf);
            myf = null;
        }
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        stopService(new Intent(this, IMusicChild.class));
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();

            if (action.equals(IMusicChild.META_CHANGED)) {
                Bundle data = intent.getExtras();
                    mTrack.setText(data.getString("tName"));
                    mArtist.setText(data.getString("aName"));
                    ImageLoader.getInstance().displayImage("file:///" + data.getString("albumArt"), im);
            } else if (action.equals(IMusicChild.NEXT_ACTION)) {

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
                onSingle();
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;

                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    Log.i("xxxxxxxxx", "onFling: " + velocityX +"  -  " +diffX + " - " + SWIPE_THRESHOLD);
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
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }

        public void onSingle() {

        }
    }

}

