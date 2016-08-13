package mp.ajapps.musicplayerfree.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class TrackPagerAdap extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments;
    private String tabtitles[] = new String[]{"Suggestions", "Tracks", "Album", "Artists", "Folders"};

    public TrackPagerAdap(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}
