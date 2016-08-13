package mp.ajapps.musicplayerfree.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Adapters.TrackAdapter;
import mp.ajapps.musicplayerfree.Helpers.CursorSorter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.RecentStore;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Fragments.dummy.DummyContent.DummyItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * interface.
 */
public class RecentFragment extends Fragment implements TrackAdapter.myOnCLickInterface {

    private LinearLayoutManager mLayoutManager;
    private TrackAdapter mAdpt;
    Cursor c;

    public RecentFragment() {
    }

    public static RecentFragment newInstance() {
        RecentFragment fragment = new RecentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_list, container, false);
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdpt = new TrackAdapter(R.layout.track_row, this);
        c = makeRecentTracksCursor();
        mAdpt.changeCursor(c);
        mRecycleView.setAdapter(mAdpt);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void myOnClick(int pos) {
        MusicUtils.setAndPlay(c,pos);
    }

    public Cursor getCursor () {
        CursorSorter cs = makeRecentTracksCursor();
        if (cs != null) {
            ArrayList<Long> missingIds = cs.getMissingIds();
            if (missingIds != null && missingIds.size() > 0) {
                // for each unfound id, remove it from the database
                // this codepath should only really be hit if the user removes songs
                // outside of the Eleven app
                for (long id : missingIds) {
                        RecentStore.getInstance(this.getActivity()).removeItem(id);
                    }
                }
            }

    return cs;
    }
    private CursorSorter makeRecentTracksCursor() {
        // first get the top results ids from the internal database

        Cursor songs = RecentStore.getInstance(this.getActivity()).queryRecentIds();
        Log.i("xxxx", "onCreateView: " + songs.getCount());
        try {
            return MusicUtils.makeSortedCursor(this.getActivity(), songs,
                    songs.getColumnIndex("songid"));
        } finally {
            if (songs != null) {
                songs.close();
                songs = null;
            }
        }
    }
}
