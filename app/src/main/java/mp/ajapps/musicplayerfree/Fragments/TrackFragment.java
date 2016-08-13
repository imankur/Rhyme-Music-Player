package mp.ajapps.musicplayerfree.Fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import mp.ajapps.musicplayerfree.Adapters.TrackAdapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Widgets.ListPlaylistDailog;


public class TrackFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TrackAdapter.myOnCLickInterface, ActionMode.Callback {

    protected LinearLayoutManager mLayoutManager;
    protected TrackAdapter mAdpt;
    private boolean isMultiSelectMode = false;
    String TAG = "cooool";
    String[] mProjection;
    ActionMode mActionMode;
    Toolbar mToolbar;
    StringBuilder where;
    Cursor mCursor;
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
                BaseColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ARTIST
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        View v = inflater.inflate(R.layout.fragment_track, container, false);
        mRecycleView = (RecyclerView) v.findViewById(R.id.mRecycleView);
        act = (AppCompatActivity)getActivity();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdpt = new TrackAdapter(R.layout.track_row, this);
        mRecycleView.setAdapter(mAdpt);
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
    public void myOnClick(int pos) {
        if (mActionMode == null) {
            MusicUtils.setAndPlay(mCursor, pos);
        } else {
            this.doToggle(pos);
        }
    }

    @Override
    public void myOnLongClick(int pos) {
        mActionMode = act.startSupportActionMode(this);
        this.doToggle(pos);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.action_playlist , menu);
        return true;
    }

    private void doToggle(int pos) {
        this.mAdpt.toggleSelection(pos);
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
                return false;
        }

    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mAdpt.clearSelections();
    }
}
