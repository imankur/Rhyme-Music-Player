package mp.ajapps.musicplayerfree.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import mp.ajapps.musicplayerfree.Adapters.TrackPagerAdap;
import mp.ajapps.musicplayerfree.Fragments.AlbumFragment;
import mp.ajapps.musicplayerfree.Fragments.RecentFragment;

import mp.ajapps.musicplayerfree.Fragments.TrackFragment;
import mp.ajapps.musicplayerfree.Helpers.BugManager;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.IMusicParent;
import mp.ajapps.musicplayerfree.Play.Play_Activity;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    ViewPager mPager;
    TrackPagerAdap mPagerAdap;
    RelativeLayout ll;
    MyReceiver myf = null;
    TextView mTrack, mArtist;
    ImageView im, mPlayim;

    @Override
    protected void onStart() {
        startService(new Intent(this, IMusicChild.class));
        Boolean b = bindService(new Intent(this, IMusicChild.class), this, 0);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(this);
        super.onStop();
    }

    private  String getDensityName(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
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
        return "ldpi";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.vpView);
        MusicUtils.initImageCacher(this);
        Toast.makeText(this,"Screen density is : " + getDensityName(this), Toast.LENGTH_LONG).show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
      //  Log.i("dentis", "onCreate:  cff555fff " + metrics.heightPixels/metrics.density +"  "+ metrics.widthPixels/metrics.density +" " + metrics.densityDpi)  ;
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar_main);
        setSupportActionBar(toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_view);
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
        fragments.add(RecentFragment.newInstance());
        fragments.add(TrackFragment.newInstance());
        fragments.add(AlbumFragment.newInstance());

      //  fragments.add(PlayFragment.newInstance());
        mPagerAdap = new TrackPagerAdap(getSupportFragmentManager(), fragments);
        mPager.setAdapter(mPagerAdap);
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(mPager);

        mPlayim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             try {
                    MusicUtils.mService.togglePlay();
                    if(MusicUtils.mService.isPlaying()) {
                        mPlayim.setImageResource(R.drawable.ic_red_pause);
                    } else {
                        mPlayim.setImageResource(R.drawable.ic_red_play);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            String path = intent.getExtras().getString("albumArt");
            if (action.equals(IMusicChild.META_CHANGED)) {

/*
                try {
                   // String s = MusicUtils.mService.getTrackName();
                    //s = (s == null)? MusicUtils.mService.getTrackName()  : "  ";
                   // mTrack.setText(s);
//                    mArtist.setText(MusicUtils.mService.getArtistName());
                    ImageLoader.getInstance().displayImage("file:///" + path,im);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
*/


            } else if (action.equals(IMusicChild.NEXT_ACTION)) {

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicUtils.mService = IMusicParent.Stub.asInterface(service);
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
    public void onPause() {
        super.onPause();
        if (myf != null) {
            unregisterReceiver(myf);
            myf = null;
        }
    }
}

