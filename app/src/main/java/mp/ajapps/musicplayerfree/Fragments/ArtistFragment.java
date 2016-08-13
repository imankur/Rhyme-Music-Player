package mp.ajapps.musicplayerfree.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.Activity.Artist_Details;
import mp.ajapps.musicplayerfree.Adapters.ArtistAdapter;
import mp.ajapps.musicplayerfree.Helpers.ArtistLoader;
import mp.ajapps.musicplayerfree.POJOS.Artist;
import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;


public class ArtistFragment extends Fragment implements  LoaderManager.LoaderCallbacks <List<Artist>>, ArtistAdapter.ClickManager {


    public ArtistFragment() {}
    private ArtistAdapter mAdapter;
    private RecyclerView mList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        mList = (RecyclerView) v.findViewById(R.id.mList);
        mAdapter = new ArtistAdapter(this);
        mList.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        getLoaderManager().restartLoader(1, null, this).forceLoad();
    }

    @Override
    public Loader<List<Artist>> onCreateLoader(int id, Bundle args) {
        return new ArtistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Artist>> loader, List<Artist> data) {
        if (data.isEmpty()) {
            return;
        }
        ArrayList<Artist> mSongs = new ArrayList<>();
        for (final Artist song : data) { mSongs.add(song); }
        mAdapter.setdata(mSongs);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Artist>> loader) {}

    @Override
    public void mOnClick(long id, String name) {
        Intent i = new Intent(getActivity(), Artist_Details.class);
        i.putExtra("mId", id).putExtra("mName", name);
        startActivity(i);
    }
}
