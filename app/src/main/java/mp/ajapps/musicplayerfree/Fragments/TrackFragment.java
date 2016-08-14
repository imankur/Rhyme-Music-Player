package mp.ajapps.musicplayerfree.Fragments;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;

import mp.ajapps.musicplayerfree.Adapters.TrackAdapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Widgets.ListPlaylistDailog;
import mp.ajapps.musicplayerfree.Widgets.RecyclerPauseOnScrollListener;
import mp.ajapps.musicplayerfree.Widgets.SimpleDividerItemDecoration;


public class TrackFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TrackAdapter.myOnCLickInterface, ActionMode.Callback {

    protected LinearLayoutManager mLayoutManager;
    protected TrackAdapter mAdpt;
    private boolean isMultiSelectMode = false;
    String TAG = "cooool";
    String[] mProjection;
    long[] mTrackIds;
    ActionMode mActionMode;
    Toolbar mToolbar;
    StringBuilder where;
    FastScroller fastScroller;
    Cursor mCursor;
    private boolean mFirst = true;
    String mWhere;
    AppCompatActivity act;
    String mSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
    Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private RecyclerView mRecycleView;

    public TrackFragment() {
    }

    public static TrackFragment newInstance() {
        TrackFragment fragment = new TrackFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        where = new StringBuilder();
        where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1").append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
        mWhere = where.toString();
        mProjection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ARTIST,MediaStore.Audio.Media.ALBUM_ID
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(4, null, this);
        View v = inflater.inflate(R.layout.fragment_track, container, false);
        mRecycleView = (RecyclerView) v.findViewById(R.id.mRecycleView);
        act = (AppCompatActivity)getActivity();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mRecycleView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdpt = new TrackAdapter(getActivity(), R.layout.track_row, this, getFragmentManager());
        mRecycleView.setAdapter(mAdpt);

      //  mRecycleView.addOnScrollListener(new RecyclerPauseOnScrollListener(ImageLoader.getInstance(), true, false));
        fastScroller = (FastScroller) v.findViewById(R.id.fastscroll);
        fastScroller.setScrollBarSize(20);

        fastScroller.setRecyclerView(mRecycleView);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), mUri, mProjection, mWhere, null, mSortOrder);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser && mActionMode != null) {
            mActionMode.finish();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        if (mCursor != null) mCursor.close();
        mAdpt.changeCursor(data);
        mAdpt.notifyDataSetChanged();
        mCursor = data;
        this.mTrackIds = MusicUtils.getSongListForCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdpt.changeCursor(null);
        mAdpt.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor != null) mCursor.close();
    }

    @Override
    public void myOnClick(int pos, View v) {
        if (mActionMode == null) {
          /*  if (!mFirst) {
                try {
                    MusicUtils.mService.setAndPlayQueue(pos);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {*/
                MusicUtils.setAndPlay(mCursor, pos);
                mFirst = false;
            //7}
        } else {
            this.doToggle(pos);
        }
    }

    @Override
    public void myOnLongClick(int pos, View v) {
        if (mActionMode == null) {
            mActionMode = act.startSupportActionMode(this);
            this.doToggle(pos);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_playlist , menu);
        return true;
    }

    private void doToggle(int pos) {
        this.mAdpt.toggleSelection(pos);
        if (mAdpt.getSelectedItemCount() == 0) {
            mActionMode.finish();
            return;
        }
        mActionMode.setTitle(mAdpt.getSelectedItemCount()+"");
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                new ListPlaylistDailog().setList(mAdpt.getSelectedItems()).show(getFragmentManager(),"5");
                mActionMode.finish();
                return true;
            case R.id.action_playSelected:
                try {
                    MusicUtils.setAndPLay(mAdpt.getSelectedItems(),0);
                    mActionMode.finish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                mActionMode.finish();
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mAdpt.clearSelections();
    }


}
