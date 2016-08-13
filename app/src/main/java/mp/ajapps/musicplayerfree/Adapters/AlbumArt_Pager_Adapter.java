package mp.ajapps.musicplayerfree.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import mp.ajapps.musicplayerfree.Fragments.AlbumArtFragment;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 8/4/2015.
 */
public class AlbumArt_Pager_Adapter extends FragmentStatePagerAdapter {
    private static final String TAG = AlbumArt_Pager_Adapter.class.getSimpleName();
    private int mPlaylistLen = 0;
    ImageLoader imageLoader;

    public AlbumArt_Pager_Adapter(FragmentManager fm, Context c) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        long trackID = MusicUtils.getQueueItemAtPosition(position);
        AlbumArtFragment ag = AlbumArtFragment.newInstance(trackID);
        return ag;
    }

    @Override
    public int getCount() {
        return mPlaylistLen;
    }

    public void setLength(int len){
        this.mPlaylistLen = len;
    }
}
