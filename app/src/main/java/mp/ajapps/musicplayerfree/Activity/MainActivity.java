package mp.ajapps.musicplayerfree.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
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
import mp.ajapps.musicplayerfree.Helpers.BugManager;
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
    Boolean mStayOn, mTheme = false;

    @Override
    protected void onStart() {
        Toast.makeText(MainActivity.this, "starty", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, IMusicChild.class));
        Boolean b = bindService(new Intent(this, IMusicChild.class), this, 0);
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onStop() {
        ImageLoader.getInstance().clearMemoryCache();
        unbindService(this);
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
        //ChangeLinear();
        SharedPreferences settings = getSharedPreferences("settings", 0);
        mTheme = settings.getBoolean("theme", false);
        if (mTheme) {
            themeDark();
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.vpView);
        MusicUtils.initImageCacher(this);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitle("My Library");
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_view);
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
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new SuggestionFragment());
        fragments.add(TrackFragment.newInstance());
        fragments.add(AlbumFragment.newInstance());
        fragments.add(new ArtistFragment());

        mPagerAdap = new TrackPagerAdap(getSupportFragmentManager(), fragments);
        mPager.setAdapter(mPagerAdap);
        mPager.setCurrentItem(1);
        mPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(mPager);
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
                try {
                    MusicUtils.mService.goToPrev();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSwipeRight() {
                try {
                    MusicUtils.mService.goToNext();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSingle() {
                startActivity(new Intent(getApplicationContext(), Play_Activity.class));
            }
        });

        mStayOn = settings.getBoolean("checkbox", false);
        toggleStayOn();
    }

    private void toggleStayOn() {
        if (mStayOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_set_stayOn);
        item.setChecked(mStayOn);
        item = menu.findItem(R.id.action_toggletheme);
        item.setChecked(mTheme);
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
            return true;
        } else if (id == R.id.action_playlist) {
            new NewPlaylistDialog().show(getSupportFragmentManager(), "yo");
            return true;
        } else if (id == R.id.action_set_stayOn) {
            boolean status = !item.isChecked();
            item.setChecked(status);
            mStayOn = status;
            SharedPreferences settings = getSharedPreferences("settings", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("checkbox", status);
            editor.apply();
            toggleStayOn();
            return true;
        } else if (id == R.id.action_toggletheme) {
            boolean status = !item.isChecked();
            item.setChecked(status);
            mStayOn = status;
            SharedPreferences settings = getSharedPreferences("settings", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("theme", status);
            editor.apply();
            changeToTheme();
        }
        return super.onOptionsItemSelected(item);
    }

    private void themeDark() {
        setTheme(R.style.AppTheme_Dark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_primary_dark));
        }
    }

    public void changeToTheme() {
        finish();
        startActivity(new Intent(this, this.getClass()));
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicUtils.mService = IMusicParent.Stub.asInterface(service);
        ChangeLinear();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        MusicUtils.mService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        return super.onTouchEvent(event);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myf != null) {
            unregisterReceiver(myf);
            myf = null;
        }
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(MainActivity.this, "destroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
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

    private void ChangeLinear() {

        try {
            mArtist.setText(MusicUtils.mService.getArtistName());
            mTrack.setText(MusicUtils.mService.getTrackName());
            String path = MusicUtils.mService.getAlbumArt();
            ImageLoader.getInstance().displayImage("file:///" + path, im);
        } catch (RemoteException e) {
            e.printStackTrace();
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

        public void onSwipeRight() {}
        public void onSwipeLeft() {}
        public void onSwipeTop() {}
        public void onSwipeBottom() {}
        public void onSingle() {}
    }
}

