package mp.ajapps.musicplayerfree.Fragments;



import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.Adapters.DndAdapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.NowPlayingLoader;
import mp.ajapps.musicplayerfree.Helpers.OnStartDragListener;
import mp.ajapps.musicplayerfree.Helpers.SimpleItemTouchHelperCallback;
import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NowPlayingListFragment extends Fragment implements LoaderManager.LoaderCallbacks <List<Song>>,OnStartDragListener{
    RecyclerView mList;
    private DndAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    public NowPlayingListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("kiss", "onCreateView: ");
        View v = inflater.inflate(R.layout.fragment_now_playing_list, container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            v.setPadding(0, getStatusBarHeight(), 0, 0);
        }
        mList = (RecyclerView) v.findViewById(R.id.view5);
        mAdapter = new DndAdapter(R.layout.now_playing, null, this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mList);
        mList.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mList.setLayoutManager(mLayoutManager);
        getLoaderManager().initLoader(2, null, this).forceLoad();
        return v;
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
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new NowPlayingLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data.isEmpty()) {
            return;
        }
        ArrayList<Song> mSongs = new ArrayList<>();
        for (final Song song : data) { mSongs.add(song); }
        mAdapter.changeCursor(mSongs);
        mAdapter.notifyDataSetChanged();
        try {
            mList.scrollToPosition(MusicUtils.mService.getQueuePosition() - 2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {}

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
