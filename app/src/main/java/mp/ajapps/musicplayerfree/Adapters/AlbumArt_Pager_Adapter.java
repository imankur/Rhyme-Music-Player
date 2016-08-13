package mp.ajapps.musicplayerfree.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;

import mp.ajapps.musicplayerfree.Fragments.AlbumArtFragment;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;

/**
 * Created by Sharing Happiness on 8/4/2015.
 */
public class AlbumArt_Pager_Adapter extends FragmentStatePagerAdapter {
    private static final String TAG = AlbumArt_Pager_Adapter.class.getSimpleName();
    ImageLoader imageLoader;
    private int mPlaylistLen = 0;

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

    public void setLength(int len) {
        this.mPlaylistLen = len;
    }
}
