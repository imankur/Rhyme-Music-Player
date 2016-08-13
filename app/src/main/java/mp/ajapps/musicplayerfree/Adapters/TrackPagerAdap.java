package mp.ajapps.musicplayerfree.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sharing Happiness on 6/15/2015.
 */
public class TrackPagerAdap extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private String tabtitles[] = new String[] { "Suggestions", "Tracks", "AlbumModel" ,"Artits","Folders" };
    public TrackPagerAdap(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}
