package mp.ajapps.musicplayerfree.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.Activity.PlaylistDetails;
import mp.ajapps.musicplayerfree.Adapters.PlaylistAdapter;
import mp.ajapps.musicplayerfree.Helpers.PlaylistLoader;
import mp.ajapps.musicplayerfree.POJOS.Playlist;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Widgets.NewPlaylistDialog;

public class PlaylistFragment extends Fragment implements LoaderCallbacks<List<Playlist>>, PlaylistAdapter.clickManger {

    ActionMode actionMode;
    GestureDetectorCompat gestureDetector;
    private PlaylistAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mList;

    public PlaylistFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // menu.findItem(R.id.action_shuffle).setVisible(false);
        menu.clear();

        inflater.inflate(R.menu.menu_main_playlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
       // setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);
        mList = (RecyclerView) view.findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mList.setLayoutManager(mLayoutManager);
        getLoaderManager().initLoader(3, null, this).forceLoad();
        mAdapter = new PlaylistAdapter(this);
        mList.setAdapter(mAdapter);

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
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        return new PlaylistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Playlist>> loader, List<Playlist> data) {
        ArrayList<Playlist> mList = new ArrayList<>();
        for (final Playlist playlist : data) {
            mList.add(playlist);
        }
        mAdapter.changeData(mList);
    }


    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
        mAdapter.changeData(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_playlist) {
            new NewPlaylistDialog().show(getFragmentManager(), "yo");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCilckEvent(int position, long id,  String name) {
        Intent i = new Intent(getActivity(), PlaylistDetails.class);
        i.putExtra("playlist_id", id);
        i.putExtra("mName", name);
        startActivity(i);
    }
}
