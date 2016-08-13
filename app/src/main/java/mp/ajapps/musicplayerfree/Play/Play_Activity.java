package mp.ajapps.musicplayerfree.Play;

import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import mp.ajapps.musicplayerfree.Activity.SearchActivity;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;

public class Play_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     //   getSupportActionBar().setHomeButtonEnabled(true);
        /*android.support.v7.app.ActionBar ab= getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);*/
       // Toolbar toolbar = (Toolbar) findViewById(R.id.play_tool);
       // setSupportActionBar(toolbar);
        MusicUtils.initImageCacher(this);
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
            case R.id.action_repeat :
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
            String s = status == 1 ?"Stop Shuffling" : "Start Shuffling";
            item.setTitle(s);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateRepeat(MenuItem item) {
        try {
            int status = MusicUtils.mService.toggleShuffle();
            String s = status == 1 ?"Stop Repeating" : "Start Repeating";
            item.setTitle(s);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
