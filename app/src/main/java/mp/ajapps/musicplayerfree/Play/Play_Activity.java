package mp.ajapps.musicplayerfree.Play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Adapters.TrackPagerAdap;
import mp.ajapps.musicplayerfree.Fragments.NowPlayingListFragment;
import mp.ajapps.musicplayerfree.Fragments.PlaylistFragment;
import mp.ajapps.musicplayerfree.Fragments.RecentFragment;
import mp.ajapps.musicplayerfree.Fragments.TrackFragment;
import mp.ajapps.musicplayerfree.Helpers.BitmapUtils;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;

public class Play_Activity extends AppCompatActivity {
    ViewPager mPager;
    TrackPagerAdap mPagerAdap;
    ImageButton m;
    private MyReceiver myf = null;
    private ImageView ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }
        Toast.makeText(this, "" + toolbar.getHeight(), Toast.LENGTH_LONG).show();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ll = (ImageView) findViewById(R.id.myll);
        //   getSupportActionBar().setHomeButtonEnabled(true);
        /*android.support.v7.app.ActionBar ab= getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);*/
        // Toolbar toolbar = (Toolbar) findViewById(R.id.play_tool);
        // setSupportActionBar(toolbar);
        MusicUtils.initImageCacher(this);
        updateBlur();
        mPager = (ViewPager) findViewById(R.id.mViewp);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        //  fragments.add(SuggestFragment.newInstance());
        fragments.add(PlayFragment.newInstance());
        //fragments.add(TrackFragment.newInstance());
        //fragments.add(AlbumFragment.newInstance());

        fragments.add(new NowPlayingListFragment());
        mPagerAdap = new TrackPagerAdap(getSupportFragmentManager(), fragments);
        mPager.setAdapter(mPagerAdap);
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(3);
        m = (ImageButton) findViewById(R.id.button);
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doUpdate();
            }

        });

    }

    private void doUpdate () {
        if (mPager.getCurrentItem() == 0) {
            mPager.setCurrentItem(1);
            m.setImageResource(R.drawable.ic_navigation_expand_more);
        } else {
            mPager.setCurrentItem(0);
            m.setImageResource(R.drawable.ic_navigation_expand_less);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_shuffle:
                this.updateShuffle(item);
                return true;
            case R.id.action_repeat:
                this.updateRepeat(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        MenuItem mSHuffle = menu.findItem(R.id.action_shuffle);
        // updateShuffle(mSH);
        return true;
    }

    public void updateShuffle(MenuItem item) {
        try {
            int status = MusicUtils.mService.toggleRepeat();
            String s = status == 1 ? "Stop Shuffling" : "Start Shuffling";
            item.setTitle(s);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateRepeat(MenuItem item) {
        try {
            int status = MusicUtils.mService.toggleShuffle();
            String s = status == 1 ? "Stop Repeating" : "Start Repeating";
            item.setTitle(s);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateBlur() {

        ImageLoader.getInstance().displayImage("file:///" + MusicUtils.getCurrentAlbumArt(), this.ll, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap bmp = BitmapUtils.createBlurredBitmap(loadedImage);
                ll.setImageBitmap(bmp);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(IMusicChild.META_CHANGED)) {
                 updateBlur();
            } else if (action.equals(IMusicChild.NEXT_ACTION)) {

            }
        }
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
    protected void onStop() {
        super.onStop();

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
